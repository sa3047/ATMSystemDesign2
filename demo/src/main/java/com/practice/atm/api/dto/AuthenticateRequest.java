package com.practice.atm.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticateRequest(
        @NotBlank String cardNumber,
        @NotBlank String method,        // "PIN" or "BIOMETRIC"
        String pin,
        String biometricToken
) {}

