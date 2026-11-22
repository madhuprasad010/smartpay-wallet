package com.smartpay.beneficiary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBeneficiaryRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Account number is required")
        String accountNumber,

        String bankCode,
        String bankName,
        String email
) {}
