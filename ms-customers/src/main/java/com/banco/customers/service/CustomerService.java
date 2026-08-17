package com.banco.customers.service;

import com.banco.customers.dto.CustomerRequest;
import com.banco.customers.dto.CustomerResponse;
import com.banco.customers.model.CustomerProfile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Contrato del servicio de negocio de clientes. Expone las
 * operaciones CRUD requeridas por el enunciado (Create, FindAll,
 * Update, Delete) mas las consultas especificas del dominio.
 */
public interface CustomerService {

    Mono<CustomerResponse> create(CustomerRequest request);

    Flux<CustomerResponse> findAll();

    Mono<CustomerResponse> findById(String id);

    Mono<CustomerResponse> update(String id, CustomerRequest request);

    Mono<Void> delete(String id);

    /** Usado por ms-accounts al activar un producto VIP/PYME sobre un cliente existente. */
    Mono<CustomerResponse> updateProfile(String id, CustomerProfile profile);
}
