package com.banco.yanki;

import com.banco.yanki.client.AccountClient;
import com.banco.yanki.dto.SendMoneyRequest;
import com.banco.yanki.dto.WalletRequest;
import com.banco.yanki.model.Wallet;
import com.banco.yanki.repository.WalletRepository;
import com.banco.yanki.service.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class WalletServiceImplTest {

    private WalletRepository walletRepository;
    private AccountClient accountClient;
    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        walletRepository = Mockito.mock(WalletRepository.class);
        accountClient = Mockito.mock(AccountClient.class);
        walletService = new WalletServiceImpl(walletRepository, accountClient);
    }

    @Test
    void shouldRegisterWalletWhenPhoneNotYetRegistered() {
        WalletRequest request = WalletRequest.builder()
                .phoneNumber("987654321")
                .customerId("c1")
                .documentNumber("12345678")
                .linkedAccountId("acc1")
                .build();

        Wallet saved = Wallet.builder().id("w1").phoneNumber("987654321").customerId("c1")
                .linkedAccountId("acc1").build();

        when(walletRepository.existsByPhoneNumber("987654321")).thenReturn(Mono.just(false));
        when(walletRepository.save(any(Wallet.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(walletService.register(request))
                .expectNextMatches(response -> response.getId().equals("w1"))
                .verifyComplete();
    }

    @Test
    void shouldRejectDuplicatePhoneRegistration() {
        WalletRequest request = WalletRequest.builder()
                .phoneNumber("987654321")
                .customerId("c1")
                .documentNumber("12345678")
                .linkedAccountId("acc1")
                .build();

        when(walletRepository.existsByPhoneNumber("987654321")).thenReturn(Mono.just(true));

        StepVerifier.create(walletService.register(request))
                .expectError(com.banco.yanki.exception.WalletAlreadyExistsException.class)
                .verify();
    }

    @Test
    void shouldSendMoneyBetweenTwoRegisteredWallets() {
        Wallet from = Wallet.builder().id("w1").phoneNumber("111111111").linkedAccountId("accFrom").build();
        Wallet to = Wallet.builder().id("w2").phoneNumber("222222222").linkedAccountId("accTo").build();

        when(walletRepository.findByPhoneNumber("111111111")).thenReturn(Mono.just(from));
        when(walletRepository.findByPhoneNumber("222222222")).thenReturn(Mono.just(to));
        when(accountClient.transfer(anyString(), anyString(), any(BigDecimal.class))).thenReturn(Mono.empty());

        SendMoneyRequest request = SendMoneyRequest.builder()
                .fromPhoneNumber("111111111")
                .toPhoneNumber("222222222")
                .amount(BigDecimal.valueOf(50))
                .build();

        StepVerifier.create(walletService.sendMoney(request)).verifyComplete();
    }
}
