package com.smartpay.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateWalletLimitRequest(
        @NotNull(message = "Daily limit is required")
        @DecimalMin(value = "1.00", message = "Daily limit must be at least 1.00")
        BigDecimal dailyLimit,

        @NotNull(message = "Per-transaction limit is required")
        @DecimalMin(value = "1.00", message = "Per-transaction limit must be at least 1.00")
        BigDecimal perTransactionLimit
) {}
