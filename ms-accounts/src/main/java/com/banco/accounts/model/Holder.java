package com.banco.accounts.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Titular o firmante autorizado de una cuenta empresarial.
 * Las cuentas empresariales pueden tener uno o mas titulares y
 * cero o mas firmantes autorizados (requerimiento de la Parte I).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holder {
    private String customerId;
    private String fullName;
    private boolean authorizedSigner;
}
