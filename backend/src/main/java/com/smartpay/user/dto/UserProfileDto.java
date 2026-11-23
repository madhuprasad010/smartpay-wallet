package com.smartpay.user.dto;

import java.time.Instant;
import java.time.LocalDate;

public record UserProfileDto(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String phone,
        String address,
        LocalDate dateOfBirth,
        String kycStatus,
        String kycDocumentType,
        String kycDocumentNumber,
        Instant kycVerifiedAt
) {}
