package com.banco.notifications.listener;

import com.banco.notifications.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Consume el topico "credit-events" publicado por ms-credits (pagos y consumos). */
@Slf4j
@Component
public class CreditEventListener {

    private final NotificationStore notificationStore;

    public CreditEventListener(NotificationStore notificationStore) {
        this.notificationStore = notificationStore;
    }

    @KafkaListener(topics = "credit-events", groupId = "ms-notifications")
    public void onCreditEvent(String payload) {
        log.info("Evento de credito recibido: {}", payload);
        Notification notification = Notification.builder()
                .sourceTopic("credit-events")
                .referenceId("n/a")
                .message("Se registro un evento en su producto de credito: " + payload)
                .receivedAt(LocalDateTime.now())
                .build();
        notificationStore.add(notification);
    }
}
