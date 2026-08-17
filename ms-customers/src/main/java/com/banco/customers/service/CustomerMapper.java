package com.banco.customers.service;

import com.banco.customers.dto.CustomerRequest;
import com.banco.customers.dto.CustomerResponse;
import com.banco.customers.model.Customer;
import org.springframework.stereotype.Component;

/**
 * Mapeo manual (liviano, sin librerias externas) entre el modelo
 * de persistencia y los DTOs expuestos por la API.
 */
@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .documentNumber(request.getDocumentNumber())
                .customerType(request.getCustomerType())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .active(true)
                .build();
    }

    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .documentNumber(customer.getDocumentNumber())
                .customerType(customer.getCustomerType())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .profile(customer.getProfile())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
