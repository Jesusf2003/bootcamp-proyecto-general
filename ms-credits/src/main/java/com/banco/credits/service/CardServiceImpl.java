package com.banco.credits.service;

import com.banco.credits.client.CustomerClient;
import com.banco.credits.dto.CardMovementResponse;
import com.banco.credits.dto.CardRequest;
import com.banco.credits.dto.CardResponse;
import com.banco.credits.dto.ConsumptionRequest;
import com.banco.credits.dto.PaymentRequest;
import com.banco.credits.exception.CardNotFoundException;
import com.banco.credits.exception.InsufficientCreditLimitException;
import com.banco.credits.kafka.CreditEventProducer;
import com.banco.credits.model.CardMovement;
import com.banco.credits.model.CardMovementType;
import com.banco.credits.model.CreditCard;
import com.banco.credits.repository.CardMovementRepository;
import com.banco.credits.repository.CreditCardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementacion del servicio de tarjetas de credito: apertura,
 * consumos limitados por la linea de credito disponible, pagos y
 * consulta de los ultimos movimientos.
 */
@Slf4j
@Service
public class CardServiceImpl implements CardService {

    private final CreditCardRepository cardRepository;
    private final CardMovementRepository movementRepository;
    private final CreditMapper mapper;
    private final CustomerClient customerClient;
    private final CreditEventProducer eventProducer;
    private final DebtCheckService debtCheckService;

    public CardServiceImpl(CreditCardRepository cardRepository, CardMovementRepository movementRepository,
                            CreditMapper mapper, CustomerClient customerClient, CreditEventProducer eventProducer,
                            DebtCheckService debtCheckService) {
        this.cardRepository = cardRepository;
        this.movementRepository = movementRepository;
        this.mapper = mapper;
        this.customerClient = customerClient;
        this.eventProducer = eventProducer;
        this.debtCheckService = debtCheckService;
    }

    @Override
    public Mono<CardResponse> create(CardRequest request) {
        return debtCheckService.hasOverdueDebt(request.getCustomerId())
                .flatMap(overdue -> {
                    if (Boolean.TRUE.equals(overdue)) {
                        return Mono.error(new com.banco.credits.exception.CreditBusinessRuleException(
                                "El cliente tiene deuda vencida en un producto de credito; no puede adquirir un nuevo producto"));
                    }
                    return customerClient.findById(request.getCustomerId());
                })
                .flatMap(customer -> {
                    CreditCard card = CreditCard.builder()
                            .customerId(request.getCustomerId())
                            .cardType(request.getCardType())
                            .cardNumber(generateCardNumber())
                            .creditLimit(request.getCreditLimit())
                            .availableLimit(request.getCreditLimit())
                            .active(true)
                            .build();
                    return cardRepository.save(card);
                })
                .doOnNext(c -> log.info("Tarjeta creada numero={} cliente={}", c.getCardNumber(), c.getCustomerId()))
                .map(mapper::toResponse);
    }

    @Override
    public Flux<CardResponse> findAll() {
        return cardRepository.findAll().map(mapper::toResponse);
    }

    @Override
    public Mono<CardResponse> findById(String id) {
        return cardRepository.findById(id)
                .switchIfEmpty(Mono.error(new CardNotFoundException(id)))
                .map(mapper::toResponse);
    }

    @Override
    public Mono<CardMovementResponse> consume(String cardId, ConsumptionRequest request) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException(cardId)))
                .flatMap(card -> {
                    if (card.getAvailableLimit().compareTo(request.getAmount()) < 0) {
                        return Mono.error(new InsufficientCreditLimitException(cardId));
                    }
                    card.setAvailableLimit(card.getAvailableLimit().subtract(request.getAmount()));
                    return cardRepository.save(card)
                            .flatMap(saved -> saveMovement(saved, CardMovementType.CONSUMPTION,
                                    request.getAmount(), request.getDescription()));
                })
                .doOnNext(m -> {
                    log.info("Consumo registrado en tarjeta {} monto={}", cardId, request.getAmount());
                    eventProducer.publish(null, cardId, "CARD_CONSUMPTION", request.getAmount());
                })
                .map(mapper::toResponse);
    }

    @Override
    public Mono<CardResponse> pay(String cardId, PaymentRequest request) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException(cardId)))
                .flatMap(card -> {
                    BigDecimal newAvailable = card.getAvailableLimit().add(request.getAmount());
                    // No se puede exceder la linea de credito original al pagar de mas.
                    card.setAvailableLimit(newAvailable.compareTo(card.getCreditLimit()) > 0
                            ? card.getCreditLimit() : newAvailable);
                    return cardRepository.save(card)
                            .flatMap(saved -> saveMovement(saved, CardMovementType.PAYMENT,
                                    request.getAmount(), "Pago de tarjeta")
                                    .thenReturn(saved));
                })
                .doOnNext(c -> log.info("Pago aplicado a tarjeta {} monto={} pagador={}",
                        cardId, request.getAmount(), request.getPayerCustomerId()))
                .map(mapper::toResponse);
    }

    private Mono<CardMovement> saveMovement(CreditCard card, CardMovementType type, BigDecimal amount, String description) {
        CardMovement movement = CardMovement.builder()
                .cardId(card.getId())
                .type(type)
                .amount(amount)
                .availableLimitAfter(card.getAvailableLimit())
                .description(description)
                .build();
        return movementRepository.save(movement);
    }

    @Override
    public Flux<CardMovementResponse> getLastMovements(String cardId, int limit) {
        return movementRepository.findByCardIdOrderByDateDesc(cardId).take(limit).map(mapper::toResponse);
    }

    @Override
    public Mono<Boolean> existsForCustomer(String customerId) {
        return cardRepository.existsByCustomerId(customerId);
    }

    @Override
    public Mono<CardResponse> markOverdue(String cardId, boolean overdue) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException(cardId)))
                .flatMap(card -> {
                    card.setOverdue(overdue);
                    return cardRepository.save(card);
                })
                .doOnNext(c -> log.info("Tarjeta {} marcada overdue={}", cardId, overdue))
                .map(mapper::toResponse);
    }

    private String generateCardNumber() {
        return "CC-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase().replace("-", "");
    }
}
