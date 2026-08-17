package com.banco.customers.dto;

import com.banco.customers.model.CustomerProfile;
import com.banco.customers.model.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private String id;
    private String documentNumber;
    private CustomerType customerType;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private CustomerProfile profile;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
