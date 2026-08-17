package com.banco.accounts.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Proyeccion minima del cliente obtenida via REST desde ms-customers. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private String id;
    private String documentNumber;
    private CustomerType customerType;
    private String fullName;
    private String profile;
    private boolean active;
}
