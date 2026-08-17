package com.banco.credits.service;

import com.banco.credits.client.CustomerClient;
import com.banco.credits.client.CustomerType;
import com.banco.credits.dto.CreditRequest;
import com.banco.credits.dto.CreditResponse;
import com.banco.credits.dto.PaymentRequest;
import com.banco.credits.exception.CreditBusinessRuleException;
import com.banco.credits.exception.CreditNotFoundException;
import com.banco.credits.kafka.CreditEventProducer;
import com.banco.credits.model.Credit;
import com.banco.credits.model.CreditType;
import com.banco.credits.repository.CreditRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Implementacion del servicio de creditos. Regla clave del
 * enunciado: un cliente PERSONAL solo puede tener un credito; una
 * empresa puede tener varios.
 */
@Slf4j
@Service
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final CreditMapper creditMapper;
    private final CustomerClient customerClient;
    private final CreditEventProducer eventProducer;
    private final DebtCheckService debtCheckService;

    public CreditServiceImpl(CreditRepository creditRepository, CreditMapper creditMapper,
                              CustomerClient customerClient, CreditEventProducer eventProducer,
                              DebtCheckService debtCheckService) {
        this.creditRepository = creditRepository;
        this.creditMapper = creditMapper;
        this.customerClient = customerClient;
        this.eventProducer = eventProducer;
        this.debtCheckService = debtCheckService;
    }

    @Override
    public Mono<CreditResponse> create(CreditRequest request) {
        return debtCheckService.hasOverdueDebt(request.getCustomerId())
                .flatMap(overdue -> {
                    if (Boolean.TRUE.equals(overdue)) {
                        return Mono.error(new CreditBusinessRuleException(
                                "El cliente tiene deuda vencida en un producto de credito; no puede adquirir un nuevo producto"));
                    }
                    return customerClient.findById(request.getCustomerId());
                })
                .flatMap(customer -> {
                    if (customer.getCustomerType() == CustomerType.PERSONAL && request.getCreditType() == CreditType.BUSINESS) {
                        return Mono.error(new CreditBusinessRuleException(
                                "Un cliente personal no puede solicitar un credito empresarial"));
                    }
                    if (request.getCreditType() == CreditType.PERSONAL) {
                        return creditRepository.findByCustomerIdAndCreditType(request.getCustomerId(), CreditType.PERSONAL)
                                .hasElements()
                                .flatMap(exists -> Boolean.TRUE.equals(exists)
                                        ? Mono.error(new CreditBusinessRuleException(
                                                "El cliente ya tiene un credito personal; solo se permite uno"))
                                        : Mono.just(customer));
                    }
                    return Mono.just(customer);
                })
                .flatMap(customer -> {
                    Credit credit = Credit.builder()
                            .customerId(request.getCustomerId())
                            .creditType(request.getCreditType())
                            .amount(request.getAmount())
                            .outstandingBalance(request.getAmount())
                            .active(true)
                            .build();
                    return creditRepository.save(credit);
                })
                .doOnNext(c -> log.info("Credito creado id={} cliente={} monto={}", c.getId(), c.getCustomerId(), c.getAmount()))
                .map(creditMapper::toResponse);
    }

    @Override
    public Flux<CreditResponse> findAll() {
        return creditRepository.findAll().map(creditMapper::toResponse);
    }

    @Override
    public Mono<CreditResponse> findById(String id) {
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(id)))
                .map(creditMapper::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(id)))
                .flatMap(creditRepository::delete);
    }

    @Override
    public Mono<CreditResponse> pay(String creditId, PaymentRequest request) {
        return creditRepository.findById(creditId)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(creditId)))
                .flatMap(credit -> {
                    BigDecimal newBalance = credit.getOutstandingBalance().subtract(request.getAmount());
                    credit.setOutstandingBalance(newBalance.signum() < 0 ? BigDecimal.ZERO : newBalance);
                    return creditRepository.save(credit);
                })
                .doOnNext(c -> {
                    log.info("Pago aplicado al credito {} monto={} pagador={}",
                            creditId, request.getAmount(), request.getPayerCustomerId());
                    eventProducer.publish(c.getCustomerId(), creditId, "CREDIT_PAYMENT", request.getAmount());
                })
                .map(creditMapper::toResponse);
    }

    @Override
    public Mono<CreditResponse> markOverdue(String creditId, boolean overdue) {
        return creditRepository.findById(creditId)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(creditId)))
                .flatMap(credit -> {
                    credit.setOverdue(overdue);
                    return creditRepository.save(credit);
                })
                .doOnNext(c -> log.info("Credito {} marcado overdue={}", creditId, overdue))
                .map(creditMapper::toResponse);
    }
}
