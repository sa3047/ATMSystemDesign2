package com.practice.atm.domain.models;


import com.practice.atm.domain.tx.Money;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public final class Account {
    private final AccountId id;
    private final AccountType type;
    private final Currency currency;
    private final CustomerId customerId;
    private Money balance; // snapshot for read model/CQRS
    private final Instant lastUpdated;

    public Account(AccountId id, AccountType type, Currency currency,
                   CustomerId customerId, Money balance, Instant lastUpdated) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.currency = Objects.requireNonNull(currency);
        this.customerId = Objects.requireNonNull(customerId);
        this.balance = balance;
        this.lastUpdated = lastUpdated;
    }

    public void withdraw(Money amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void deposit(Money amount) {
        this.balance = this.balance.add(amount);
    }

}

