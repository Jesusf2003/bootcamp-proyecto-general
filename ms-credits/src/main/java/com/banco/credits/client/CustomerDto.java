package com.banco.credits.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private String id;
    private String documentNumber;
    private CustomerType customerType;
    private String fullName;
    private String profile;
    private boolean active;
}
