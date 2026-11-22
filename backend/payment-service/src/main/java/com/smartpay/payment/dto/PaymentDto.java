package com.smartpay.payment.dto;

import com.smartpay.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDto(
        Long id,
        String paymentRef,
        Long userId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String type,
        String provider,
        String description,
        Instant createdAt
) {}
