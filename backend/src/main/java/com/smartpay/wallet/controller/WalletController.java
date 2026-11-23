package com.smartpay.wallet.controller;

import com.smartpay.common.dto.ApiResponse;
import com.smartpay.wallet.dto.*;
import com.smartpay.wallet.entity.EntryType;
import com.smartpay.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/number/{walletNumber}")
    public ResponseEntity<ApiResponse<WalletDto>> getWalletByNumber(@PathVariable String walletNumber) {
        WalletDto wallet = walletService.getWalletByNumber(walletNumber);
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

    @PostMapping("/{id}/freeze")
    public ResponseEntity<ApiResponse<WalletDto>> freezeWallet(@PathVariable Long id) {
        WalletDto wallet = walletService.freezeWallet(id);
        return ResponseEntity.ok(ApiResponse.success("Wallet frozen successfully", wallet));
    }

    @PostMapping("/{id}/unfreeze")
    public ResponseEntity<ApiResponse<WalletDto>> unfreezeWallet(@PathVariable Long id) {
        WalletDto wallet = walletService.unfreezeWallet(id);
        return ResponseEntity.ok(ApiResponse.success("Wallet unfrozen successfully", wallet));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<WalletDto>> activateWallet(@PathVariable Long id) {
        WalletDto wallet = walletService.activateWallet(id);
        return ResponseEntity.ok(ApiResponse.success("Wallet activated successfully", wallet));
    }

    @GetMapping("/{id}/limits")
    public ResponseEntity<ApiResponse<WalletLimitDto>> getLimits(@PathVariable Long id) {
        WalletLimitDto limits = walletService.getWalletLimits(id);
        return ResponseEntity.ok(ApiResponse.success(limits));
    }

    @PutMapping("/{id}/limits")
    public ResponseEntity<ApiResponse<WalletLimitDto>> updateLimits(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWalletLimitRequest request) {
        WalletLimitDto limits = walletService.updateWalletLimits(id, request);
        return ResponseEntity.ok(ApiResponse.success("Wallet limits updated successfully", limits));
    }

    @GetMapping("/{id}/reconcile")
    public ResponseEntity<ApiResponse<ReconciliationReportDto>> reconcileWallet(@PathVariable Long id) {
        ReconciliationReportDto report = walletService.reconcileWallet(id);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/reconcile-all")
    public ResponseEntity<ApiResponse<List<ReconciliationReportDto>>> reconcileAllWallets() {
        List<ReconciliationReportDto> reports = walletService.reconcileAllWallets();
        return ResponseEntity.ok(ApiResponse.success(reports));
    }
}
