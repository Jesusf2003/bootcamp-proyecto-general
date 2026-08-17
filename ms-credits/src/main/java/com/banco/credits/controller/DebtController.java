package com.banco.credits.controller;

import com.banco.credits.service.DebtCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/credits/customer")
@Tag(name = "Debt", description = "Verificacion de deuda vencida (Parte III)")
public class DebtController {

    private final DebtCheckService debtCheckService;

    public DebtController(DebtCheckService debtCheckService) {
        this.debtCheckService = debtCheckService;
    }

    @Operation(summary = "Indica si el cliente tiene deuda vencida en algun producto de credito")
    @GetMapping("/{customerId}/overdue-debt")
    public Mono<Boolean> hasOverdueDebt(@PathVariable String customerId) {
        return debtCheckService.hasOverdueDebt(customerId);
    }
}
