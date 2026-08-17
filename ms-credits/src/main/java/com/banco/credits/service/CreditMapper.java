package com.banco.credits.service;

import com.banco.credits.dto.CardMovementResponse;
import com.banco.credits.dto.CardResponse;
import com.banco.credits.dto.CreditResponse;
import com.banco.credits.model.CardMovement;
import com.banco.credits.model.Credit;
import com.banco.credits.model.CreditCard;
import org.springframework.stereotype.Component;

@Component
public class CreditMapper {

    public CreditResponse toResponse(Credit credit) {
        return CreditResponse.builder()
                .id(credit.getId())
                .customerId(credit.getCustomerId())
                .creditType(credit.getCreditType())
                .amount(credit.getAmount())
                .outstandingBalance(credit.getOutstandingBalance())
                .interestRate(credit.getInterestRate())
                .active(credit.isActive())
                .createdAt(credit.getCreatedAt())
                .updatedAt(credit.getUpdatedAt())
                .build();
    }

    public CardResponse toResponse(CreditCard card) {
        return CardResponse.builder()
                .id(card.getId())
                .customerId(card.getCustomerId())
                .cardType(card.getCardType())
                .cardNumber(card.getCardNumber())
                .creditLimit(card.getCreditLimit())
                .availableLimit(card.getAvailableLimit())
                .active(card.isActive())
                .createdAt(card.getCreatedAt())
                .build();
    }

    public CardMovementResponse toResponse(CardMovement movement) {
        return CardMovementResponse.builder()
                .id(movement.getId())
                .cardId(movement.getCardId())
                .type(movement.getType())
                .amount(movement.getAmount())
                .availableLimitAfter(movement.getAvailableLimitAfter())
                .description(movement.getDescription())
                .date(movement.getDate())
                .build();
    }
}
