package com.banco.accounts.dto;

import com.banco.accounts.model.MovementType;
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
public class MovementResponse {
    private String id;
    private String accountId;
    private MovementType type;
    private BigDecimal amount;
    private BigDecimal commissionCharged;
    private BigDecimal balanceAfter;
    private String description;
    private String counterpartAccountId;
    private LocalDateTime date;
}
