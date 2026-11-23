package com.smartpay.user.dto;

import jakarta.validation.constraints.NotBlank;

public record KycSubmitRequest(
        @NotBlank(message = "Document type is required")
        String documentType,

        @NotBlank(message = "Document number is required")
        String documentNumber
) {}
