package com.banco.accounts.service;

import com.banco.accounts.client.CreditClient;
import com.banco.accounts.client.CustomerClient;
import com.banco.accounts.client.CustomerDto;
import com.banco.accounts.kafka.MovementEventProducer;
import com.banco.accounts.dto.AccountReportItem;
import com.banco.accounts.dto.AccountRequest;
import com.banco.accounts.dto.AccountResponse;
import com.banco.accounts.dto.MovementRequest;
import com.banco.accounts.dto.MovementResponse;
import com.banco.accounts.dto.TransferRequest;
import com.banco.accounts.exception.AccountBusinessRuleException;
import com.banco.accounts.exception.AccountNotFoundException;
import com.banco.accounts.exception.InsufficientFundsException;
import com.banco.accounts.model.Account;
import com.banco.accounts.model.Holder;
import com.banco.accounts.model.Movement;
import com.banco.accounts.model.MovementType;
import com.banco.accounts.pattern.AccountRule;
import com.banco.accounts.pattern.AccountRuleFactory;
import com.banco.accounts.repository.AccountRepository;
import com.banco.accounts.repository.MovementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de cuentas. Compone las validaciones
 * de negocio (Strategy: {@link AccountRule}) con las llamadas
 * reactivas a ms-customers y ms-credits, protegidas con circuit
 * breaker.
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final AccountMapper accountMapper;
    private final AccountRuleFactory ruleFactory;
    private final CustomerClient customerClient;
    private final CreditClient creditClient;
    private final MovementEventProducer movementEventProducer;

    public AccountServiceImpl(AccountRepository accountRepository,
                               MovementRepository movementRepository,
                               AccountMapper accountMapper,
                               AccountRuleFactory ruleFactory,
                               CustomerClient customerClient,
                               CreditClient creditClient,
                               MovementEventProducer movementEventProducer) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.accountMapper = accountMapper;
        this.ruleFactory = ruleFactory;
        this.customerClient = customerClient;
        this.creditClient = creditClient;
        this.movementEventProducer = movementEventProducer;
    }

    @Override
    public Mono<AccountResponse> create(AccountRequest request) {
        AccountRule rule = ruleFactory.getRule(request.getAccountType());

        return creditClient.hasOverdueDebt(request.getCustomerId())
                .flatMap(overdue -> {
                    if (Boolean.TRUE.equals(overdue)) {
                        return Mono.<CustomerDto>error(new AccountBusinessRuleException(
                                "El cliente tiene deuda vencida en un producto de credito; no puede adquirir un nuevo producto"));
                    }
                    return customerClient.findById(request.getCustomerId());
                })
                .flatMap(customer -> resolveProfile(customer, request))
                .flatMap(customer -> accountRepository
                        .findByCustomerIdAndAccountType(request.getCustomerId(), request.getAccountType())
                        .collectList()
                        .flatMap(existing -> rule.validateOpening(customer, existing, request)
                                .thenReturn(customer)))
                .flatMap(customer -> {
                    Account account = Account.builder()
                            .accountNumber(generateAccountNumber())
                            .customerId(request.getCustomerId())
                            .accountType(request.getAccountType())
                            .customerProfile(customer.getProfile())
                            .openingAmount(request.getOpeningAmount())
                            .balance(request.getOpeningAmount())
                            .holders(buildHolders(customer, request))
                            .active(true)
                            .build();
                    rule.applyDefaults(account, customer);
                    return accountRepository.save(account);
                })
                .doOnNext(a -> log.info("Cuenta creada numero={} tipo={} cliente={}",
                        a.getAccountNumber(), a.getAccountType(), a.getCustomerId()))
                .map(accountMapper::toResponse);
    }

    /**
     * Si el request pide un perfil especial (VIP/PYME) distinto al actual,
     * valida el requisito de tarjeta de credito y actualiza el perfil en
     * ms-customers antes de continuar con la apertura.
     */
    private Mono<CustomerDto> resolveProfile(CustomerDto customer, AccountRequest request) {
        String requested = request.getRequestedProfile();
        if (requested == null || requested.equalsIgnoreCase(customer.getProfile())) {
            return Mono.just(customer);
        }
        return creditClient.hasCreditCard(customer.getId())
                .flatMap(hasCard -> {
                    if (Boolean.FALSE.equals(hasCard)) {
                        return Mono.error(new AccountBusinessRuleException(
                                "El cliente debe tener una tarjeta de credito con el banco para acceder al perfil " + requested));
                    }
                    return customerClient.updateProfile(customer.getId(), requested);
                });
    }

    private List<Holder> buildHolders(CustomerDto customer, AccountRequest request) {
        // Uso de Streams (Parte II) para construir la lista de titulares/firmantes.
        List<Holder> holders = new java.util.ArrayList<>();
        holders.add(Holder.builder()
                .customerId(customer.getId())
                .fullName(customer.getFullName())
                .authorizedSigner(false)
                .build());
        if (request.getAdditionalHolderIds() != null) {
            holders.addAll(request.getAdditionalHolderIds().stream()
                    .filter(id -> !id.equals(customer.getId()))
                    .map(id -> Holder.builder().customerId(id).authorizedSigner(true).build())
                    .collect(Collectors.toList()));
        }
        return holders;
    }

    @Override
    public Flux<AccountResponse> findAll() {
        return accountRepository.findAll().map(accountMapper::toResponse);
    }

    @Override
    public Mono<AccountResponse> findById(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(id)))
                .map(accountMapper::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(id)))
                .flatMap(accountRepository::delete);
    }

    @Override
    public Mono<MovementResponse> deposit(String accountId, MovementRequest request) {
        return applyMovement(accountId, request.getAmount(), MovementType.DEPOSIT, request.getDescription(), null);
    }

    @Override
    public Mono<MovementResponse> withdraw(String accountId, MovementRequest request) {
        return applyMovement(accountId, request.getAmount().negate(), MovementType.WITHDRAWAL, request.getDescription(), null);
    }

    @Override
    public Mono<Void> transfer(String sourceAccountId, TransferRequest request) {
        return accountRepository.findById(request.getTargetAccountId())
                .switchIfEmpty(Mono.error(new AccountNotFoundException(request.getTargetAccountId())))
                .then(applyMovement(sourceAccountId, request.getAmount().negate(), MovementType.TRANSFER_OUT,
                        request.getDescription(), request.getTargetAccountId()))
                .then(applyMovement(request.getTargetAccountId(), request.getAmount(), MovementType.TRANSFER_IN,
                        request.getDescription(), sourceAccountId))
                .then();
    }

    /**
     * Nucleo transaccional de depositos, retiros y transferencias.
     * amount positivo = credito a la cuenta, negativo = debito.
     */
    private Mono<MovementResponse> applyMovement(String accountId, BigDecimal amount, MovementType type,
                                                  String description, String counterpartAccountId) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(accountId)))
                .flatMap(account -> countMovementsThisMonth(accountId)
                        .flatMap(count -> {
                            AccountRule rule = ruleFactory.getRule(account.getAccountType());
                            return rule.validateMovement(account, count)
                                    .then(Mono.fromCallable(() -> rule.calculateCommission(account, count)))
                                    .flatMap(commission -> finalizeMovement(account, amount, commission, type,
                                            description, counterpartAccountId));
                        }));
    }

    private Mono<MovementResponse> finalizeMovement(Account account, BigDecimal amount, BigDecimal commission,
                                                      MovementType type, String description, String counterpartAccountId) {
        BigDecimal totalDebit = amount.signum() < 0 ? amount.abs().add(commission) : commission;
        BigDecimal newBalance = amount.signum() < 0
                ? account.getBalance().subtract(totalDebit)
                : account.getBalance().add(amount).subtract(commission);

        if (newBalance.signum() < 0) {
            return Mono.error(new InsufficientFundsException(account.getId()));
        }

        account.setBalance(newBalance);

        Movement movement = Movement.builder()
                .accountId(account.getId())
                .type(type)
                .amount(amount.abs())
                .commissionCharged(commission)
                .balanceAfter(newBalance)
                .description(description)
                .counterpartAccountId(counterpartAccountId)
                .build();

        return accountRepository.save(account)
                .then(movementRepository.save(movement))
                .doOnNext(m -> {
                    log.info("Movimiento {} registrado en cuenta {} monto={} comision={}",
                            type, account.getId(), amount, commission);
                    movementEventProducer.publish(account.getId(), type.name(), amount.abs(), newBalance);
                })
                .map(accountMapper::toResponse);
    }

    private Mono<Long> countMovementsThisMonth(String accountId) {
        YearMonth currentMonth = YearMonth.from(LocalDate.now());
        LocalDateTime from = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime to = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        return movementRepository.findByAccountIdAndDateBetween(accountId, from, to).count();
    }

    @Override
    public Mono<AccountResponse> getBalance(String accountId) {
        return findById(accountId);
    }

    @Override
    public Flux<MovementResponse> getMovements(String accountId) {
        return movementRepository.findByAccountIdOrderByDateDesc(accountId).map(accountMapper::toResponse);
    }

    @Override
    public Flux<MovementResponse> getLastMovements(String accountId, int limit) {
        return movementRepository.findByAccountIdOrderByDateDesc(accountId).take(limit).map(accountMapper::toResponse);
    }

    @Override
    public Mono<MovementResponse> debitCascade(java.util.List<String> orderedAccountIds, BigDecimal amount, String description) {
        return tryDebitFrom(orderedAccountIds, 0, amount, description);
    }

    private Mono<MovementResponse> tryDebitFrom(java.util.List<String> accountIds, int index,
                                                  BigDecimal amount, String description) {
        if (index >= accountIds.size()) {
            return Mono.error(new AccountBusinessRuleException(
                    "Ninguna de las cuentas asociadas a la tarjeta tiene fondos suficientes"));
        }
        String accountId = accountIds.get(index);
        return applyMovement(accountId, amount.negate(), MovementType.WITHDRAWAL, description, null)
                .onErrorResume(InsufficientFundsException.class,
                        ex -> tryDebitFrom(accountIds, index + 1, amount, description));
    }

    @Override
    public Flux<AccountReportItem> generateReport(LocalDateTime from, LocalDateTime to) {
        return accountRepository.findAll()
                .flatMap(account -> movementRepository.findByAccountIdAndDateBetween(account.getId(), from, to)
                        .collectList()
                        .map(movements -> buildReportItem(account, movements)));
    }

    /**
     * Construye la fila de reporte usando la API de Streams (requerimiento
     * explicito de la Parte II) para agregar los movimientos del intervalo.
     */
    private AccountReportItem buildReportItem(Account account, List<Movement> movements) {
        BigDecimal totalDeposited = movements.stream()
                .filter(m -> m.getType() == MovementType.DEPOSIT || m.getType() == MovementType.TRANSFER_IN)
                .map(Movement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawn = movements.stream()
                .filter(m -> m.getType() == MovementType.WITHDRAWAL || m.getType() == MovementType.TRANSFER_OUT)
                .map(Movement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommissions = movements.stream()
                .map(Movement::getCommissionCharged)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AccountReportItem.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .totalMovements(movements.size())
                .totalDeposited(totalDeposited)
                .totalWithdrawn(totalWithdrawn)
                .totalCommissionsCharged(totalCommissions)
                .currentBalance(account.getBalance())
                .build();
    }

    private String generateAccountNumber() {
        return "AC-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
