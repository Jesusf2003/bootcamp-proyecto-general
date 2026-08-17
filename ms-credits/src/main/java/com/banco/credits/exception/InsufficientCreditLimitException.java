package com.banco.credits.exception;

public class InsufficientCreditLimitException extends RuntimeException {
    public InsufficientCreditLimitException(String cardId) {
        super("La tarjeta " + cardId + " no tiene linea de credito disponible suficiente para este consumo");
    }
}
