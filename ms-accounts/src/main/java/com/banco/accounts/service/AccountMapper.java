package com.banco.accounts.service;

import com.banco.accounts.dto.AccountResponse;
import com.banco.accounts.dto.MovementResponse;
import com.banco.accounts.model.Account;
import com.banco.accounts.model.Movement;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType())
                .customerProfile(account.getCustomerProfile())
                .balance(account.getBalance())
                .openingAmount(account.getOpeningAmount())
                .maintenanceFee(account.getMaintenanceFee())
                .maxFreeMonthlyMovements(account.getMaxFreeMonthlyMovements())
                .movementCommission(account.getMovementCommission())
                .fixedTermAllowedDay(account.getFixedTermAllowedDay())
                .holders(account.getHolders())
                .active(account.isActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public MovementResponse toResponse(Movement movement) {
        return MovementResponse.builder()
                .id(movement.getId())
                .accountId(movement.getAccountId())
                .type(movement.getType())
                .amount(movement.getAmount())
                .commissionCharged(movement.getCommissionCharged())
                .balanceAfter(movement.getBalanceAfter())
                .description(movement.getDescription())
                .counterpartAccountId(movement.getCounterpartAccountId())
                .date(movement.getDate())
                .build();
    }
}
