package com.banco.notifications.listener;

import com.banco.notifications.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consume el topico "account-movements" publicado por ms-accounts y
 * simula el envio de una notificacion (push/SMS/email) al cliente.
 * Este es el consumidor de la arquitectura orientada a eventos de
 * la Parte III: desacopla a ms-accounts de la logica de notificacion.
 */
@Slf4j
@Component
public class AccountMovementListener {

    private final NotificationStore notificationStore;

    public AccountMovementListener(NotificationStore notificationStore) {
        this.notificationStore = notificationStore;
    }

    @KafkaListener(topics = "account-movements", groupId = "ms-notifications")
    public void onMovement(String payload) {
        log.info("Evento de movimiento recibido: {}", payload);
        Notification notification = Notification.builder()
                .sourceTopic("account-movements")
                .referenceId(extractField(payload, "accountId"))
                .message("Se registro un movimiento en su cuenta: " + payload)
                .receivedAt(LocalDateTime.now())
                .build();
        notificationStore.add(notification);
    }

    /** Extraccion simple de un campo del JSON plano, sin acoplar una libreria completa de parsing. */
    private String extractField(String json, String field) {
        try {
            String marker = "\"" + field + "\":\"";
            int start = json.indexOf(marker);
            if (start < 0) {
                return "desconocido";
            }
            start += marker.length();
            int end = json.indexOf('"', start);
            return json.substring(start, end);
        } catch (Exception ex) {
            return "desconocido";
        }
    }
}
