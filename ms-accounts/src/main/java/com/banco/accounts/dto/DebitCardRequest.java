package com.banco.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardRequest {
    @NotBlank(message = "El id del cliente es obligatorio")
    private String customerId;

    @NotBlank(message = "La cuenta principal es obligatoria")
    private String primaryAccountId;

    /** Cuentas adicionales, en orden de prioridad para la cascada de debito. */
    @Builder.Default
    private List<String> associatedAccountIds = List.of();
}
