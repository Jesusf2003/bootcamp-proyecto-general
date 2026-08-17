package com.banco.credits.exception;

public class CreditNotFoundException extends RuntimeException {
    public CreditNotFoundException(String id) {
        super("No se encontro el credito con id: " + id);
    }
}
