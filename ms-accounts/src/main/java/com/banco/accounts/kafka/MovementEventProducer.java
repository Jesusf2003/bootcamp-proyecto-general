package com.banco.accounts.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Publica un evento asincrono en Kafka cada vez que se registra un
 * movimiento (deposito, retiro o transferencia). ms-notifications
 * consume este topico y simula el envio de una notificacion al
 * cliente (Parte III: arquitectura orientada a eventos).
 *
 * El fallo al publicar NO revierte la transaccion de negocio: el
 * movimiento ya quedo persistido en MongoDB, la notificacion es un
 * efecto secundario best-effort.
 */
@Slf4j
@Component
public class MovementEventProducer {

    private static final String TOPIC = "account-movements";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public MovementEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String accountId, String movementType, BigDecimal amount, BigDecimal balanceAfter) {
        try {
            MovementEvent event = new MovementEvent(accountId, movementType, amount, balanceAfter, LocalDateTime.now());
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, accountId, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("No se pudo publicar el evento de movimiento en Kafka: {}", ex.getMessage());
                        } else {
                            log.debug("Evento de movimiento publicado en Kafka para cuenta {}", accountId);
                        }
                    });
        } catch (Exception ex) {
            log.error("Error serializando el evento de movimiento", ex);
        }
    }

    /** DTO interno del evento, serializado como JSON plano en el mensaje de Kafka. */
    public record MovementEvent(String accountId, String type, BigDecimal amount,
                                 BigDecimal balanceAfter, LocalDateTime timestamp) {
    }
}
