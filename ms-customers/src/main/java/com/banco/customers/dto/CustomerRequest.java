package com.banco.customers.dto;

import com.banco.customers.model.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para crear o actualizar un cliente. Se separa del
 * modelo de persistencia para no exponer el documento de Mongo
 * directamente en la capa REST.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "El numero de documento es obligatorio")
    private String documentNumber;

    @NotNull(message = "El tipo de cliente es obligatorio")
    private CustomerType customerType;

    @NotBlank(message = "El nombre o razon social es obligatorio")
    private String fullName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    private String email;

    private String phoneNumber;

    private String address;
}
