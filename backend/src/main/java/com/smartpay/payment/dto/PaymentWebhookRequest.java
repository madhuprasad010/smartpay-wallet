package com.smartpay.payment.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record PaymentWebhookRequest(
        @NotBlank(message = "Webhook ID is required")
        String webhookId,

        @NotBlank(message = "Provider is required")
        String provider,

        @NotBlank(message = "Payment reference is required")
        String paymentRef,

        @NotBlank(message = "Status is required")
        String status,

        String signature,

        Map<String, Object> data
) {}
