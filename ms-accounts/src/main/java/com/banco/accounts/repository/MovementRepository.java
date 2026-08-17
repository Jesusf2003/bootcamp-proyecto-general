package com.banco.accounts.repository;

import com.banco.accounts.model.Movement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface MovementRepository extends ReactiveMongoRepository<Movement, String> {

    Flux<Movement> findByAccountId(String accountId);

    Flux<Movement> findByAccountIdOrderByDateDesc(String accountId);

    Flux<Movement> findByAccountIdAndDateBetween(String accountId, LocalDateTime from, LocalDateTime to);
}
