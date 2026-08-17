package com.banco.accounts.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Cliente reactivo hacia ms-credits. Se usa exclusivamente para
 * validar el requisito de la Parte II: un cliente debe tener ya una
 * tarjeta de credito con el banco para acceder a los perfiles
 * VIP (ahorro) o PYME (cuenta corriente).
 */
@Slf4j
@Component
public class CreditClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public CreditClient(WebClient.Builder webClientBuilder,
                         CircuitBreakerRegistry circuitBreakerRegistry,
                         Environment env) {
        String baseUrl = env.getProperty("clients.credits.url", "http://ms-credits");
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("creditsClient");
    }

    public Mono<Boolean> hasCreditCard(String customerId) {
        return webClient.get()
                .uri("/api/v1/cards/customer/{customerId}/exists", customerId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(ex -> log.error("Error consultando ms-credits: {}", ex.getMessage()))
                .onErrorReturn(false);
    }

    public Mono<Boolean> hasOverdueDebt(String customerId) {
        return webClient.get()
                .uri("/api/v1/credits/customer/{customerId}/overdue-debt", customerId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(ex -> log.error("Error consultando deuda vencida en ms-credits: {}", ex.getMessage()))
                // Ante fallo del microservicio de creditos, se permite continuar (fail-open)
                // para no bloquear toda la apertura de cuentas por una caida transitoria.
                .onErrorReturn(false);
    }
}
