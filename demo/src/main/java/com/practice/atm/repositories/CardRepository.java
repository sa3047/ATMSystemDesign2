package com.practice.atm.repositories;

import com.practice.atm.domain.models.Card;

import java.util.Optional;

public interface CardRepository {
    Optional<Card> findByCardNumber(String cardNumber);
    void save(Card card);
}
