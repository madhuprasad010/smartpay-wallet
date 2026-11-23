package com.smartpay.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WalletLimitDto(
        Long id,
        Long walletId,
        BigDecimal dailyLimit,
        BigDecimal perTransactionLimit,
        BigDecimal dailyUsed,
        BigDecimal remainingDailyLimit,
        LocalDate lastResetDate
) {}
