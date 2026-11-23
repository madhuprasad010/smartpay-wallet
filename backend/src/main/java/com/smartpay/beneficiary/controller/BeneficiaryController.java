package com.smartpay.beneficiary.controller;

import com.smartpay.beneficiary.dto.*;
import com.smartpay.beneficiary.service.BeneficiaryService;
import com.smartpay.common.dto.ApiResponse;
import com.smartpay.transaction.dto.TransactionDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryDto>> create(@Valid @RequestBody CreateBeneficiaryRequest request) {
        BeneficiaryDto beneficiary = beneficiaryService.createBeneficiary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Beneficiary added successfully", beneficiary));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BeneficiaryDto>>> getByUser(@PathVariable Long userId) {
        List<BeneficiaryDto> beneficiaries = beneficiaryService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(beneficiaries));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BeneficiaryDto>> getById(@PathVariable Long id) {
        BeneficiaryDto beneficiary = beneficiaryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(beneficiary));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BeneficiaryDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBeneficiaryRequest request) {
        BeneficiaryDto updated = beneficiaryService.updateBeneficiary(id, request);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(id);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BeneficiaryDto>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        BeneficiaryDto updated = beneficiaryService.toggleStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary status updated", updated));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<ApiResponse<TransactionDto>> transferToBeneficiary(
            @PathVariable Long id,
            @Valid @RequestBody BeneficiaryTransferRequest request) {
        TransactionDto txn = beneficiaryService.transferToBeneficiary(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Transfer to beneficiary executed successfully", txn));
    }
}
