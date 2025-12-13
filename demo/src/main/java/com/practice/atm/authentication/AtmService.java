package com.practice.atm.authentication;

import com.practice.atm.domain.tx.AtmTransaction;
import com.practice.atm.domain.tx.Money;

public interface AtmService {
    AuthSession authenticate(String cardNumber, AuthenticationRequest request);
    AtmTransaction withdraw(String sessionId, String accountId, Money amount);
    AtmTransaction deposit(String sessionId, String accountId, Money amount);
    AtmTransaction transfer(String sessionId, String fromAccountId, String toAccountId, Money amount);
    Money getBalance(String sessionId, String accountId);
}
