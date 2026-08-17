package com.banco.accounts.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Cliente reactivo hacia ms-customers, usado para validar la
 * existencia y el tipo de un cliente antes de aperturar una cuenta.
 *
 * Protegido con Resilience4j Circuit Breaker (timeout de 2s
 * configurado en application.yml, tal como exige el enunciado)
 * para que una caida de ms-customers no bloquee ms-accounts.
 */
@Slf4j
@Component
public class CustomerClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public CustomerClient(WebClient.Builder webClientBuilder,
                           CircuitBreakerRegistry circuitBreakerRegistry,
                           org.springframework.core.env.Environment env) {
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
                .doOnError(WebClientResponseException.class,
                        ex -> log.error("Error consultando ms-customers: {}", ex.getMessage()))
                .onErrorResume(ex -> !(ex instanceof CustomerClientException),
                        ex -> Mono.error(new CustomerClientException(
                                "ms-customers no disponible en este momento, intente nuevamente")));
    }

    public Mono<CustomerDto> updateProfile(String customerId, String profile) {
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/customers/{id}/profile")
                        .queryParam("profile", profile)
                        .build(customerId))
                .retrieve()
                .bodyToMono(CustomerDto.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(ex -> Mono.error(new CustomerClientException(
                        "No se pudo actualizar el perfil del cliente: " + ex.getMessage())));
    }
}
