package com.banco.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Servidor de registro y descubrimiento de servicios (Eureka).
 * Todos los microservicios del banco se registran aqui y el
 * API Gateway lo usa para enrutar dinamicamente.
 */
@SpringBootApplication
@EnableEurekaServer
public class MsEurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsEurekaApplication.class, args);
    }
}
