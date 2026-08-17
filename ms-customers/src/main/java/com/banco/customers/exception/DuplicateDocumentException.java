package com.banco.customers.exception;

public class DuplicateDocumentException extends RuntimeException {
    public DuplicateDocumentException(String documentNumber) {
        super("Ya existe un cliente registrado con el documento: " + documentNumber);
    }
}
