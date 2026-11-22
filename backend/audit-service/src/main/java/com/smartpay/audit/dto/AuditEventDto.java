package com.smartpay.audit.dto;

import java.time.Instant;

public record AuditEventDto(
        Long id,
        String eventType,
        String actorId,
        String actorType,
        String resourceType,
        String resourceId,
        String action,
        String details,
        String ipAddress,
        String correlationId,
        Instant createdAt
) {}
