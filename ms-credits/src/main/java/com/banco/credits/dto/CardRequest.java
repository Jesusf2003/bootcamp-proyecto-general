package com.banco.credits.dto;

import com.banco.credits.model.CreditType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest {
    @NotBlank(message = "El id del cliente es obligatorio")
    private String customerId;

    @NotNull(message = "El tipo de tarjeta es obligatorio")
    private CreditType cardType;

    @NotNull(message = "La linea de credito es obligatoria")
    @DecimalMin(value = "0.01", message = "La linea de credito debe ser mayor a cero")
    private BigDecimal creditLimit;
}
