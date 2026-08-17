package com.banco.accounts.controller;

import com.banco.accounts.dto.DebitCardPaymentRequest;
import com.banco.accounts.dto.DebitCardRequest;
import com.banco.accounts.dto.DebitCardResponse;
import com.banco.accounts.dto.MovementResponse;
import com.banco.accounts.service.DebitCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Tarjeta de debito (Parte III): se asocia a una cuenta principal y,
 * opcionalmente, a cuentas adicionales usadas en cascada al pagar.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/debit-cards")
@Tag(name = "DebitCards", description = "Tarjetas de debito asociadas a cuentas")
public class DebitCardController {

    private final DebitCardService debitCardService;

    public DebitCardController(DebitCardService debitCardService) {
        this.debitCardService = debitCardService;
    }

    @Operation(summary = "Emitir una tarjeta de debito")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DebitCardResponse> create(@Valid @RequestBody DebitCardRequest request) {
        return debitCardService.create(request);
    }

    @Operation(summary = "Listar tarjetas de debito")
    @GetMapping
    public Flux<DebitCardResponse> findAll() {
        return debitCardService.findAll();
    }

    @Operation(summary = "Obtener una tarjeta de debito por id")
    @GetMapping("/{id}")
    public Mono<DebitCardResponse> findById(@PathVariable String id) {
        return debitCardService.findById(id);
    }

    @Operation(summary = "Pagar con la tarjeta (debito en cascada: principal -> asociadas)")
    @PostMapping("/{id}/payments")
    public Mono<MovementResponse> pay(@PathVariable String id, @Valid @RequestBody DebitCardPaymentRequest request) {
        log.info("POST /debit-cards/{}/payments monto={}", id, request.getAmount());
        return debitCardService.pay(id, request);
    }
}
