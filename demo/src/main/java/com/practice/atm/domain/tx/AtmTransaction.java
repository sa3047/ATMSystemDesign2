package com.practice.atm.domain.tx;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class AtmTransaction {
    private final String transactionId; // idempotency
    private final TransactionType type;
    private final String fromAccountId;
    private final String toAccountId; // for transfers
    private final Money amount;
    private final Instant createdAt;
    private TransactionStatus status;

    public void markAsSuccess() {
        this.status = TransactionStatus.SUCCESS;
    }

    public void markAsFailed() {
        this.status = TransactionStatus.FAILED;
    }

}
