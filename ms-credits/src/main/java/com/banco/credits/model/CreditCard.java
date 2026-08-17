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

/** Tarjeta de credito personal o empresarial, con linea de credito y saldo disponible. */
@Document(collection = "cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {

    @Id
    private String id;

    private String customerId;

    private CreditType cardType;

    private String cardNumber;

    private BigDecimal creditLimit;

    private BigDecimal availableLimit;

    @Builder.Default
    private boolean active = true;

    /** Deuda vencida (Parte III). */
    @Builder.Default
    private boolean overdue = false;

    @CreatedDate
    private LocalDateTime createdAt;
}
