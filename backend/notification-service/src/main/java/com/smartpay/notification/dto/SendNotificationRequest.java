package com.smartpay.notification.dto;

import com.smartpay.notification.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "Type is required")
        String type,

        @NotNull(message = "Channel is required")
        NotificationChannel channel,

        @NotBlank(message = "Subject is required")
        String subject,

        @NotBlank(message = "Body is required")
        String body
) {}
