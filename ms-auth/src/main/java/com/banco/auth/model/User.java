package com.banco.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Usuario de autenticacion. Para esta entrega se maneja una lista
 * en memoria (ver UserStore) en lugar de una coleccion Mongo, ya
 * que el foco de la Parte III es demostrar el flujo JWT de punta a
 * punta, no un modulo completo de gestion de usuarios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String passwordHash;
    private String customerId;
    private String role;
}
