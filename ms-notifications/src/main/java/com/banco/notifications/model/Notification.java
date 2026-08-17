package com.banco.notifications.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Notificacion simulada, generada al consumir un evento de Kafka. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private String sourceTopic;
    private String referenceId;
    private String message;
    private LocalDateTime receivedAt;
}
