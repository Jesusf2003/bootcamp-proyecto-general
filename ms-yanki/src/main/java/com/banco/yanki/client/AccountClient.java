package com.banco.yanki.client;

import com.banco.yanki.exception.AccountClientException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Cliente reactivo hacia ms-accounts: Yanki no mueve dinero por su
 * cuenta, delega la transferencia real a la cuenta de debito
 * vinculada de cada usuario, reusando toda la logica de negocio
 * (comisiones, validaciones, publicacion de eventos Kafka) que ya
 * vive en ms-accounts.
 */
@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public AccountClient(WebClient.Builder webClientBuilder,
                          CircuitBreakerRegistry circuitBreakerRegistry,
                          Environment env) {
        String baseUrl = env.getProperty("clients.accounts.url", "http://ms-accounts");
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("accountsClient");
    }

    public Mono<Void> transfer(String sourceAccountId, String targetAccountId, BigDecimal amount) {
        return webClient.post()
                .uri("/api/v1/accounts/{id}/transfers", sourceAccountId)
                .bodyValue(new TransferBody(targetAccountId, amount, "Envio Yanki"))
                .retrieve()
                .toBodilessEntity()
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(ex -> log.error("Error ejecutando transferencia Yanki en ms-accounts: {}", ex.getMessage()))
                .onErrorMap(ex -> !(ex instanceof AccountClientException),
                        ex -> new AccountClientException(
                                "No se pudo completar el envio: " + ex.getMessage()))
                .then();
    }

    private record TransferBody(String targetAccountId, BigDecimal amount, String description) {
    }
}
