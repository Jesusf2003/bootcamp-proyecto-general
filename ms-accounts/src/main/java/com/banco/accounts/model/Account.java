package com.banco.accounts.model;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de negocio Cuenta bancaria (producto pasivo). Cubre
 * ahorro, corriente y plazo fijo con sus reglas particulares:
 * comisiones, limites de movimientos y titularidad.
 *
 * Solo ms-accounts accede a esta coleccion (database per service).
 */
@Document(collection = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private String id;

    private String accountNumber;

    /** Id del cliente principal en ms-customers. */
    private String customerId;

    private AccountType accountType;

    /** Perfil comercial vigente al momento de apertura (Parte II: STANDARD, VIP, PYME). */
    @Builder.Default
    private String customerProfile = "STANDARD";

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /** Monto minimo de apertura (Parte II: puede ser cero). */
    @Builder.Default
    private BigDecimal openingAmount = BigDecimal.ZERO;

    /** Comision de mantenimiento mensual (aplica solo a CHECKING sin perfil PYME). */
    @Builder.Default
    private BigDecimal maintenanceFee = BigDecimal.ZERO;

    /** Numero maximo de movimientos mensuales libres de comision. */
    @Builder.Default
    private Integer maxFreeMonthlyMovements = Integer.MAX_VALUE;

    /** Comision cobrada por cada movimiento que exceda el limite mensual. */
    @Builder.Default
    private BigDecimal movementCommission = BigDecimal.ZERO;

    /** Solo aplica a PLAZO FIJO: dia del mes (1-28) permitido para el unico movimiento. */
    private Integer fixedTermAllowedDay;

    /** Titulares y firmantes (solo cuentas empresariales). */
    @Builder.Default
    private List<Holder> holders = new ArrayList<>();

    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
