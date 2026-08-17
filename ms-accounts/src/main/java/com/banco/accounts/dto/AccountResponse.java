package com.banco.accounts.dto;

import com.banco.accounts.model.AccountType;
import com.banco.accounts.model.Holder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String id;
    private String accountNumber;
    private String customerId;
    private AccountType accountType;
    private String customerProfile;
    private BigDecimal balance;
    private BigDecimal openingAmount;
    private BigDecimal maintenanceFee;
    private Integer maxFreeMonthlyMovements;
    private BigDecimal movementCommission;
    private Integer fixedTermAllowedDay;
    private List<Holder> holders;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
