package com.smartpay.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WithdrawalRequest(
        @NotNull(message = "Wallet ID is required")
        Long walletId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Withdrawal amount must be at least 1.00")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotBlank(message = "Bank account number is required")
        String bankAccountNumber,

        String ifscCode,

        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey,

        String description
) {}
