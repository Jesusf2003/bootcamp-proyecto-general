package com.banco.yanki.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Monedero movil Yanki: vincula un numero de celular con un cliente
 * y su cuenta de debito (el dinero enviado/recibido se mueve
 * directamente en esa cuenta via ms-accounts; el wallet no guarda
 * saldo propio, solo la asociacion telefono <-> cuenta).
 */
@Document(collection = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    private String id;

    private String phoneNumber;

    private String customerId;

    private String documentNumber;

    private String linkedAccountId;

    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;
}
