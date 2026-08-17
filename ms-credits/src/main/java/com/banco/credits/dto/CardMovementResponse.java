package com.banco.credits.dto;

import com.banco.credits.model.CardMovementType;
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
public class CardMovementResponse {
    private String id;
    private String cardId;
    private CardMovementType type;
    private BigDecimal amount;
    private BigDecimal availableLimitAfter;
    private String description;
    private LocalDateTime date;
}
