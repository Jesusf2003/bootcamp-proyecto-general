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
public class CardResponse {
    private String id;
    private String customerId;
    private CreditType cardType;
    private String cardNumber;
    private BigDecimal creditLimit;
    private BigDecimal availableLimit;
    private boolean active;
    private LocalDateTime createdAt;
}
