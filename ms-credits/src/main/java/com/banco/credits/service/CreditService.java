package com.banco.credits.service;

import com.banco.credits.dto.CreditRequest;
import com.banco.credits.dto.CreditResponse;
import com.banco.credits.dto.PaymentRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditService {

    Mono<CreditResponse> create(CreditRequest request);

    Flux<CreditResponse> findAll();

    Mono<CreditResponse> findById(String id);

    Mono<Void> delete(String id);

    /** Pago de un credito propio o de un tercero (el enunciado permite pagar deuda de terceros). */
    Mono<CreditResponse> pay(String creditId, PaymentRequest request);

    /** Marca o desmarca un credito como vencido (Parte III). */
    Mono<CreditResponse> markOverdue(String creditId, boolean overdue);
}
