package com.banco.credits.repository;

import com.banco.credits.model.CreditCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditCardRepository extends ReactiveMongoRepository<CreditCard, String> {
    Flux<CreditCard> findByCustomerId(String customerId);
    Mono<Boolean> existsByCustomerId(String customerId);
    Mono<Boolean> existsByCustomerIdAndOverdueTrue(String customerId);
}
