package com.smartpay.user.dto;

import java.time.LocalDate;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String phone,
        String address,
        LocalDate dateOfBirth
) {}
