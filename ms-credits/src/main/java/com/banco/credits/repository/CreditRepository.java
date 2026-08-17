package com.banco.credits.repository;

import com.banco.credits.model.Credit;
import com.banco.credits.model.CreditType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditRepository extends ReactiveMongoRepository<Credit, String> {
    Flux<Credit> findByCustomerId(String customerId);
    Flux<Credit> findByCustomerIdAndCreditType(String customerId, CreditType creditType);
    Mono<Boolean> existsByCustomerIdAndOverdueTrue(String customerId);
}
