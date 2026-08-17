package com.banco.yanki.service;

import com.banco.yanki.client.AccountClient;
import com.banco.yanki.dto.SendMoneyRequest;
import com.banco.yanki.dto.WalletRequest;
import com.banco.yanki.dto.WalletResponse;
import com.banco.yanki.exception.WalletAlreadyExistsException;
import com.banco.yanki.exception.WalletNotFoundException;
import com.banco.yanki.model.Wallet;
import com.banco.yanki.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementacion del servicio de monederos Yanki: registro de la
 * asociacion telefono-cuenta y envio de dinero entre dos numeros de
 * celular delegando el movimiento real a ms-accounts.
 */
@Slf4j
@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AccountClient accountClient;

    public WalletServiceImpl(WalletRepository walletRepository, AccountClient accountClient) {
        this.walletRepository = walletRepository;
        this.accountClient = accountClient;
    }

    @Override
    public Mono<WalletResponse> register(WalletRequest request) {
        return walletRepository.existsByPhoneNumber(request.getPhoneNumber())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new WalletAlreadyExistsException(request.getPhoneNumber()));
                    }
                    Wallet wallet = Wallet.builder()
                            .phoneNumber(request.getPhoneNumber())
                            .customerId(request.getCustomerId())
                            .documentNumber(request.getDocumentNumber())
                            .linkedAccountId(request.getLinkedAccountId())
                            .active(true)
                            .build();
                    return walletRepository.save(wallet);
                })
                .doOnNext(w -> log.info("Wallet Yanki registrado telefono={} cliente={}", w.getPhoneNumber(), w.getCustomerId()))
                .map(this::toResponse);
    }

    @Override
    public Mono<WalletResponse> findByPhoneNumber(String phoneNumber) {
        return walletRepository.findByPhoneNumber(phoneNumber)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(phoneNumber)))
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> sendMoney(SendMoneyRequest request) {
        return Mono.zip(
                        walletRepository.findByPhoneNumber(request.getFromPhoneNumber())
                                .switchIfEmpty(Mono.error(new WalletNotFoundException(request.getFromPhoneNumber()))),
                        walletRepository.findByPhoneNumber(request.getToPhoneNumber())
                                .switchIfEmpty(Mono.error(new WalletNotFoundException(request.getToPhoneNumber())))
                )
                .flatMap(tuple -> accountClient.transfer(
                        tuple.getT1().getLinkedAccountId(),
                        tuple.getT2().getLinkedAccountId(),
                        request.getAmount()))
                .doOnSuccess(v -> log.info("Envio Yanki de {} a {} por {}",
                        request.getFromPhoneNumber(), request.getToPhoneNumber(), request.getAmount()));
    }

    private WalletResponse toResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .phoneNumber(wallet.getPhoneNumber())
                .customerId(wallet.getCustomerId())
                .documentNumber(wallet.getDocumentNumber())
                .linkedAccountId(wallet.getLinkedAccountId())
                .active(wallet.isActive())
                .createdAt(wallet.getCreatedAt())
                .build();
    }
}
