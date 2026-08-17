package com.banco.accounts.service;

import com.banco.accounts.dto.AccountReportItem;
import com.banco.accounts.dto.AccountRequest;
import com.banco.accounts.dto.AccountResponse;
import com.banco.accounts.dto.MovementRequest;
import com.banco.accounts.dto.MovementResponse;
import com.banco.accounts.dto.TransferRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface AccountService {

    Mono<AccountResponse> create(AccountRequest request);

    Flux<AccountResponse> findAll();

    Mono<AccountResponse> findById(String id);

    Mono<Void> delete(String id);

    Mono<MovementResponse> deposit(String accountId, MovementRequest request);

    Mono<MovementResponse> withdraw(String accountId, MovementRequest request);

    Mono<Void> transfer(String sourceAccountId, TransferRequest request);

    Mono<AccountResponse> getBalance(String accountId);

    Flux<MovementResponse> getMovements(String accountId);

    /** Reporte general por producto en un intervalo de tiempo, usando Streams (Parte II). */
    Flux<AccountReportItem> generateReport(LocalDateTime from, LocalDateTime to);

    /** Ultimos 10 movimientos de una cuenta (para debito, reutilizado tambien por tarjeta en ms-credits). */
    Flux<MovementResponse> getLastMovements(String accountId, int limit);

    /**
     * Debito en cascada (Parte III, tarjeta de debito): intenta debitar
     * de la primera cuenta de la lista; si no tiene fondos suficientes,
     * pasa a la siguiente, en orden.
     */
    Mono<MovementResponse> debitCascade(java.util.List<String> orderedAccountIds, java.math.BigDecimal amount, String description);
}
