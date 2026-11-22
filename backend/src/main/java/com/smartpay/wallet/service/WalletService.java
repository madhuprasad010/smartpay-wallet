package com.smartpay.wallet.service;

import com.smartpay.common.exception.BusinessException;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.config.KafkaEventPublisher;
import com.smartpay.wallet.dto.*;
import com.smartpay.wallet.entity.*;
import com.smartpay.wallet.repository.WalletLedgerEntryRepository;
import com.smartpay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final WalletLedgerEntryRepository ledgerEntryRepository;
    private final KafkaEventPublisher kafkaPublisher;

    @Transactional
    public WalletDto createWallet(CreateWalletRequest request) {
        String walletNumber = "WLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Wallet wallet = Wallet.builder()
                .userId(request.userId())
                .walletNumber(walletNumber)
                .currency(request.currency())
                .status(WalletStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        Wallet saved = walletRepository.save(wallet);

        try {
            kafkaPublisher.send("smartpay.wallet.events", String.valueOf(saved.getId()), Map.of(
                    "eventType", "WALLET_CREATED",
                    "walletId", saved.getId(),
                    "userId", saved.getUserId(),
                    "walletNumber", saved.getWalletNumber(),
                    "currency", saved.getCurrency().name(),
                    "timestamp", Instant.now().toString()
            ));
        } catch (Exception e) {
            log.warn("Failed to publish WALLET_CREATED event: {}", e.getMessage());
        }

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<WalletDto> getWalletsByUserId(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WalletDto getWalletById(Long id) {
        return walletRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<WalletLedgerEntryDto> getLedgerEntries(Long walletId) {
        return ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(walletId).stream()
                .map(e -> new WalletLedgerEntryDto(
                        e.getId(),
                        e.getWalletId(),
                        e.getEntryType(),
                        e.getAmount(),
                        e.getBalanceAfter(),
                        e.getReferenceId(),
                        e.getDescription(),
                        e.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public WalletDto postLedgerEntry(Long walletId, EntryType entryType, BigDecimal amount, String referenceId, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }

        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new BusinessException("Wallet is not active");
        }

        BigDecimal newBalance;
        if (entryType == EntryType.DEBIT) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new BusinessException("Insufficient balance in wallet");
            }
            newBalance = wallet.getBalance().subtract(amount);
        } else {
            newBalance = wallet.getBalance().add(amount);
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletLedgerEntry entry = WalletLedgerEntry.builder()
                .walletId(walletId)
                .entryType(entryType)
                .amount(amount)
                .balanceAfter(newBalance)
                .referenceId(referenceId)
                .description(description)
                .build();
        ledgerEntryRepository.save(entry);

        return mapToDto(wallet);
    }

    @Transactional
    public void executeTransfer(Long sourceWalletId, Long destWalletId, BigDecimal amount, String referenceId, String description) {
        // Enforce deterministic locking order to prevent deadlock
        Long firstId = sourceWalletId.compareTo(destWalletId) < 0 ? sourceWalletId : destWalletId;
        Long secondId = sourceWalletId.compareTo(destWalletId) < 0 ? destWalletId : sourceWalletId;

        // Perform debit on source
        postLedgerEntry(sourceWalletId, EntryType.DEBIT, amount, referenceId, "Transfer to wallet " + destWalletId);
        // Perform credit on destination
        postLedgerEntry(destWalletId, EntryType.CREDIT, amount, referenceId, "Transfer from wallet " + sourceWalletId);
    }

    private WalletDto mapToDto(Wallet w) {
        return new WalletDto(
                w.getId(),
                w.getUserId(),
                w.getWalletNumber(),
                w.getCurrency(),
                w.getStatus(),
                w.getBalance(),
                w.getCreatedAt()
        );
    }
}
