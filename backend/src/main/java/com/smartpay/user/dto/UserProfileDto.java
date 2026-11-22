package com.smartpay.user.dto;

import java.time.LocalDate;

public record UserProfileDto(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String phone,
        String address,
        LocalDate dateOfBirth
) {}
