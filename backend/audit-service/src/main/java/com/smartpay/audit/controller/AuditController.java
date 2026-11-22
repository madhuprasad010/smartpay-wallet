package com.smartpay.audit.controller;

import com.smartpay.audit.dto.ApiResponse;
import com.smartpay.audit.dto.AuditEventDto;
import com.smartpay.audit.dto.CreateAuditEventRequest;
import com.smartpay.audit.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuditEventDto>> recordEvent(@Valid @RequestBody CreateAuditEventRequest request) {
        AuditEventDto recorded = auditService.recordEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Audit event recorded", recorded));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditEventDto>>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        Page<AuditEventDto> page = auditService.getAuditEvents(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/actor/{actorId}")
    public ResponseEntity<ApiResponse<Page<AuditEventDto>>> getByActor(
            @PathVariable String actorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditEventDto> page = auditService.getByActorId(actorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
