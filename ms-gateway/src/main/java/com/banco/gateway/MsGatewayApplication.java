package com.banco.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway del sistema bancario. Enruta las peticiones externas
 * hacia los microservicios registrados en Eureka y aplica circuit
 * breaker (Resilience4j) con timeout de 2 segundos.
 */
@SpringBootApplication
public class MsGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsGatewayApplication.class, args);
    }
}
