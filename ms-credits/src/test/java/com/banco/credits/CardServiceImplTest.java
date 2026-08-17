package com.banco.credits;

import com.banco.credits.client.CustomerClient;
import com.banco.credits.client.CustomerDto;
import com.banco.credits.client.CustomerType;
import com.banco.credits.dto.CardRequest;
import com.banco.credits.dto.ConsumptionRequest;
import com.banco.credits.kafka.CreditEventProducer;
import com.banco.credits.model.CardMovement;
import com.banco.credits.model.CreditCard;
import com.banco.credits.model.CreditType;
import com.banco.credits.repository.CardMovementRepository;
import com.banco.credits.repository.CreditCardRepository;
import com.banco.credits.service.CardServiceImpl;
import com.banco.credits.service.CreditMapper;
import com.banco.credits.service.DebtCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de tarjetas de credito: emision de
 * tarjeta y validacion del limite disponible en un consumo.
 */
class CardServiceImplTest {

    private CreditCardRepository cardRepository;
    private CardMovementRepository movementRepository;
    private CustomerClient customerClient;
    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        cardRepository = Mockito.mock(CreditCardRepository.class);
        movementRepository = Mockito.mock(CardMovementRepository.class);
        customerClient = Mockito.mock(CustomerClient.class);
        DebtCheckService debtCheckService = Mockito.mock(DebtCheckService.class);
        when(debtCheckService.hasOverdueDebt(org.mockito.ArgumentMatchers.anyString())).thenReturn(Mono.just(false));
        cardService = new CardServiceImpl(cardRepository, movementRepository, new CreditMapper(), customerClient,
                Mockito.mock(CreditEventProducer.class), debtCheckService);
    }

    @Test
    void shouldCreateCreditCardForExistingCustomer() {
        CustomerDto customer = new CustomerDto("c1", "12345678", CustomerType.PERSONAL, "Ana Lopez", "STANDARD", true);
        CardRequest request = CardRequest.builder()
                .customerId("c1")
                .cardType(com.banco.credits.model.CreditType.PERSONAL)
                .creditLimit(BigDecimal.valueOf(1000))
                .build();

        CreditCard saved = CreditCard.builder().id("card1").customerId("c1")
                .creditLimit(BigDecimal.valueOf(1000)).availableLimit(BigDecimal.valueOf(1000)).build();

        when(customerClient.findById("c1")).thenReturn(Mono.just(customer));
        when(cardRepository.save(any(CreditCard.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(cardService.create(request))
                .expectNextMatches(response -> response.getId().equals("card1"))
                .verifyComplete();
    }

    @Test
    void shouldRejectConsumptionWhenExceedsAvailableLimit() {
        CreditCard card = CreditCard.builder().id("card1").customerId("c1")
                .creditLimit(BigDecimal.valueOf(500)).availableLimit(BigDecimal.valueOf(100)).build();

        when(cardRepository.findById("card1")).thenReturn(Mono.just(card));

        ConsumptionRequest request = ConsumptionRequest.builder()
                .amount(BigDecimal.valueOf(200))
                .description("Compra en tienda")
                .build();

        StepVerifier.create(cardService.consume("card1", request))
                .expectError(com.banco.credits.exception.InsufficientCreditLimitException.class)
                .verify();
    }

    @Test
    void shouldRegisterConsumptionWhenWithinAvailableLimit() {
        CreditCard card = CreditCard.builder().id("card1").customerId("c1")
                .creditLimit(BigDecimal.valueOf(500)).availableLimit(BigDecimal.valueOf(300)).build();
        CreditCard updated = CreditCard.builder().id("card1").customerId("c1")
                .creditLimit(BigDecimal.valueOf(500)).availableLimit(BigDecimal.valueOf(200)).build();
        CardMovement movement = CardMovement.builder().id("m1").cardId("card1")
                .type(com.banco.credits.model.CardMovementType.CONSUMPTION)
                .amount(BigDecimal.valueOf(100)).availableLimitAfter(BigDecimal.valueOf(200)).build();

        when(cardRepository.findById("card1")).thenReturn(Mono.just(card));
        when(cardRepository.save(any(CreditCard.class))).thenReturn(Mono.just(updated));
        when(movementRepository.save(any(CardMovement.class))).thenReturn(Mono.just(movement));

        ConsumptionRequest request = ConsumptionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .description("Compra supermercado")
                .build();

        StepVerifier.create(cardService.consume("card1", request))
                .expectNextMatches(response -> response.getAmount().compareTo(BigDecimal.valueOf(100)) == 0)
                .verifyComplete();
    }
}
