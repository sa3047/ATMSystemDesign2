package com.practice.atm.service;

import com.practice.atm.domain.tx.Money;
import org.springframework.stereotype.Component;

@Component
public class FraudService {
    public void checkWithdraw(String customerId, String accountId, Money amount) {
        // placeholder: call external fraud detection, rate limiting, etc.
    }
    public void checkDeposit(String customerId, String accountId, Money amount) {}
    public void checkTransfer(String customerId, String fromAccountId, String toAccountId, Money amount) {}
}
