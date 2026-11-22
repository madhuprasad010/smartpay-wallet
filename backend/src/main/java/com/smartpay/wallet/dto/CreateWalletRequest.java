package com.smartpay.wallet.dto;

import com.smartpay.wallet.entity.Currency;
import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Currency is required")
        Currency currency
) {}
