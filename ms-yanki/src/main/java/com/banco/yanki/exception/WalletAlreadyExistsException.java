package com.banco.yanki.exception;

public class WalletAlreadyExistsException extends RuntimeException {
    public WalletAlreadyExistsException(String phoneNumber) {
        super("El numero " + phoneNumber + " ya tiene un monedero Yanki registrado");
    }
}
