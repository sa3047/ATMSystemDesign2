package com.practice.atm.repositories.infra;

import com.practice.atm.domain.models.Card;
import com.practice.atm.repositories.CardRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCardRepository implements CardRepository {

    private final Map<String, Card> cards = new ConcurrentHashMap<>();

    @Override
    public Optional<Card> findByCardNumber(String cardNumber) {
        return Optional.ofNullable(cards.get(cardNumber));
    }

    @Override
    public void save(Card card) {
        cards.put(card.getNumber().value(), card);
    }

    public void put(Card card) { save(card); }
}
