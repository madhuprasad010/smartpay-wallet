package com.smartpay.audit.service;

import com.smartpay.audit.dto.AuditEventDto;
import com.smartpay.audit.dto.CreateAuditEventRequest;
import com.smartpay.audit.entity.AuditEvent;
import com.smartpay.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    @Transactional
    public AuditEventDto recordEvent(CreateAuditEventRequest request) {
        AuditEvent event = AuditEvent.builder()
                .eventType(request.eventType())
                .actorId(request.actorId())
                .actorType(request.actorType() != null ? request.actorType() : "SYSTEM")
                .resourceType(request.resourceType())
                .resourceId(request.resourceId())
                .action(request.action())
                .details(request.details())
                .ipAddress(request.ipAddress())
                .correlationId(request.correlationId())
                .build();

        AuditEvent saved = auditEventRepository.save(event);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDto> getAuditEvents(Pageable pageable) {
        return auditEventRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDto> getByActorId(String actorId, Pageable pageable) {
        return auditEventRepository.findByActorIdOrderByCreatedAtDesc(actorId, pageable).map(this::mapToDto);
    }

    private AuditEventDto mapToDto(AuditEvent e) {
        return new AuditEventDto(
                e.getId(),
                e.getEventType(),
                e.getActorId(),
                e.getActorType(),
                e.getResourceType(),
                e.getResourceId(),
                e.getAction(),
                e.getDetails(),
                e.getIpAddress(),
                e.getCorrelationId(),
                e.getCreatedAt()
        );
    }
}
