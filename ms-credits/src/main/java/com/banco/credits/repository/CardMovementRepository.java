package com.banco.credits.repository;

import com.banco.credits.model.CardMovement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CardMovementRepository extends ReactiveMongoRepository<CardMovement, String> {
    Flux<CardMovement> findByCardIdOrderByDateDesc(String cardId);
}
