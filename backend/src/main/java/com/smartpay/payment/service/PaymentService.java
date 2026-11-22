package com.smartpay.payment.service;

import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.config.KafkaEventPublisher;
import com.smartpay.payment.dto.PaymentDto;
import com.smartpay.payment.dto.PaymentInitiateRequest;
import com.smartpay.payment.entity.Payment;
import com.smartpay.payment.entity.PaymentStatus;
import com.smartpay.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final KafkaEventPublisher kafkaPublisher;

    @Transactional
    public PaymentDto initiatePayment(PaymentInitiateRequest request) {
        String paymentRef = "PAY-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Payment payment = Payment.builder()
                .paymentRef(paymentRef)
                .userId(request.userId())
                .amount(request.amount())
                .currency(request.currency())
                .status(PaymentStatus.SUCCESS) // Simulated synchronous provider clearance
                .type(request.type() != null ? request.type() : "BILL_PAYMENT")
                .provider(request.provider() != null ? request.provider() : "INTERNAL")
                .description(request.description())
                .build();

        Payment saved = paymentRepository.save(payment);

        try {
            kafkaPublisher.send("smartpay.payment.events", saved.getPaymentRef(), Map.of(
                    "eventType", "PAYMENT_PROCESSED",
                    "paymentRef", saved.getPaymentRef(),
                    "userId", saved.getUserId(),
                    "amount", saved.getAmount(),
                    "currency", saved.getCurrency(),
                    "status", saved.getStatus().name(),
                    "timestamp", Instant.now().toString()
            ));
        } catch (Exception e) {
            log.warn("Failed to publish payment event: {}", e.getMessage());
        }

        return mapToDto(saved);
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
