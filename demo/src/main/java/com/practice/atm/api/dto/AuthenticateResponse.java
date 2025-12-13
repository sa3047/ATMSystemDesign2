package com.practice.atm.api.dto;

public record AuthenticateResponse(
        String sessionId,
        String customerId,
        long expiresAtEpochSeconds
) {}
