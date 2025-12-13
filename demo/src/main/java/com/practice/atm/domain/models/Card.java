package com.practice.atm.domain.models;

import lombok.Getter;

import java.time.Instant;

@Getter
public final class Card {
    private final CardNumber number;
    private final CustomerId customerId;
    private final Instant expiry;
    private final boolean blocked;

    public Card(CardNumber number, CustomerId customerId, Instant expiry, boolean blocked) {
        this.number = number;
        this.customerId = customerId;
        this.expiry = expiry;
        this.blocked = blocked;
    }
}
