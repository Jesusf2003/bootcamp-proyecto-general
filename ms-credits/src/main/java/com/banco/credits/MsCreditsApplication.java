package com.banco.credits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/** Microservicio de creditos y tarjetas de credito del sistema bancario. */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class MsCreditsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsCreditsApplication.class, args);
    }
}
