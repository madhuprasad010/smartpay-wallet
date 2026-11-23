package com.smartpay.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpay.common.exception.BusinessException;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.common.service.IdempotencyService;
import com.smartpay.config.KafkaEventPublisher;
import com.smartpay.payment.dto.*;
import com.smartpay.payment.entity.*;
import com.smartpay.payment.repository.PaymentRepository;
import com.smartpay.payment.repository.PaymentWebhookEventRepository;
import com.smartpay.wallet.dto.WalletDto;
import com.smartpay.wallet.entity.EntryType;
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
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final WalletService walletService;
    private final KafkaEventPublisher kafkaPublisher;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Transactional
    public PaymentDto simulateDeposit(DepositRequest request) {
        String requestHash = idempotencyService.computeHash(request);
        Optional<PaymentDto> cached = idempotencyService.getExistingResponse(
                request.idempotencyKey(),
                requestHash,
                PaymentDto.class
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        WalletDto wallet = walletService.getWalletById(request.walletId());
        String paymentRef = "DEP-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        // Credit the wallet with simulated gateway clearance
        walletService.postLedgerEntry(
                wallet.id(),
                EntryType.CREDIT,
                request.amount(),
                paymentRef,
                "Simulated deposit (" + (request.paymentMethod() != null ? request.paymentMethod() : "NetBanking") + ")"
        );

        Payment payment = Payment.builder()
                .paymentRef(paymentRef)
                .userId(wallet.userId())
                .amount(request.amount())
                .currency(request.currency())
                .status(PaymentStatus.SUCCESS)
                .type("DEPOSIT")
                .provider(request.paymentMethod() != null ? request.paymentMethod() : "SIMULATED_GATEWAY")
                .description(request.description() != null ? request.description() : "Simulated bank deposit")
                .build();

        Payment saved = paymentRepository.save(payment);
        PaymentDto dto = mapToDto(saved);

        idempotencyService.recordSuccess(request.idempotencyKey(), requestHash, dto);

        try {
            kafkaPublisher.send("smartpay.payment.events.v1", saved.getPaymentRef(), Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", "DEPOSIT_COMPLETED",
                    "eventVersion", "v1",
                    "aggregateId", saved.getPaymentRef(),
                    "occurredAt", Instant.now().toString(),
                    "payload", Map.of(
                            "paymentRef", saved.getPaymentRef(),
                            "userId", saved.getUserId(),
                            "walletId", wallet.id(),
                            "amount", saved.getAmount(),
                            "currency", saved.getCurrency()
                    )
            ));
        } catch (Exception e) {
            log.warn("Failed to publish deposit event: {}", e.getMessage());
        }

        return dto;
    }

    @Transactional
    public PaymentDto simulateWithdrawal(WithdrawalRequest request) {
        String requestHash = idempotencyService.computeHash(request);
        Optional<PaymentDto> cached = idempotencyService.getExistingResponse(
                request.idempotencyKey(),
                requestHash,
                PaymentDto.class
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        WalletDto wallet = walletService.getWalletById(request.walletId());
        String paymentRef = "WTH-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        // Debit the wallet with simulated bank clearance
        walletService.postLedgerEntry(
                wallet.id(),
                EntryType.DEBIT,
                request.amount(),
                paymentRef,
                "Simulated withdrawal to A/C " + request.bankAccountNumber()
        );

        Payment payment = Payment.builder()
                .paymentRef(paymentRef)
                .userId(wallet.userId())
                .amount(request.amount())
                .currency(request.currency())
                .status(PaymentStatus.SUCCESS)
                .type("WITHDRAWAL")
                .provider("SIMULATED_IMPS")
                .description(request.description() != null ? request.description() : "Simulated bank payout")
                .build();

        Payment saved = paymentRepository.save(payment);
        PaymentDto dto = mapToDto(saved);

        idempotencyService.recordSuccess(request.idempotencyKey(), requestHash, dto);

        try {
            kafkaPublisher.send("smartpay.payment.events.v1", saved.getPaymentRef(), Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", "WITHDRAWAL_COMPLETED",
                    "eventVersion", "v1",
                    "aggregateId", saved.getPaymentRef(),
                    "occurredAt", Instant.now().toString(),
                    "payload", Map.of(
                            "paymentRef", saved.getPaymentRef(),
                            "userId", saved.getUserId(),
                            "walletId", wallet.id(),
                            "amount", saved.getAmount(),
                            "currency", saved.getCurrency()
                    )
            ));
        } catch (Exception e) {
            log.warn("Failed to publish withdrawal event: {}", e.getMessage());
        }

        return dto;
    }

    @Transactional
    public PaymentDto initiatePayment(PaymentInitiateRequest request) {
        String paymentRef = "PAY-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Payment payment = Payment.builder()
                .paymentRef(paymentRef)
                .userId(request.userId())
                .amount(request.amount())
                .currency(request.currency())
                .status(PaymentStatus.SUCCESS)
                .type(request.type() != null ? request.type() : "BILL_PAYMENT")
                .provider(request.provider() != null ? request.provider() : "SIMULATED_PROVIDER")
                .description(request.description())
                .build();

        Payment saved = paymentRepository.save(payment);

        try {
            kafkaPublisher.send("smartpay.payment.events.v1", saved.getPaymentRef(), Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", "PAYMENT_PROCESSED",
                    "eventVersion", "v1",
                    "aggregateId", saved.getPaymentRef(),
                    "occurredAt", Instant.now().toString(),
                    "payload", Map.of(
                            "paymentRef", saved.getPaymentRef(),
                            "userId", saved.getUserId(),
                            "amount", saved.getAmount(),
                            "currency", saved.getCurrency(),
                            "status", saved.getStatus().name()
                    )
            ));
        } catch (Exception e) {
            log.warn("Failed to publish payment event: {}", e.getMessage());
        }

        return mapToDto(saved);
    }

    @Transactional
    public PaymentDto simulateRefund(Long paymentId, String reason) {
        Payment original = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (original.getStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessException("Only successful payments can be refunded");
        }

        String refundRef = "RFD-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Payment refund = Payment.builder()
                .paymentRef(refundRef)
                .userId(original.getUserId())
                .amount(original.getAmount())
                .currency(original.getCurrency())
                .status(PaymentStatus.SUCCESS)
                .type("REFUND")
                .provider(original.getProvider())
                .description("Refund for " + original.getPaymentRef() + " (" + reason + ")")
                .build();

        Payment saved = paymentRepository.save(refund);

        // Update original payment status to REFUNDED
        original.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(original);

        return mapToDto(saved);
    }

    @Transactional
    public void processWebhook(PaymentWebhookRequest request) {
        try {
            String payloadJson = objectMapper.writeValueAsString(request.data());
            PaymentWebhookEvent webhook = PaymentWebhookEvent.builder()
                    .webhookId(request.webhookId())
                    .provider(request.provider())
                    .payload(payloadJson)
                    .signature(request.signature())
                    .status("PROCESSED")
                    .processedAt(Instant.now())
                    .build();
            webhookEventRepository.save(webhook);

            // If webhook updates a payment status
            paymentRepository.findByPaymentRef(request.paymentRef()).ifPresent(payment -> {
                if ("FAILED".equalsIgnoreCase(request.status())) {
                    payment.setStatus(PaymentStatus.FAILED);
                } else if ("SUCCESS".equalsIgnoreCase(request.status())) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                }
                paymentRepository.save(payment);
            });
        } catch (Exception e) {
            log.error("Failed to process webhook: {}", e.getMessage());
            throw new BusinessException("Webhook processing error: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getPaymentsByUser(Long userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::mapToDto);
    }

    private PaymentDto mapToDto(Payment p) {
        return new PaymentDto(
                p.getId(),
                p.getPaymentRef(),
                p.getUserId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getType(),
                p.getProvider(),
                p.getDescription(),
                p.getCreatedAt()
        );
    }
}
