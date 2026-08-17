package com.banco.yanki.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String phoneNumber) {
        super("No se encontro un monedero Yanki registrado para el numero: " + phoneNumber);
    }
}
