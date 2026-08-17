package com.banco.credits.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Producto activo: credito personal o empresarial. Un cliente
 * personal solo puede tener uno; una empresa puede tener varios
 * (regla validada en el servicio, no aqui).
 */
@Document(collection = "credits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credit {

    @Id
    private String id;

    private String customerId;

    private CreditType creditType;

    /** Monto total otorgado. */
    private BigDecimal amount;

    /** Saldo pendiente de pago (disminuye con cada pago). */
    private BigDecimal outstandingBalance;

    @Builder.Default
    private BigDecimal interestRate = BigDecimal.valueOf(0.15);

    @Builder.Default
    private boolean active = true;

    /** Deuda vencida (Parte III): marcado por un proceso batch o manualmente para pruebas. */
    @Builder.Default
    private boolean overdue = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
