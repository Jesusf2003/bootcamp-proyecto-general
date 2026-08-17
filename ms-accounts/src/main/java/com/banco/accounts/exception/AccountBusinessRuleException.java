package com.banco.accounts.exception;

/** Se lanza cuando se viola una regla de negocio: limites de cuentas por cliente,
 * dia invalido para plazo fijo, requisito de tarjeta de credito para VIP/PYME, etc. */
public class AccountBusinessRuleException extends RuntimeException {
    public AccountBusinessRuleException(String message) {
        super(message);
    }
}
