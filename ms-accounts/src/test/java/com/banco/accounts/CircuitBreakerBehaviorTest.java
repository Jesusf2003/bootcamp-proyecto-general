package com.banco.accounts;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba automatizada del circuit breaker aplicado a las llamadas
 * salientes de ms-accounts hacia ms-customers/ms-credits (ver
 * {@link com.banco.accounts.client.CustomerClient} y
 * {@link com.banco.accounts.client.CreditClient}), replicando los
 * mismos parametros de resilience4j.circuitbreaker.configs.default
 * del application.yml de este microservicio.
 *
 * No depende de red real ni de que el otro microservicio este arriba
 * o caido: construye su propio CircuitBreakerRegistry con la misma
 * configuracion, y prueba el comportamiento de apertura/cierre contra
 * llamadas simuladas.
 */
class CircuitBreakerBehaviorTest {

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(300))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        circuitBreaker = CircuitBreakerRegistry.of(config).circuitBreaker("customersClientTest");
    }

    @Test
    void shouldOpenAfterHalfOfTheCallsFail() {
        // 5 exitosas + 5 fallidas = exactamente 50%, alcanza el umbral.
        for (int i = 0; i < 5; i++) {
            Mono<String> okCall = Mono.just("cliente-encontrado")
                    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
            StepVerifier.create(okCall).expectNext("cliente-encontrado").verifyComplete();
        }
        for (int i = 0; i < 5; i++) {
            Mono<String> failingCall = Mono.<String>error(new RuntimeException("ms-customers no responde"))
                    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
            StepVerifier.create(failingCall).expectError(RuntimeException.class).verify();
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldRejectCallsImmediatelyOnceOpenProtectingTheCallingService() {
        circuitBreaker.transitionToOpenState();

        AtomicBoolean webClientWasInvoked = new AtomicBoolean(false);
        Mono<String> simulatedWebClientCall = Mono.fromCallable(() -> {
                    webClientWasInvoked.set(true);
                    return "no deberia llegar aqui";
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));

        StepVerifier.create(simulatedWebClientCall)
                .expectError(CallNotPermittedException.class)
                .verify();

        assertThat(webClientWasInvoked.get()).isFalse();
    }
}
