package com.smartpay.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey,

        @NotNull(message = "Source wallet ID is required")
        Long sourceWalletId,

        @NotNull(message = "Destination wallet ID is required")
        Long destWalletId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        String description
) {}
