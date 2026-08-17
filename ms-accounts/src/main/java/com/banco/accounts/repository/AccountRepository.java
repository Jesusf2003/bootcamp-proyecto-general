package com.banco.accounts.repository;

import com.banco.accounts.model.Account;
import com.banco.accounts.model.AccountType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

    Flux<Account> findByCustomerId(String customerId);

    Flux<Account> findByCustomerIdAndAccountType(String customerId, AccountType accountType);

    Mono<Account> findByAccountNumber(String accountNumber);

    Mono<Boolean> existsByAccountNumber(String accountNumber);
}
