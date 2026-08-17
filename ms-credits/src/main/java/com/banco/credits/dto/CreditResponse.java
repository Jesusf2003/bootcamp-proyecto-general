package com.banco.credits.dto;

import com.banco.credits.model.CreditType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponse {
    private String id;
    private String customerId;
    private CreditType creditType;
    private BigDecimal amount;
    private BigDecimal outstandingBalance;
    private BigDecimal interestRate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
