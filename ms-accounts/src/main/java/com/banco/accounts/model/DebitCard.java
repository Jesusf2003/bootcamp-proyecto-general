package com.banco.accounts.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tarjeta de debito (Parte III). Se asocia a una cuenta principal y,
 * opcionalmente, a cuentas adicionales que se usan en cascada: si la
 * cuenta principal no tiene saldo suficiente, se intenta debitar de
 * la siguiente cuenta asociada, en el orden de la lista.
 */
@Document(collection = "debit_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCard {

    @Id
    private String id;

    private String customerId;

    private String cardNumber;

    private String primaryAccountId;

    @Builder.Default
    private List<String> associatedAccountIds = List.of();

    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;
}
