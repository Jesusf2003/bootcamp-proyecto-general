package com.banco.credits.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Historial de consumos y pagos de una tarjeta de credito. */
@Document(collection = "card_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardMovement {

    @Id
    private String id;

    private String cardId;

    private CardMovementType type;

    private BigDecimal amount;

    private BigDecimal availableLimitAfter;

    private String description;

    @CreatedDate
    private LocalDateTime date;
}
