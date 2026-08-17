package com.banco.customers.model;

/**
 * Perfil especial del cliente, introducido en la Parte II.
 * STANDARD es el perfil regular de la Parte I; VIP aplica a clientes
 * personales con cuenta de ahorro de alto movimiento, PYME aplica a
 * clientes empresariales con cuenta corriente sin comision.
 */
public enum CustomerProfile {
    STANDARD,
    VIP,
    PYME
}
