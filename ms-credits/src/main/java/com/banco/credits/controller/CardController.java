package com.banco.credits.controller;

import com.banco.credits.dto.CardMovementResponse;
import com.banco.credits.dto.CardRequest;
import com.banco.credits.dto.CardResponse;
import com.banco.credits.dto.ConsumptionRequest;
import com.banco.credits.dto.PaymentRequest;
import com.banco.credits.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Cards", description = "Gestion de tarjetas de credito")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Emitir una nueva tarjeta de credito")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CardResponse> create(@Valid @RequestBody CardRequest request) {
        return cardService.create(request);
    }

    @Operation(summary = "Listar todas las tarjetas")
    @GetMapping
    public Flux<CardResponse> findAll() {
        return cardService.findAll();
    }

    @Operation(summary = "Obtener una tarjeta por id")
    @GetMapping("/{id}")
    public Mono<CardResponse> findById(@PathVariable String id) {
        return cardService.findById(id);
    }

    @Operation(summary = "Registrar un consumo en la tarjeta")
    @PostMapping("/{id}/consumptions")
    public Mono<CardMovementResponse> consume(@PathVariable String id, @Valid @RequestBody ConsumptionRequest request) {
        log.info("POST /cards/{}/consumptions monto={}", id, request.getAmount());
        return cardService.consume(id, request);
    }

    @Operation(summary = "Pagar la tarjeta de credito")
    @PostMapping("/{id}/payments")
    public Mono<CardResponse> pay(@PathVariable String id, @Valid @RequestBody PaymentRequest request) {
        return cardService.pay(id, request);
    }

    @Operation(summary = "Ultimos 10 movimientos de la tarjeta de credito")
    @GetMapping("/{id}/movements/last")
    public Flux<CardMovementResponse> getLastMovements(@PathVariable String id,
                                                          @RequestParam(defaultValue = "10") int limit) {
        return cardService.getLastMovements(id, limit);
    }

    @Operation(summary = "Verifica si un cliente ya tiene una tarjeta de credito (usado por ms-accounts)")
    @GetMapping("/customer/{customerId}/exists")
    public Mono<Boolean> existsForCustomer(@PathVariable String customerId) {
        return cardService.existsForCustomer(customerId);
    }

    @Operation(summary = "Marcar/desmarcar una tarjeta como vencida (simula el proceso batch de mora)")
    @PatchMapping("/{id}/overdue")
    public Mono<CardResponse> markOverdue(@PathVariable String id, @RequestParam boolean overdue) {
        return cardService.markOverdue(id, overdue);
    }
}
