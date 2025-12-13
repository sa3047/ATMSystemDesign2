package com.practice.atm.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepositRequest(
        @NotBlank String sessionId,
        @NotBlank String accountId,
        @NotNull MoneyDto amount
) {}
