package com.banco.accounts.dto;

import com.banco.accounts.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** DTO de entrada para aperturar una cuenta. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotBlank(message = "El id del cliente titular es obligatorio")
    private String customerId;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private AccountType accountType;

    @DecimalMin(value = "0.0", message = "El monto de apertura no puede ser negativo")
    @Builder.Default
    private BigDecimal openingAmount = BigDecimal.ZERO;

    /** Solo para cuentas empresariales: titulares y firmantes adicionales. */
    private List<String> additionalHolderIds;

    /** Perfil especial solicitado (Parte II): "VIP" (personal+ahorro) o "PYME" (empresarial+corriente). */
    private String requestedProfile;
}
