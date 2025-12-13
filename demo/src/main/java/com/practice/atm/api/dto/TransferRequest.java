package com.practice.atm.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
        @NotBlank String sessionId,
        @NotBlank String fromAccountId,
        @NotBlank String toAccountId,
        @NotNull MoneyDto amount
) {}
