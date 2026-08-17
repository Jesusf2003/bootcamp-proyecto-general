package com.banco.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardResponse {
    private String id;
    private String customerId;
    private String cardNumber;
    private String primaryAccountId;
    private List<String> associatedAccountIds;
    private boolean active;
    private LocalDateTime createdAt;
}
