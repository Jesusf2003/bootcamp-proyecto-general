package com.banco.accounts.service;

import com.banco.accounts.dto.DebitCardPaymentRequest;
import com.banco.accounts.dto.DebitCardRequest;
import com.banco.accounts.dto.DebitCardResponse;
import com.banco.accounts.dto.MovementResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DebitCardService {

    Mono<DebitCardResponse> create(DebitCardRequest request);

    Flux<DebitCardResponse> findAll();

    Mono<DebitCardResponse> findById(String id);

    /** Paga con la tarjeta: intenta debitar de la cuenta principal, en cascada hacia las asociadas. */
    Mono<MovementResponse> pay(String cardId, DebitCardPaymentRequest request);
}
