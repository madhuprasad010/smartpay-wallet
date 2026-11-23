package com.smartpay.beneficiary.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBeneficiaryRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Account number is required")
        String accountNumber,

        String bankCode,
        String bankName,
        String email
) {}
