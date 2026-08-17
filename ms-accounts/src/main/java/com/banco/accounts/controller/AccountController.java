package com.banco.accounts.controller;

import com.banco.accounts.dto.AccountReportItem;
import com.banco.accounts.dto.AccountRequest;
import com.banco.accounts.dto.AccountResponse;
import com.banco.accounts.dto.MovementRequest;
import com.banco.accounts.dto.MovementResponse;
import com.banco.accounts.dto.TransferRequest;
import com.banco.accounts.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Controlador REST de cuentas bancarias. Cubre el CRUD basico mas
 * las operaciones de negocio: depositos, retiros, transferencias,
 * consulta de saldo/movimientos y reportes (Parte II).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Gestion de cuentas bancarias: ahorro, corriente y plazo fijo")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Aperturar una cuenta")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        log.info("POST /accounts - cliente={} tipo={}", request.getCustomerId(), request.getAccountType());
        return accountService.create(request);
    }

    @Operation(summary = "Listar todas las cuentas")
    @GetMapping
    public Flux<AccountResponse> findAll() {
        return accountService.findAll();
    }

    @Operation(summary = "Obtener una cuenta por id")
    @GetMapping("/{id}")
    public Mono<AccountResponse> findById(@PathVariable String id) {
        return accountService.findById(id);
    }

    @Operation(summary = "Eliminar (cerrar) una cuenta")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return accountService.delete(id);
    }

    @Operation(summary = "Consultar el saldo disponible")
    @GetMapping("/{id}/balance")
    public Mono<AccountResponse> getBalance(@PathVariable String id) {
        return accountService.getBalance(id);
    }

    @Operation(summary = "Depositar dinero en la cuenta")
    @PostMapping("/{id}/deposits")
    public Mono<MovementResponse> deposit(@PathVariable String id, @Valid @RequestBody MovementRequest request) {
        log.info("POST /accounts/{}/deposits monto={}", id, request.getAmount());
        return accountService.deposit(id, request);
    }

    @Operation(summary = "Retirar dinero de la cuenta")
    @PostMapping("/{id}/withdrawals")
    public Mono<MovementResponse> withdraw(@PathVariable String id, @Valid @RequestBody MovementRequest request) {
        log.info("POST /accounts/{}/withdrawals monto={}", id, request.getAmount());
        return accountService.withdraw(id, request);
    }

    @Operation(summary = "Transferir dinero a otra cuenta (propia o de tercero, mismo banco)")
    @PostMapping("/{id}/transfers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> transfer(@PathVariable String id, @Valid @RequestBody TransferRequest request) {
        log.info("POST /accounts/{}/transfers destino={} monto={}", id, request.getTargetAccountId(), request.getAmount());
        return accountService.transfer(id, request);
    }

    @Operation(summary = "Listar todos los movimientos de la cuenta")
    @GetMapping("/{id}/movements")
    public Flux<MovementResponse> getMovements(@PathVariable String id) {
        return accountService.getMovements(id);
    }

    @Operation(summary = "Ultimos 10 movimientos de la cuenta (usada como tarjeta de debito)")
    @GetMapping("/{id}/movements/last")
    public Flux<MovementResponse> getLastMovements(@PathVariable String id,
                                                     @RequestParam(defaultValue = "10") int limit) {
        return accountService.getLastMovements(id, limit);
    }

    @Operation(summary = "Reporte general de cuentas en un intervalo de tiempo")
    @GetMapping("/reports")
    public Flux<AccountReportItem> generateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("GET /accounts/reports from={} to={}", from, to);
        return accountService.generateReport(from, to);
    }
}
