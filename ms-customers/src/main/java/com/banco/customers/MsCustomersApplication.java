package com.banco.customers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Microservicio de gestion de clientes del sistema bancario.
 * Parte I del proyecto.
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class MsCustomersApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsCustomersApplication.class, args);
    }
}
