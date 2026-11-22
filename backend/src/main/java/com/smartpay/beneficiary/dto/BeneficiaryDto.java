package com.smartpay.beneficiary.dto;

import java.time.Instant;

public record BeneficiaryDto(
        Long id,
        Long userId,
        String name,
        String accountNumber,
        String bankCode,
        String bankName,
        String email,
        String status,
        Instant createdAt
) {}
