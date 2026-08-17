package com.banco.accounts.dto;

import com.banco.accounts.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Fila del reporte general por producto: resume los movimientos de
 * una cuenta dentro del intervalo de tiempo solicitado por el usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountReportItem {
    private String accountId;
    private String accountNumber;
    private AccountType accountType;
    private long totalMovements;
    private BigDecimal totalDeposited;
    private BigDecimal totalWithdrawn;
    private BigDecimal totalCommissionsCharged;
    private BigDecimal currentBalance;
}
