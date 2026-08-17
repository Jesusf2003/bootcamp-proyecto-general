package com.banco.gateway;

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
 * Prueba automatizada del comportamiento del circuit breaker, replicando
 * exactamente los mismos parametros configurados en application.yml
 * (ventana de 10 llamadas, minimo de 10 llamadas para evaluar, 50% de
 * fallos como umbral). El tiempo de espera en estado abierto se acelera
 * a 300ms solo para que el test corra rapido; en produccion es 10s.
 *
 * Esta prueba existe porque el requerimiento de circuit breaker no se
 * puede validar solo con una demo manual (apagar un contenedor): aqui
 * se verifica de forma repetible, sin red real, que:
 *   1. Con menos fallos que el minimo de llamadas, el circuito sigue CERRADO.
 *   2. Superado el umbral de fallos, el circuito se ABRE.
 *   3. Mientras esta abierto, las llamadas fallan de inmediato
 *      (CallNotPermittedException) SIN invocar al proveedor real -- este
 *      es el comportamiento central que justifica el patron.
 *   4. Pasado el tiempo de espera y con llamadas de prueba exitosas en
 *      HALF_OPEN, el circuito vuelve a CERRARSE solo.
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
        circuitBreaker = CircuitBreakerRegistry.of(config).circuitBreaker("gatewayTestCircuitBreaker");
    }

    @Test
    void shouldStayClosedWhileBelowMinimumNumberOfCalls() {
        // Solo 4 llamadas, todas fallidas: no alcanza el minimo de 10 para evaluar.
        for (int i = 0; i < 4; i++) {
            Mono<String> failingCall = Mono.<String>error(new RuntimeException("downstream caido"))
                    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
            StepVerifier.create(failingCall).expectError(RuntimeException.class).verify();
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldOpenCircuitWhenFailureRateExceedsThreshold() {
        // 10 llamadas fallidas seguidas = 100% de fallo, supera el umbral del 50%.
        for (int i = 0; i < 10; i++) {
            Mono<String> failingCall = Mono.<String>error(new RuntimeException("downstream caido"))
                    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
            StepVerifier.create(failingCall).expectError(RuntimeException.class).verify();
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldFailFastWithoutCallingDownstreamWhenCircuitIsOpen() {
        circuitBreaker.transitionToOpenState();

        AtomicBoolean downstreamWasCalled = new AtomicBoolean(false);
        Mono<String> call = Mono.fromCallable(() -> {
                    downstreamWasCalled.set(true);
                    return "respuesta real del microservicio";
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));

        StepVerifier.create(call)
                .expectError(CallNotPermittedException.class)
                .verify();

        assertThat(downstreamWasCalled.get())
                .as("el circuito abierto no debe siquiera invocar la llamada real")
                .isFalse();
    }

    @Test
    void shouldTransitionBackToClosedAfterWaitDurationAndSuccessfulProbes() throws InterruptedException {
        circuitBreaker.transitionToOpenState();
        Thread.sleep(400); // > waitDurationInOpenState (300ms) para pasar a HALF_OPEN

        // Las llamadas de prueba en HALF_OPEN, si son exitosas, cierran el circuito.
        for (int i = 0; i < 3; i++) {
            Mono<String> okCall = Mono.just("ok")
                    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
            StepVerifier.create(okCall).expectNext("ok").verifyComplete();
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
