package com.banco.accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Microservicio de cuentas bancarias del sistema bancario.
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class MsAccountsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsAccountsApplication.class, args);
    }
}
