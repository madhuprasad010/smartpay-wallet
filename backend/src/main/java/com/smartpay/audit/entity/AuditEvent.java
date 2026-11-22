package com.smartpay.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "actor_id", length = 64)
    private String actorId;

    @Column(name = "actor_type", length = 32)
    private String actorType; // USER, ADMIN, SYSTEM

    @Column(name = "resource_type", length = 64)
    private String resourceType; // WALLET, TRANSACTION, USER

    @Column(name = "resource_id", length = 64)
    private String resourceId;

    @Column(nullable = false, length = 32)
    private String action; // CREATE, UPDATE, DELETE, TRANSFER, LOGIN

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
