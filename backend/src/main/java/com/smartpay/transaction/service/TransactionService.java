package com.smartpay.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpay.common.exception.BusinessException;
import com.smartpay.common.exception.ResourceNotFoundException;
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
    private final ObjectMapper objectMapper;

    @Transactional
    public TransactionDto processTransfer(TransferRequest request) {
        // 1. Idempotency check: return existing transaction if key was already processed
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            log.info("Idempotent hit for key: {}", request.idempotencyKey());
            return mapToDto(existing.get());
        }

        if (request.sourceWalletId().equals(request.destWalletId())) {
            throw new BusinessException("Source and destination wallet cannot be the same");
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be greater than zero");
        }

        String transactionRef = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // 2. Atomic double-entry transfer with pessimistic locking
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

        // 4. Transactional Outbox pattern
        try {
            Map<String, Object> eventData = Map.of(
                    "eventType", "TRANSACTION_COMPLETED",
                    "transactionRef", saved.getTransactionRef(),
                    "sourceWalletId", saved.getSourceWalletId(),
                    "destWalletId", saved.getDestWalletId(),
                    "amount", saved.getAmount(),
                    "currency", saved.getCurrency(),
                    "timestamp", Instant.now().toString()
            );

            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("TRANSACTION")
                    .aggregateId(saved.getTransactionRef())
                    .eventType("TRANSACTION_COMPLETED")
                    .payload(objectMapper.writeValueAsString(eventData))
                    .status("PUBLISHED")
                    .sentAt(Instant.now())
                    .build();
            outboxEventRepository.save(outbox);

            // 5. Optional Kafka dispatch
            kafkaPublisher.send("smartpay.transaction.events", saved.getTransactionRef(), eventData);
        } catch (Exception e) {
            log.warn("Outbox dispatch notification error: {}", e.getMessage());
        }

        return mapToDto(saved);
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
