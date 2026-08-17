package com.banco.credits.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Cliente reactivo hacia ms-customers, con circuit breaker
 * (Resilience4j, timeout 2s) para validar la existencia y el tipo
 * de cliente antes de otorgar un credito o tarjeta.
 */
@Slf4j
@Component
public class CustomerClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public CustomerClient(WebClient.Builder webClientBuilder,
                           CircuitBreakerRegistry circuitBreakerRegistry,
                           Environment env) {
        String baseUrl = env.getProperty("clients.customers.url", "http://ms-customers");
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("customersClient");
    }

    public Mono<CustomerDto> findById(String customerId) {
        return webClient.get()
                .uri("/api/v1/customers/{id}", customerId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new CustomerClientException("Cliente no encontrado: " + customerId)))
                .bodyToMono(CustomerDto.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(ex -> !(ex instanceof CustomerClientException),
                        ex -> Mono.error(new CustomerClientException(
                                "ms-customers no disponible en este momento, intente nuevamente")));
    }
}
