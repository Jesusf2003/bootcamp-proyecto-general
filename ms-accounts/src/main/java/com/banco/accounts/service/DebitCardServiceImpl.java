package com.banco.accounts.service;

import com.banco.accounts.dto.DebitCardPaymentRequest;
import com.banco.accounts.dto.DebitCardRequest;
import com.banco.accounts.dto.DebitCardResponse;
import com.banco.accounts.dto.MovementResponse;
import com.banco.accounts.exception.AccountNotFoundException;
import com.banco.accounts.model.DebitCard;
import com.banco.accounts.repository.AccountRepository;
import com.banco.accounts.repository.DebitCardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DebitCardServiceImpl implements DebitCardService {

    private final DebitCardRepository debitCardRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public DebitCardServiceImpl(DebitCardRepository debitCardRepository, AccountRepository accountRepository,
                                 AccountService accountService) {
        this.debitCardRepository = debitCardRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Override
    public Mono<DebitCardResponse> create(DebitCardRequest request) {
        return accountRepository.findById(request.getPrimaryAccountId())
                .switchIfEmpty(Mono.error(new AccountNotFoundException(request.getPrimaryAccountId())))
                .flatMap(account -> {
                    DebitCard card = DebitCard.builder()
                            .customerId(request.getCustomerId())
                            .cardNumber(generateCardNumber())
                            .primaryAccountId(request.getPrimaryAccountId())
                            .associatedAccountIds(request.getAssociatedAccountIds())
                            .active(true)
                            .build();
                    return debitCardRepository.save(card);
                })
                .doOnNext(c -> log.info("Tarjeta de debito creada numero={} cliente={}", c.getCardNumber(), c.getCustomerId()))
                .map(this::toResponse);
    }

    @Override
    public Flux<DebitCardResponse> findAll() {
        return debitCardRepository.findAll().map(this::toResponse);
    }

    @Override
    public Mono<DebitCardResponse> findById(String id) {
        return debitCardRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(id)))
                .map(this::toResponse);
    }

    @Override
    public Mono<MovementResponse> pay(String cardId, DebitCardPaymentRequest request) {
        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(cardId)))
                .flatMap(card -> {
                    List<String> orderedAccounts = new ArrayList<>();
                    orderedAccounts.add(card.getPrimaryAccountId());
                    orderedAccounts.addAll(card.getAssociatedAccountIds());
                    return accountService.debitCascade(orderedAccounts, request.getAmount(), request.getDescription());
                });
    }

    private DebitCardResponse toResponse(DebitCard card) {
        return DebitCardResponse.builder()
                .id(card.getId())
                .customerId(card.getCustomerId())
                .cardNumber(card.getCardNumber())
                .primaryAccountId(card.getPrimaryAccountId())
                .associatedAccountIds(card.getAssociatedAccountIds())
                .active(card.isActive())
                .createdAt(card.getCreatedAt())
                .build();
    }

    private String generateCardNumber() {
        return "DC-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase().replace("-", "");
    }
}
