package com.banco.accounts.pattern;

import com.banco.accounts.client.CustomerDto;
import com.banco.accounts.client.CustomerType;
import com.banco.accounts.dto.AccountRequest;
import com.banco.accounts.exception.AccountBusinessRuleException;
import com.banco.accounts.model.Account;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Regla de la cuenta a PLAZO FIJO: libre de comision de mantenimiento,
 * solo permite un movimiento de retiro o deposito en un dia especifico
 * del mes. Solo disponible para clientes personales, uno por cliente.
 */
@Component
public class FixedTermAccountRule implements AccountRule {

    private static final int DEFAULT_ALLOWED_DAY = 1;

    @Override
    public Mono<Void> validateOpening(CustomerDto customer, List<Account> existingAccountsOfType, AccountRequest request) {
        if (customer.getCustomerType() != CustomerType.PERSONAL) {
            return Mono.error(new AccountBusinessRuleException(
                    "Solo los clientes personales pueden aperturar cuentas a plazo fijo"));
        }
        if (!existingAccountsOfType.isEmpty()) {
            return Mono.error(new AccountBusinessRuleException(
                    "El cliente ya posee una cuenta a plazo fijo; solo se permite una"));
        }
        return Mono.empty();
    }

    @Override
    public void applyDefaults(Account account, CustomerDto customer) {
        account.setMaintenanceFee(BigDecimal.ZERO);
        account.setMaxFreeMonthlyMovements(1);
        account.setMovementCommission(BigDecimal.ZERO);
        account.setFixedTermAllowedDay(DEFAULT_ALLOWED_DAY);
    }

    @Override
    public Mono<Void> validateMovement(Account account, long movementsThisMonth) {
        LocalDate today = LocalDate.now();
        if (account.getFixedTermAllowedDay() != null && today.getDayOfMonth() != account.getFixedTermAllowedDay()) {
            return Mono.error(new AccountBusinessRuleException(
                    "Las cuentas a plazo fijo solo permiten movimientos el dia " + account.getFixedTermAllowedDay() + " del mes"));
        }
        if (movementsThisMonth >= account.getMaxFreeMonthlyMovements()) {
            return Mono.error(new AccountBusinessRuleException(
                    "Ya se realizo el unico movimiento permitido este mes en la cuenta a plazo fijo"));
        }
        return Mono.empty();
    }

    @Override
    public BigDecimal calculateCommission(Account account, long movementsThisMonth) {
        return BigDecimal.ZERO;
    }
}
