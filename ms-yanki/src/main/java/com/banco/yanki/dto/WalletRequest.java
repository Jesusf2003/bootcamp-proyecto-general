package com.banco.yanki.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {

    @NotBlank(message = "El numero de celular es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El numero de celular debe tener 9 digitos")
    private String phoneNumber;

    @NotBlank(message = "El id del cliente es obligatorio")
    private String customerId;

    @NotBlank(message = "El documento del cliente es obligatorio")
    private String documentNumber;

    @NotBlank(message = "La cuenta de debito a vincular es obligatoria")
    private String linkedAccountId;
}
