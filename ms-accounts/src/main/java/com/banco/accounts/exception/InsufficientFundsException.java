package com.banco.accounts.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountId) {
        super("Fondos insuficientes en la cuenta: " + accountId);
    }
}
