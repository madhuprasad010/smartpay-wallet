package com.smartpay.payment.controller;

import com.smartpay.payment.dto.ApiResponse;
import com.smartpay.payment.dto.PaymentDto;
import com.smartpay.payment.dto.PaymentInitiateRequest;
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

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDto>> initiate(@Valid @RequestBody PaymentInitiateRequest request) {
        PaymentDto payment = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Payment initiated successfully", payment));
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
}
