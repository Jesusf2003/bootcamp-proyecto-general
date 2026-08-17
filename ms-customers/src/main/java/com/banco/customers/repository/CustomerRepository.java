package com.banco.customers.repository;

import com.banco.customers.model.Customer;
import com.banco.customers.model.CustomerType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo de clientes. Se usan metodos derivados
 * (query methods) de Spring Data; queda prohibido el uso de
 * @Query o SQL dinamico segun los requerimientos no funcionales.
 */
public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

    Mono<Customer> findByDocumentNumber(String documentNumber);

    Mono<Boolean> existsByDocumentNumber(String documentNumber);

    Flux<Customer> findByCustomerType(CustomerType customerType);

    Flux<Customer> findByActiveTrue();
}
