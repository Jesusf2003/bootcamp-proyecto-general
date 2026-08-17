package com.banco.credits.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Publica eventos de pagos y consumos de credito/tarjeta en Kafka
 * para que ms-notifications simule el envio de alertas al cliente
 * (Parte III: arquitectura orientada a eventos).
 */
@Slf4j
@Component
public class CreditEventProducer {

    private static final String TOPIC = "credit-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CreditEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String customerId, String productId, String eventType, BigDecimal amount) {
        try {
            CreditEvent event = new CreditEvent(customerId, productId, eventType, amount, LocalDateTime.now());
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, productId, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("No se pudo publicar el evento de credito en Kafka: {}", ex.getMessage());
                        }
                    });
        } catch (Exception ex) {
            log.error("Error serializando el evento de credito", ex);
        }
    }

    public record CreditEvent(String customerId, String productId, String type,
                               BigDecimal amount, LocalDateTime timestamp) {
    }
}
