package com.smartpay.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpay.common.entity.IdempotencyRecord;
import com.smartpay.common.exception.BusinessException;
import com.smartpay.common.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    public String computeHash(Object request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return String.valueOf(request.hashCode());
        }
    }

    @Transactional(readOnly = true)
    public <T> Optional<T> getExistingResponse(String key, String requestHash, Class<T> responseClass) {
        Optional<IdempotencyRecord> recordOpt = idempotencyRecordRepository.findByIdempotencyKey(key);
        if (recordOpt.isEmpty()) {
            return Optional.empty();
        }

        IdempotencyRecord record = recordOpt.get();
        if (!record.getRequestHash().equals(requestHash)) {
            throw new BusinessException("Idempotency key " + key + " was previously used with a different request payload.");
        }

        if (!"COMPLETED".equals(record.getStatus())) {
            throw new BusinessException("Transaction for key " + key + " is currently in progress or in failed state.");
        }

        try {
            return Optional.of(objectMapper.readValue(record.getResponseBody(), responseClass));
        } catch (Exception e) {
            throw new BusinessException("Failed to deserialize cached idempotent response");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String key, String requestHash, Object response) {
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(key)
                    .requestHash(requestHash)
                    .responseBody(responseJson)
                    .status("COMPLETED")
                    .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                    .build();
            idempotencyRecordRepository.save(record);
        } catch (Exception ignored) {
        }
    }
}
