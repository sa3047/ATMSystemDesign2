package com.practice.atm.authentication;

import java.time.Instant;

public record AuthSession(String sessionId,
                          String customerId,
                          Instant expiresAt) {

}

