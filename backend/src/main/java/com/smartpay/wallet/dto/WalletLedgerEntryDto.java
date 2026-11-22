package com.smartpay.wallet.dto;

import com.smartpay.wallet.entity.EntryType;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletLedgerEntryDto(
        Long id,
        Long walletId,
        EntryType entryType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String referenceId,
        String description,
        Instant createdAt
) {}
