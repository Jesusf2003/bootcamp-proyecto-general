package com.banco.credits.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String id) {
        super("No se encontro la tarjeta con id: " + id);
    }
}
