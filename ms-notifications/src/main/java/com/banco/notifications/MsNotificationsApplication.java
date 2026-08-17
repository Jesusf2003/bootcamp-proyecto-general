package com.banco.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microservicio consumidor de eventos Kafka: simula notificaciones al cliente. */
@SpringBootApplication
public class MsNotificationsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsNotificationsApplication.class, args);
    }
}
