package com.banco.customers.controller;

import com.banco.customers.dto.CustomerRequest;
import com.banco.customers.dto.CustomerResponse;
import com.banco.customers.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST de clientes. Expone las operaciones CRUD
 * (Create, FindAll, Update, Delete) siguiendo los lineamientos REST.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Gestion de clientes personales y empresariales")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Crear un nuevo cliente")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        log.info("POST /customers - creando cliente con documento {}", request.getDocumentNumber());
        return customerService.create(request);
    }

    @Operation(summary = "Listar todos los clientes")
    @GetMapping
    public Flux<CustomerResponse> findAll() {
        log.info("GET /customers - listando todos los clientes");
        return customerService.findAll();
    }

    @Operation(summary = "Obtener un cliente por id")
    @GetMapping("/{id}")
    public Mono<CustomerResponse> findById(@PathVariable String id) {
        log.info("GET /customers/{} ", id);
        return customerService.findById(id);
    }

    @Operation(summary = "Actualizar un cliente existente")
    @PutMapping("/{id}")
    public Mono<CustomerResponse> update(@PathVariable String id, @Valid @RequestBody CustomerRequest request) {
        log.info("PUT /customers/{}", id);
        return customerService.update(id, request);
    }

    @Operation(summary = "Eliminar un cliente")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        log.info("DELETE /customers/{}", id);
        return customerService.delete(id);
    }

    @Operation(summary = "Actualizar el perfil especial del cliente (VIP/PYME)")
    @PatchMapping("/{id}/profile")
    public Mono<CustomerResponse> updateProfile(@PathVariable String id,
                                                 @RequestParam com.banco.customers.model.CustomerProfile profile) {
        log.info("PATCH /customers/{}/profile -> {}", id, profile);
        return customerService.updateProfile(id, profile);
    }
}
