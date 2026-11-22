package com.smartpay.audit.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAuditEventRequest(
        @NotBlank(message = "Event type is required")
        String eventType,

        String actorId,
        String actorType,
        String resourceType,
        String resourceId,

        @NotBlank(message = "Action is required")
        String action,

        String details,
        String ipAddress,
        String correlationId
) {}
