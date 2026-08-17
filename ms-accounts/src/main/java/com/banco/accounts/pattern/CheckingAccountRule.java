package com.banco.accounts.pattern;

import com.banco.accounts.client.CustomerDto;
import com.banco.accounts.client.CustomerType;
import com.banco.accounts.dto.AccountRequest;
import com.banco.accounts.exception.AccountBusinessRuleException;
import com.banco.accounts.model.Account;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * Regla de la cuenta CORRIENTE: tiene comision de mantenimiento y
 * sin limite de movimientos mensuales gratuitos. Un cliente personal
 * solo puede tener una; un cliente empresarial puede tener varias.
 * El perfil PYME (Parte II) exime de la comision de mantenimiento,
 * pero exige que el cliente ya tenga una tarjeta de credito con el banco.
 */
@Component
public class CheckingAccountRule implements AccountRule {

    private static final BigDecimal DEFAULT_MAINTENANCE_FEE = BigDecimal.valueOf(15.0);
    private static final int DEFAULT_FREE_MONTHLY_MOVEMENTS = 10;
    private static final BigDecimal DEFAULT_MOVEMENT_COMMISSION = BigDecimal.valueOf(3.0);

    @Override
    public Mono<Void> validateOpening(CustomerDto customer, List<Account> existingAccountsOfType, AccountRequest request) {
        if (customer.getCustomerType() == CustomerType.PERSONAL && !existingAccountsOfType.isEmpty()) {
            return Mono.error(new AccountBusinessRuleException(
                    "El cliente personal ya posee una cuenta corriente; solo se permite una"));
        }
        return Mono.empty();
    }

    @Override
    public void applyDefaults(Account account, CustomerDto customer) {
        boolean pyme = "PYME".equalsIgnoreCase(customer.getProfile());
        account.setMaintenanceFee(pyme ? BigDecimal.ZERO : DEFAULT_MAINTENANCE_FEE);
        account.setMaxFreeMonthlyMovements(DEFAULT_FREE_MONTHLY_MOVEMENTS);
        account.setMovementCommission(DEFAULT_MOVEMENT_COMMISSION);
    }

    @Override
    public Mono<Void> validateMovement(Account account, long movementsThisMonth) {
        return Mono.empty();
    }

    @Override
    public BigDecimal calculateCommission(Account account, long movementsThisMonth) {
        if (movementsThisMonth >= account.getMaxFreeMonthlyMovements()) {
            return account.getMovementCommission();
        }
        return BigDecimal.ZERO;
    }
}
