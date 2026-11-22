package com.smartpay.wallet.dto;

import com.smartpay.wallet.entity.Currency;
import com.smartpay.wallet.entity.WalletStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletDto(
        Long id,
        Long userId,
        String walletNumber,
        Currency currency,
        WalletStatus status,
        BigDecimal balance,
        Instant createdAt
) {}
