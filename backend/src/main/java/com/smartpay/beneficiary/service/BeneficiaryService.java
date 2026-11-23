package com.smartpay.beneficiary.service;

import com.smartpay.beneficiary.dto.*;
import com.smartpay.beneficiary.entity.Beneficiary;
import com.smartpay.beneficiary.repository.BeneficiaryRepository;
import com.smartpay.common.exception.BusinessException;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.transaction.dto.TransactionDto;
import com.smartpay.transaction.dto.TransferRequest;
import com.smartpay.transaction.service.TransactionService;
import com.smartpay.wallet.dto.WalletDto;
import com.smartpay.wallet.entity.EntryType;
import com.smartpay.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final WalletService walletService;
    private final TransactionService transactionService;

    @Transactional
    public BeneficiaryDto createBeneficiary(CreateBeneficiaryRequest request) {
        Beneficiary beneficiary = Beneficiary.builder()
                .userId(request.userId())
                .name(request.name().trim())
                .accountNumber(request.accountNumber().trim())
                .bankCode(request.bankCode())
                .bankName(request.bankName() != null ? request.bankName().trim() : "SmartPay Partner Bank")
                .email(request.email())
                .status("VERIFIED")
                .build();

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return mapToDto(saved);
    }

    @Transactional
    public BeneficiaryDto updateBeneficiary(Long id, UpdateBeneficiaryRequest request) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id: " + id));

        beneficiary.setName(request.name().trim());
        beneficiary.setAccountNumber(request.accountNumber().trim());
        beneficiary.setBankCode(request.bankCode());
        if (request.bankName() != null) {
            beneficiary.setBankName(request.bankName().trim());
        }
        beneficiary.setEmail(request.email());

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return mapToDto(saved);
    }

    @Transactional
    public BeneficiaryDto toggleStatus(Long id, String status) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id: " + id));

        beneficiary.setStatus(status.toUpperCase());
        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryDto> getByUserId(Long userId) {
        return beneficiaryRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BeneficiaryDto getById(Long id) {
        return beneficiaryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id: " + id));
    }

    @Transactional
    public void deleteBeneficiary(Long id) {
        if (!beneficiaryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Beneficiary not found with id: " + id);
        }
        beneficiaryRepository.deleteById(id);
    }

    @Transactional
    public TransactionDto transferToBeneficiary(Long beneficiaryId, BeneficiaryTransferRequest request) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id: " + beneficiaryId));

        if (!"VERIFIED".equalsIgnoreCase(beneficiary.getStatus()) && !"ACTIVE".equalsIgnoreCase(beneficiary.getStatus())) {
            throw new BusinessException("Beneficiary is inactive (status: " + beneficiary.getStatus() + ")");
        }

        // Check if beneficiary has a SmartPay wallet number (e.g. WLT-XXXX)
        if (beneficiary.getAccountNumber().startsWith("WLT-")) {
            WalletDto destWallet = walletService.getWalletByNumber(beneficiary.getAccountNumber());
            TransferRequest transferRequest = new TransferRequest(
                    request.idempotencyKey(),
                    request.sourceWalletId(),
                    destWallet.id(),
                    request.amount(),
                    request.currency(),
                    request.description() != null ? request.description() : "Transfer to " + beneficiary.getName()
            );
            return transactionService.processTransfer(transferRequest);
        } else {
            // Simulated external bank payout via ledger debit
            String payoutRef = "PAYOUT-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            walletService.postLedgerEntry(
                    request.sourceWalletId(),
                    EntryType.DEBIT,
                    request.amount(),
                    payoutRef,
                    "Payout to beneficiary " + beneficiary.getName() + " (A/C: " + beneficiary.getAccountNumber() + ")"
            );

            return new TransactionDto(
                    null,
                    payoutRef,
                    request.idempotencyKey(),
                    request.sourceWalletId(),
                    null,
                    request.amount(),
                    request.currency(),
                    com.smartpay.transaction.entity.TransactionStatus.COMPLETED,
                    com.smartpay.transaction.entity.TransactionType.PAYMENT,
                    "External payout to " + beneficiary.getName() + " (" + beneficiary.getBankName() + ")",
                    java.time.Instant.now(),
                    java.time.Instant.now()
            );
        }
    }

    private BeneficiaryDto mapToDto(Beneficiary b) {
        return new BeneficiaryDto(
                b.getId(),
                b.getUserId(),
                b.getName(),
                b.getAccountNumber(),
                b.getBankCode(),
                b.getBankName(),
                b.getEmail(),
                b.getStatus(),
                b.getCreatedAt()
        );
    }
}
