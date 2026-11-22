package com.smartpay.notification.dto;

import com.smartpay.notification.entity.NotificationChannel;
import com.smartpay.notification.entity.NotificationStatus;

import java.time.Instant;

public record NotificationDto(
        Long id,
        Long userId,
        String type,
        NotificationChannel channel,
        String subject,
        String body,
        NotificationStatus status,
        Instant createdAt,
        Instant sentAt
) {}
