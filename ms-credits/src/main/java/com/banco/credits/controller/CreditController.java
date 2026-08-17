package com.banco.credits.controller;

import com.banco.credits.dto.CreditRequest;
import com.banco.credits.dto.CreditResponse;
import com.banco.credits.dto.PaymentRequest;
import com.banco.credits.service.CreditService;
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
@RequestMapping("/api/v1/credits")
@Tag(name = "Credits", description = "Gestion de creditos personales y empresariales")
public class CreditController {

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @Operation(summary = "Otorgar un nuevo credito")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreditResponse> create(@Valid @RequestBody CreditRequest request) {
        return creditService.create(request);
    }

    @Operation(summary = "Listar todos los creditos")
    @GetMapping
    public Flux<CreditResponse> findAll() {
        return creditService.findAll();
    }

    @Operation(summary = "Obtener un credito por id")
    @GetMapping("/{id}")
    public Mono<CreditResponse> findById(@PathVariable String id) {
        return creditService.findById(id);
    }

    @Operation(summary = "Eliminar un credito")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return creditService.delete(id);
    }

    @Operation(summary = "Pagar un credito (propio o de un tercero)")
    @PostMapping("/{id}/payments")
    public Mono<CreditResponse> pay(@PathVariable String id, @Valid @RequestBody PaymentRequest request) {
        log.info("POST /credits/{}/payments monto={}", id, request.getAmount());
        return creditService.pay(id, request);
    }

    @Operation(summary = "Marcar/desmarcar un credito como vencido (simula el proceso batch de mora)")
    @PatchMapping("/{id}/overdue")
    public Mono<CreditResponse> markOverdue(@PathVariable String id, @RequestParam boolean overdue) {
        return creditService.markOverdue(id, overdue);
    }
}
