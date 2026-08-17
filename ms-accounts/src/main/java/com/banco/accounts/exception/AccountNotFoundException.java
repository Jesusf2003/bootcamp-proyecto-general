package com.banco.accounts.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String id) {
        super("No se encontro la cuenta con id: " + id);
    }
}
