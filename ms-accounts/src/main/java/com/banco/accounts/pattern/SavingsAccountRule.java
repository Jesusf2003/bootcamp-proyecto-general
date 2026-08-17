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
 * Regla de la cuenta de AHORRO: libre de comision de mantenimiento,
 * con limite maximo de movimientos mensuales gratuitos. Un cliente
 * personal solo puede tener una. Las empresas no pueden tener cuentas
 * de ahorro (Parte I). El perfil VIP (Parte II) exige que el cliente
 * ya tenga una tarjeta de credito con el banco -- esa validacion
 * cruzada la resuelve el servicio, que es quien tiene acceso al
 * cliente de ms-credits.
 */
@Component
public class SavingsAccountRule implements AccountRule {

    private static final int DEFAULT_FREE_MONTHLY_MOVEMENTS = 5;
    private static final BigDecimal DEFAULT_MOVEMENT_COMMISSION = BigDecimal.valueOf(5.0);

    @Override
    public Mono<Void> validateOpening(CustomerDto customer, List<Account> existingAccountsOfType, AccountRequest request) {
        if (customer.getCustomerType() != CustomerType.PERSONAL) {
            return Mono.error(new AccountBusinessRuleException(
                    "Solo los clientes personales pueden aperturar cuentas de ahorro"));
        }
        if (!existingAccountsOfType.isEmpty()) {
            return Mono.error(new AccountBusinessRuleException(
                    "El cliente ya posee una cuenta de ahorro; solo se permite una por cliente personal"));
        }
        return Mono.empty();
    }

    @Override
    public void applyDefaults(Account account, CustomerDto customer) {
        account.setMaintenanceFee(BigDecimal.ZERO);
        boolean vip = "VIP".equalsIgnoreCase(customer.getProfile());
        // El perfil VIP tolera mas movimientos gratuitos como beneficio.
        account.setMaxFreeMonthlyMovements(vip ? DEFAULT_FREE_MONTHLY_MOVEMENTS * 2 : DEFAULT_FREE_MONTHLY_MOVEMENTS);
        account.setMovementCommission(DEFAULT_MOVEMENT_COMMISSION);
    }

    @Override
    public Mono<Void> validateMovement(Account account, long movementsThisMonth) {
        // La cuenta de ahorro no bloquea movimientos al superar el limite,
        // simplemente empieza a cobrar comision (ver calculateCommission).
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
