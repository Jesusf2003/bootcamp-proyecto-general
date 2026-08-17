package com.banco.accounts.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Movimiento (deposito, retiro o transferencia) realizado sobre una
 * cuenta. Se guarda como historial inmutable para poder generar
 * reportes y consultar los ultimos movimientos.
 */
@Document(collection = "movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movement {

    @Id
    private String id;

    private String accountId;

    private MovementType type;

    private BigDecimal amount;

    private BigDecimal commissionCharged;

    private BigDecimal balanceAfter;

    private String description;

    /** Cuando el movimiento es parte de una transferencia, referencia a la cuenta contraparte. */
    private String counterpartAccountId;

    @CreatedDate
    private LocalDateTime date;
}
