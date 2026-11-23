package com.smartpay.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpay.common.exception.BusinessException;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.common.service.IdempotencyService;
import com.smartpay.config.KafkaEventPublisher;
import com.smartpay.transaction.dto.TransactionDto;
import com.smartpay.transaction.dto.TransferRequest;
import com.smartpay.transaction.entity.*;
import com.smartpay.transaction.repository.OutboxEventRepository;
import com.smartpay.transaction.repository.TransactionRepository;
import com.smartpay.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final WalletService walletService;
    private final KafkaEventPublisher kafkaPublisher;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Transactional
    public TransactionDto processTransfer(TransferRequest request) {
        String requestHash = idempotencyService.computeHash(request);

        // 1. Check idempotency record
        Optional<TransactionDto> cached = idempotencyService.getExistingResponse(
                request.idempotencyKey(),
                requestHash,
                TransactionDto.class
        );
        if (cached.isPresent()) {
            log.info("Idempotency hit for key: {}", request.idempotencyKey());
            return cached.get();
        }

        // Also check transaction table for idempotency key
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            log.info("Idempotent hit in transaction table for key: {}", request.idempotencyKey());
            TransactionDto dto = mapToDto(existing.get());
            idempotencyService.recordSuccess(request.idempotencyKey(), requestHash, dto);
            return dto;
        }

        if (request.sourceWalletId().equals(request.destWalletId())) {
            throw new BusinessException("Source and destination wallet cannot be the same");
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be strictly greater than zero");
        }

        String transactionRef = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // 2. Atomic double-entry transfer with deterministic locking & limit checks
        walletService.executeTransfer(
                request.sourceWalletId(),
                request.destWalletId(),
                request.amount(),
                transactionRef,
                request.description() != null ? request.description() : "Peer-to-peer transfer"
        );

        // 3. Record completed transaction
        Transaction txn = Transaction.builder()
                .transactionRef(transactionRef)
                .idempotencyKey(request.idempotencyKey())
                .sourceWalletId(request.sourceWalletId())
                .destWalletId(request.destWalletId())
                .amount(request.amount())
                .currency(request.currency())
                .status(TransactionStatus.COMPLETED)
                .type(TransactionType.TRANSFER)
                .description(request.description())
                .completedAt(Instant.now())
                .build();

        Transaction saved = transactionRepository.save(txn);
        TransactionDto responseDto = mapToDto(saved);

        // Record in idempotency registry
        idempotencyService.recordSuccess(request.idempotencyKey(), requestHash, responseDto);

        // 4. Transactional Outbox pattern
        try {
            Map<String, Object> eventData = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", "TRANSACTION_COMPLETED",
                    "eventVersion", "v1",
                    "aggregateId", saved.getTransactionRef(),
                    "occurredAt", Instant.now().toString(),
                    "correlationId", UUID.randomUUID().toString(),
                    "payload", Map.of(
                            "transactionRef", saved.getTransactionRef(),
                            "sourceWalletId", saved.getSourceWalletId(),
                            "destWalletId", saved.getDestWalletId(),
                            "amount", saved.getAmount(),
                            "currency", saved.getCurrency()
                    )
            );

            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("TRANSACTION")
                    .aggregateId(saved.getTransactionRef())
                    .eventType("TRANSACTION_COMPLETED")
                    .payload(objectMapper.writeValueAsString(eventData))
                    .status("PENDING") // Processed by Outbox Poller
                    .build();
            outboxEventRepository.save(outbox);

            // Immediate optional dispatch attempt
            kafkaPublisher.send("smartpay.transaction.events.v1", saved.getTransactionRef(), eventData);
        } catch (Exception e) {
            log.warn("Outbox dispatch notification error: {}", e.getMessage());
        }

        return responseDto;
    }

    @Transactional
    public TransactionDto reverseTransaction(String transactionRef, String reason) {
        Transaction original = transactionRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ref: " + transactionRef));

        if (original.getStatus() != TransactionStatus.COMPLETED) {
            throw new BusinessException("Cannot reverse transaction in status: " + original.getStatus());
        }
        if (original.getType() != TransactionType.TRANSFER) {
            throw new BusinessException("Only peer-to-peer transfers can be reversed through this operation");
        }

        String reversalRef = "REV-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // Execute compensating double-entry debit & credit
        walletService.executeCompensatingReversal(
                original.getSourceWalletId(),
                original.getDestWalletId(),
                original.getAmount(),
                original.getTransactionRef(),
                reversalRef,
                reason
        );

        // Create reversal transaction entry
        Transaction revTxn = Transaction.builder()
                .transactionRef(reversalRef)
                .idempotencyKey("IDEMP-" + reversalRef)
                .sourceWalletId(original.getDestWalletId())
                .destWalletId(original.getSourceWalletId())
                .amount(original.getAmount())
                .currency(original.getCurrency())
                .status(TransactionStatus.COMPLETED)
                .type(TransactionType.REVERSAL)
                .description("Reversal of " + original.getTransactionRef() + " (" + reason + ")")
                .completedAt(Instant.now())
                .build();

        Transaction savedReversal = transactionRepository.save(revTxn);

        // Mark original as reversed
        original.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(original);

        return mapToDto(savedReversal);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionByRef(String ref) {
        return transactionRepository.findByTransactionRef(ref)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ref: " + ref));
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactions(Long walletId, Pageable pageable) {
        if (walletId != null) {
            return transactionRepository.findByWalletId(walletId, pageable).map(this::mapToDto);
        }
        return transactionRepository.findAll(pageable).map(this::mapToDto);
    }

    private TransactionDto mapToDto(Transaction t) {
        return new TransactionDto(
                t.getId(),
                t.getTransactionRef(),
                t.getIdempotencyKey(),
                t.getSourceWalletId(),
                t.getDestWalletId(),
                t.getAmount(),
                t.getCurrency(),
                t.getStatus(),
                t.getType(),
                t.getDescription(),
                t.getCreatedAt(),
                t.getCompletedAt()
        );
    }
}
