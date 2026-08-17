package com.banco.customers.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String id) {
        super("No se encontro el cliente con id: " + id);
    }
}
