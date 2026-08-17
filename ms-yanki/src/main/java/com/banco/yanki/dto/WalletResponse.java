package com.banco.yanki.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private String id;
    private String phoneNumber;
    private String customerId;
    private String documentNumber;
    private String linkedAccountId;
    private boolean active;
    private LocalDateTime createdAt;
}
