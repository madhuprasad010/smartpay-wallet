package com.smartpay.transaction.controller;

import com.smartpay.common.dto.ApiResponse;
import com.smartpay.transaction.dto.TransactionDto;
import com.smartpay.transaction.dto.TransferRequest;
import com.smartpay.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionDto>> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionDto result = transactionService.processTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Transfer processed successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDto>> getById(@PathVariable Long id) {
        TransactionDto txn = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(txn));
    }

    @GetMapping("/ref/{ref}")
    public ResponseEntity<ApiResponse<TransactionDto>> getByRef(@PathVariable String ref) {
        TransactionDto txn = transactionService.getTransactionByRef(ref);
        return ResponseEntity.ok(ApiResponse.success(txn));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> getAll(
            @RequestParam(required = false) Long walletId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TransactionDto> page = transactionService.getTransactions(walletId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
