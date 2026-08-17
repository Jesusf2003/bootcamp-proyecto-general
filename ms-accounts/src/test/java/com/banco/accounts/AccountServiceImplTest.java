package com.banco.accounts;

import com.banco.accounts.client.CreditClient;
import com.banco.accounts.client.CustomerClient;
import com.banco.accounts.client.CustomerDto;
import com.banco.accounts.client.CustomerType;
import com.banco.accounts.dto.AccountRequest;
import com.banco.accounts.kafka.MovementEventProducer;
import com.banco.accounts.model.Account;
import com.banco.accounts.model.AccountType;
import com.banco.accounts.pattern.AccountRuleFactory;
import com.banco.accounts.pattern.CheckingAccountRule;
import com.banco.accounts.pattern.FixedTermAccountRule;
import com.banco.accounts.pattern.SavingsAccountRule;
import com.banco.accounts.repository.AccountRepository;
import com.banco.accounts.repository.MovementRepository;
import com.banco.accounts.service.AccountMapper;
import com.banco.accounts.service.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de cuentas. Usa mocks para
 * repositorios y clientes REST, validando las reglas de negocio
 * mediante StepVerifier sobre los flujos reactivos.
 */
class AccountServiceImplTest {

    private AccountRepository accountRepository;
    private MovementRepository movementRepository;
    private CustomerClient customerClient;
    private CreditClient creditClient;
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountRepository = Mockito.mock(AccountRepository.class);
        movementRepository = Mockito.mock(MovementRepository.class);
        customerClient = Mockito.mock(CustomerClient.class);
        creditClient = Mockito.mock(CreditClient.class);
        MovementEventProducer movementEventProducer = Mockito.mock(MovementEventProducer.class);

        AccountRuleFactory ruleFactory = new AccountRuleFactory(
                new SavingsAccountRule(), new CheckingAccountRule(), new FixedTermAccountRule());

        accountService = new AccountServiceImpl(accountRepository, movementRepository,
                new AccountMapper(), ruleFactory, customerClient, creditClient, movementEventProducer);
    }

    @Test
    void shouldCreateSavingsAccountForPersonalCustomerWithoutExistingOne() {
        CustomerDto customer = new CustomerDto("c1", "12345678", CustomerType.PERSONAL, "Ana Lopez", "STANDARD", true);
        AccountRequest request = AccountRequest.builder()
                .customerId("c1")
                .accountType(AccountType.SAVINGS)
                .openingAmount(BigDecimal.valueOf(100))
                .build();

        Account saved = Account.builder().id("a1").customerId("c1").accountType(AccountType.SAVINGS)
                .balance(BigDecimal.valueOf(100)).build();

        when(customerClient.findById("c1")).thenReturn(Mono.just(customer));
        when(creditClient.hasOverdueDebt("c1")).thenReturn(Mono.just(false));
        when(accountRepository.findByCustomerIdAndAccountType("c1", AccountType.SAVINGS)).thenReturn(Flux.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(accountService.create(request))
                .expectNextMatches(response -> response.getId().equals("a1"))
                .verifyComplete();
    }

    @Test
    void shouldRejectSecondSavingsAccountForSamePersonalCustomer() {
        CustomerDto customer = new CustomerDto("c1", "12345678", CustomerType.PERSONAL, "Ana Lopez", "STANDARD", true);
        AccountRequest request = AccountRequest.builder()
                .customerId("c1")
                .accountType(AccountType.SAVINGS)
                .openingAmount(BigDecimal.ZERO)
                .build();

        Account existing = Account.builder().id("existing").customerId("c1").accountType(AccountType.SAVINGS).build();

        when(customerClient.findById("c1")).thenReturn(Mono.just(customer));
        when(creditClient.hasOverdueDebt("c1")).thenReturn(Mono.just(false));
        when(accountRepository.findByCustomerIdAndAccountType("c1", AccountType.SAVINGS))
                .thenReturn(Flux.just(existing));

        StepVerifier.create(accountService.create(request))
                .expectError(com.banco.accounts.exception.AccountBusinessRuleException.class)
                .verify();
    }

    @Test
    void shouldRejectWithdrawalWhenInsufficientFunds() {
        Account account = Account.builder().id("a1").accountType(AccountType.SAVINGS)
                .balance(BigDecimal.valueOf(10))
                .maxFreeMonthlyMovements(5)
                .movementCommission(BigDecimal.ZERO)
                .build();

        when(accountRepository.findById("a1")).thenReturn(Mono.just(account));
        when(movementRepository.findByAccountIdAndDateBetween(anyString(), any(), any())).thenReturn(Flux.empty());

        com.banco.accounts.dto.MovementRequest request = com.banco.accounts.dto.MovementRequest.builder()
                .amount(BigDecimal.valueOf(50))
                .build();

        StepVerifier.create(accountService.withdraw("a1", request))
                .expectError(com.banco.accounts.exception.InsufficientFundsException.class)
                .verify();
    }
}
