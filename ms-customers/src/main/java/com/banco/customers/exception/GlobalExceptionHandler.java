package com.banco.customers.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador centralizado de excepciones para toda la capa REST.
 * Traduce las excepciones de negocio en respuestas HTTP consistentes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNotFound(CustomerNotFoundException ex) {
        log.warn("Cliente no encontrado: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildBody(ex.getMessage(), HttpStatus.NOT_FOUND)));
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleDuplicate(DuplicateDocumentException ex) {
        log.warn("Documento duplicado: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(buildBody(ex.getMessage(), HttpStatus.CONFLICT)));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidation(WebExchangeBindException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validacion");
        log.warn("Error de validacion: {}", errors);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildBody(errors, HttpStatus.BAD_REQUEST)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneric(Exception ex) {
        log.error("Error inesperado", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody("Ocurrio un error inesperado", HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    private Map<String, Object> buildBody(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
