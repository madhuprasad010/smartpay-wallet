package com.smartpay.audit.repository;

import com.smartpay.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    Page<AuditEvent> findByActorIdOrderByCreatedAtDesc(String actorId, Pageable pageable);
    Page<AuditEvent> findByResourceTypeOrderByCreatedAtDesc(String resourceType, Pageable pageable);
}
