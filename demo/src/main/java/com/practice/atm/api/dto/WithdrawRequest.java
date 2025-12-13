package com.practice.atm.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WithdrawRequest(
        @NotBlank String sessionId,
        @NotBlank String accountId,
        @NotNull MoneyDto amount
) {}
