package com.practice.atm.authentication;

import java.time.Instant;

public record AuthSession(String sesionId,
                          String customerId,
                          Instant expiresAt) {

}

