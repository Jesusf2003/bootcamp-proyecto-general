package com.banco.yanki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/** Microservicio de monedero movil Yanki. */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class MsYankiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsYankiApplication.class, args);
    }
}
