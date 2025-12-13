package com.practice.atm.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MoneyDto(
        @Min(0) long amountInCents,
        @NotBlank String currency
) {}
