package com.smartpay.wallet.controller;

import com.smartpay.common.dto.ApiResponse;
import com.smartpay.wallet.dto.*;
import com.smartpay.wallet.entity.EntryType;
import com.smartpay.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletDto>> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        WalletDto wallet = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Wallet created successfully", wallet));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WalletDto>>> getWalletsByUser(@PathVariable Long userId) {
        List<WalletDto> wallets = walletService.getWalletsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(wallets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WalletDto>> getWallet(@PathVariable Long id) {
        WalletDto wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    @GetMapping("/{id}/ledger")
    public ResponseEntity<ApiResponse<List<WalletLedgerEntryDto>>> getLedger(@PathVariable Long id) {
        List<WalletLedgerEntryDto> entries = walletService.getLedgerEntries(id);
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<ApiResponse<WalletDto>> creditWallet(
            @PathVariable Long id,
            @Valid @RequestBody CreditWalletRequest request) {
        WalletDto wallet = walletService.postLedgerEntry(id, EntryType.CREDIT, request.amount(), request.referenceId(), request.description());
        return ResponseEntity.ok(ApiResponse.success("Wallet credited successfully", wallet));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<ApiResponse<WalletDto>> debitWallet(
            @PathVariable Long id,
            @Valid @RequestBody CreditWalletRequest request) {
        WalletDto wallet = walletService.postLedgerEntry(id, EntryType.DEBIT, request.amount(), request.referenceId(), request.description());
        return ResponseEntity.ok(ApiResponse.success("Wallet debited successfully", wallet));
    }
}
