package com.smartpay.beneficiary.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BeneficiaryTransferRequest(
        @NotNull(message = "Source wallet ID is required")
        Long sourceWalletId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey,

        String description
) {}
