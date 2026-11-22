package com.smartpay.beneficiary.controller;

import com.smartpay.beneficiary.dto.BeneficiaryDto;
import com.smartpay.beneficiary.dto.CreateBeneficiaryRequest;
import com.smartpay.beneficiary.service.BeneficiaryService;
import com.smartpay.common.dto.ApiResponse;
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
        BeneficiaryDto created = beneficiaryService.createBeneficiary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Beneficiary added successfully", created));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BeneficiaryDto>>> getByUser(@PathVariable Long userId) {
        List<BeneficiaryDto> list = beneficiaryService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BeneficiaryDto>> getById(@PathVariable Long id) {
        BeneficiaryDto b = beneficiaryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(b));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(id);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary removed successfully", null));
    }
}
