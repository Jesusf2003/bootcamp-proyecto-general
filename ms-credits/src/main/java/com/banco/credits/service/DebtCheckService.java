package com.banco.credits.service;

import com.banco.credits.repository.CreditCardRepository;
import com.banco.credits.repository.CreditRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Verifica si un cliente tiene deuda vencida en cualquiera de sus
 * productos de credito (creditos o tarjetas). Regla de la Parte III:
 * "Un cliente no podra adquirir un producto si posee alguna deuda
 * vencida en algun producto de credito" -- se usa tanto internamente
 * en ms-credits (antes de otorgar un nuevo credito/tarjeta) como
 * desde ms-accounts (antes de aperturar una cuenta).
 */
@Service
public class DebtCheckService {

    private final CreditRepository creditRepository;
    private final CreditCardRepository cardRepository;

    public DebtCheckService(CreditRepository creditRepository, CreditCardRepository cardRepository) {
        this.creditRepository = creditRepository;
        this.cardRepository = cardRepository;
    }

    public Mono<Boolean> hasOverdueDebt(String customerId) {
        return Mono.zip(
                creditRepository.existsByCustomerIdAndOverdueTrue(customerId),
                cardRepository.existsByCustomerIdAndOverdueTrue(customerId),
                (creditsOverdue, cardsOverdue) -> creditsOverdue || cardsOverdue
        );
    }
}
