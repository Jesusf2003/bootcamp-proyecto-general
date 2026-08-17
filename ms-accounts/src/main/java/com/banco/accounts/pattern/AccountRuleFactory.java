package com.banco.accounts.pattern;

import com.banco.accounts.model.AccountType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Patron de diseno Factory: entrega la estrategia {@link AccountRule}
 * correcta segun el {@link AccountType}, evitando que el servicio
 * tenga que decidir con un switch cada vez que necesita las reglas.
 */
@Component
public class AccountRuleFactory {

    private final Map<AccountType, AccountRule> rules = new EnumMap<>(AccountType.class);

    public AccountRuleFactory(SavingsAccountRule savingsRule,
                               CheckingAccountRule checkingRule,
                               FixedTermAccountRule fixedTermRule) {
        rules.put(AccountType.SAVINGS, savingsRule);
        rules.put(AccountType.CHECKING, checkingRule);
        rules.put(AccountType.FIXED_TERM, fixedTermRule);
    }

    public AccountRule getRule(AccountType accountType) {
        AccountRule rule = rules.get(accountType);
        if (rule == null) {
            throw new IllegalArgumentException("No existe regla de negocio para el tipo de cuenta: " + accountType);
        }
        return rule;
    }
}
