package com.smartpay.beneficiary.service;

import com.smartpay.beneficiary.dto.BeneficiaryDto;
import com.smartpay.beneficiary.dto.CreateBeneficiaryRequest;
import com.smartpay.beneficiary.entity.Beneficiary;
import com.smartpay.beneficiary.exception.ResourceNotFoundException;
import com.smartpay.beneficiary.repository.BeneficiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    @Transactional
    public BeneficiaryDto createBeneficiary(CreateBeneficiaryRequest request) {
        Beneficiary beneficiary = Beneficiary.builder()
                .userId(request.userId())
                .name(request.name())
                .accountNumber(request.accountNumber())
                .bankCode(request.bankCode())
                .bankName(request.bankName() != null ? request.bankName() : "SmartPay Bank")
                .email(request.email())
                .status("VERIFIED")
                .build();

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
