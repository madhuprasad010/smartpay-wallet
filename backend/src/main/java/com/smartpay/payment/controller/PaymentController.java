package com.smartpay.payment.controller;

import com.smartpay.common.dto.ApiResponse;
import com.smartpay.payment.dto.*;
import com.smartpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<PaymentDto>> deposit(@Valid @RequestBody DepositRequest request) {
        PaymentDto payment = paymentService.simulateDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit processed successfully (Simulated)", payment));
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<ApiResponse<PaymentDto>> withdrawal(@Valid @RequestBody WithdrawalRequest request) {
        PaymentDto payment = paymentService.simulateWithdrawal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal processed successfully (Simulated)", payment));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDto>> initiate(@Valid @RequestBody PaymentInitiateRequest request) {
        PaymentDto payment = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated successfully (Simulated)", payment));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentDto>> refund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        PaymentDto refund = paymentService.simulateRefund(id, request.reason());
        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully (Simulated)", refund));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@Valid @RequestBody PaymentWebhookRequest request) {
        paymentService.processWebhook(request);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getById(@PathVariable Long id) {
        PaymentDto payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentDto> payments = paymentService.getPaymentsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentDto> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
