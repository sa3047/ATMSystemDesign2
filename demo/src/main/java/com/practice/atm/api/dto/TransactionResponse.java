package com.practice.atm.api.dto;

public record TransactionResponse(
        String transactionId,
        String type,
        String status,
        MoneyDto amount
) {}
