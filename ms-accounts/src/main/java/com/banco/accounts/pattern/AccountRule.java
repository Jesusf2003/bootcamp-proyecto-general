package com.banco.accounts.pattern;

import com.banco.accounts.client.CustomerDto;
import com.banco.accounts.dto.AccountRequest;
import com.banco.accounts.model.Account;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * Patron de diseno Strategy: cada tipo de cuenta (ahorro, corriente,
 * plazo fijo) tiene sus propias reglas de apertura, comisiones y
 * limites de movimientos. El servicio delega en la implementacion
 * correcta segun {@link com.banco.accounts.model.AccountType} sin
 * usar condicionales dispersos por todo el codigo.
 */
public interface AccountRule {

    /**
     * Valida que el cliente pueda aperturar este tipo de cuenta
     * (ej. un personal ya no puede tener otra cuenta de ahorro).
     * Se completa (o falla) sin emitir valor.
     */
    Mono<Void> validateOpening(CustomerDto customer, List<Account> existingAccountsOfType, AccountRequest request);

    /** Aplica los valores por defecto (comisiones, limites) al construir la cuenta nueva. */
    void applyDefaults(Account account, CustomerDto customer);

    /**
     * Valida que un movimiento (deposito/retiro) sea permitido para este tipo de cuenta,
     * por ejemplo el dia especifico de plazo fijo o el limite mensual de movimientos.
     */
    Mono<Void> validateMovement(Account account, long movementsThisMonth);

    /** Calcula la comision a cobrar por un movimiento segun el conteo mensual ya realizado. */
    BigDecimal calculateCommission(Account account, long movementsThisMonth);
}
