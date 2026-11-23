package com.smartpay.user.dto;

import com.smartpay.auth.entity.Role;
import com.smartpay.auth.entity.UserStatus;

import java.time.Instant;

public record UserSummaryDto(
        Long id,
        String email,
        Role role,
        UserStatus status,
        String firstName,
        String lastName,
        String kycStatus,
        Instant createdAt
) {}
