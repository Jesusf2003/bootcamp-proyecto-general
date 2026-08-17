package com.banco.yanki.service;

import com.banco.yanki.dto.SendMoneyRequest;
import com.banco.yanki.dto.WalletRequest;
import com.banco.yanki.dto.WalletResponse;
import reactor.core.publisher.Mono;

public interface WalletService {

    Mono<WalletResponse> register(WalletRequest request);

    Mono<WalletResponse> findByPhoneNumber(String phoneNumber);

    Mono<Void> sendMoney(SendMoneyRequest request);
}
