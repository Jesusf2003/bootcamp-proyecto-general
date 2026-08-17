package com.banco.credits.service;

import com.banco.credits.dto.CardMovementResponse;
import com.banco.credits.dto.CardRequest;
import com.banco.credits.dto.CardResponse;
import com.banco.credits.dto.ConsumptionRequest;
import com.banco.credits.dto.PaymentRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CardService {

    Mono<CardResponse> create(CardRequest request);

    Flux<CardResponse> findAll();

    Mono<CardResponse> findById(String id);

    Mono<CardMovementResponse> consume(String cardId, ConsumptionRequest request);

    Mono<CardResponse> pay(String cardId, PaymentRequest request);

    Flux<CardMovementResponse> getLastMovements(String cardId, int limit);

    /** Usado por ms-accounts para validar el requisito VIP/PYME: "ya tener tarjeta de credito". */
    Mono<Boolean> existsForCustomer(String customerId);

    /** Marca o desmarca una tarjeta como vencida (Parte III). */
    Mono<CardResponse> markOverdue(String cardId, boolean overdue);
}
