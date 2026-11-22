package com.smartpay.transaction.dto;

import com.smartpay.transaction.entity.TransactionStatus;
import com.smartpay.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDto(
        Long id,
        String transactionRef,
        String idempotencyKey,
        Long sourceWalletId,
        Long destWalletId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        TransactionType type,
        String description,
        Instant createdAt,
        Instant completedAt
) {}
