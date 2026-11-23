package com.smartpay.wallet.service;

import com.smartpay.common.exception.BusinessException;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.config.KafkaEventPublisher;
import com.smartpay.wallet.dto.*;
import com.smartpay.wallet.entity.*;
import com.smartpay.wallet.repository.WalletLedgerEntryRepository;
import com.smartpay.wallet.repository.WalletLimitRepository;
import com.smartpay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final WalletLedgerEntryRepository ledgerEntryRepository;
    private final WalletLimitRepository walletLimitRepository;
    private final KafkaEventPublisher kafkaPublisher;

    @Transactional
    public WalletDto createWallet(CreateWalletRequest request) {
        String walletNumber = "WLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Wallet wallet = Wallet.builder()
                .userId(request.userId())
                .walletNumber(walletNumber)
                .currency(request.currency() != null ? request.currency() : Currency.INR)
                .status(WalletStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        Wallet saved = walletRepository.save(wallet);

        // Initialize default limits (100,000 daily, 50,000 per txn)
        WalletLimit limit = WalletLimit.builder()
                .walletId(saved.getId())
                .dailyLimit(new BigDecimal("100000.0000"))
                .perTransactionLimit(new BigDecimal("50000.0000"))
                .dailyUsed(BigDecimal.ZERO)
                .lastResetDate(LocalDate.now())
                .build();
        walletLimitRepository.save(limit);

        try {
            kafkaPublisher.send("smartpay.wallet.events.v1", String.valueOf(saved.getId()), Map.of(
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
    public WalletDto getWalletByNumber(String walletNumber) {
        return walletRepository.findByWalletNumber(walletNumber)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with number: " + walletNumber));
    }

    @Transactional
    public WalletDto freezeWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        wallet.setStatus(WalletStatus.FROZEN);
        Wallet saved = walletRepository.save(wallet);
        return mapToDto(saved);
    }

    @Transactional
    public WalletDto unfreezeWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        wallet.setStatus(WalletStatus.ACTIVE);
        Wallet saved = walletRepository.save(wallet);
        return mapToDto(saved);
    }

    @Transactional
    public WalletDto activateWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        wallet.setStatus(WalletStatus.ACTIVE);
        Wallet saved = walletRepository.save(wallet);
        return mapToDto(saved);
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
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }

        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new BusinessException("Wallet is not active (status: " + wallet.getStatus() + ")");
        }

        if (entryType == EntryType.DEBIT) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new BusinessException("Insufficient balance in wallet. Available: " + wallet.getBalance() + ", Requested: " + amount);
            }
            checkAndUpdateLimit(walletId, amount);
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else {
            wallet.setBalance(wallet.getBalance().add(amount));
        }

        walletRepository.save(wallet);

        WalletLedgerEntry entry = WalletLedgerEntry.builder()
                .walletId(walletId)
                .entryType(entryType)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .referenceId(referenceId)
                .description(description)
                .build();
        ledgerEntryRepository.save(entry);

        return mapToDto(wallet);
    }

    @Transactional
    public void executeTransfer(Long sourceWalletId, Long destWalletId, BigDecimal amount, String referenceId, String description) {
        if (sourceWalletId.equals(destWalletId)) {
            throw new BusinessException("Cannot transfer money to the same wallet");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be strictly greater than zero");
        }

        // Enforce deterministic locking order to prevent deadlocks under high concurrency
        Long firstId = sourceWalletId.compareTo(destWalletId) < 0 ? sourceWalletId : destWalletId;
        Long secondId = sourceWalletId.compareTo(destWalletId) < 0 ? destWalletId : sourceWalletId;

        Wallet firstWallet = walletRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + firstId));
        Wallet secondWallet = walletRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + secondId));

        Wallet source = sourceWalletId.equals(firstId) ? firstWallet : secondWallet;
        Wallet dest = destWalletId.equals(firstId) ? firstWallet : secondWallet;

        if (source.getStatus() != WalletStatus.ACTIVE) {
            throw new BusinessException("Source wallet is not active (status: " + source.getStatus() + ")");
        }
        if (dest.getStatus() != WalletStatus.ACTIVE) {
            throw new BusinessException("Destination wallet is not active (status: " + dest.getStatus() + ")");
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance. Available: " + source.getBalance() + ", Requested: " + amount);
        }

        // Limit verification and update on source wallet
        checkAndUpdateLimit(sourceWalletId, amount);

        // Perform balance updates
        BigDecimal newSourceBalance = source.getBalance().subtract(amount);
        source.setBalance(newSourceBalance);
        walletRepository.save(source);

        BigDecimal newDestBalance = dest.getBalance().add(amount);
        dest.setBalance(newDestBalance);
        walletRepository.save(dest);

        // Balanced double-entry ledger entries
        WalletLedgerEntry debitEntry = WalletLedgerEntry.builder()
                .walletId(sourceWalletId)
                .entryType(EntryType.DEBIT)
                .amount(amount)
                .balanceAfter(newSourceBalance)
                .referenceId(referenceId)
                .description("Transfer to wallet #" + dest.getWalletNumber() + " (" + description + ")")
                .build();
        ledgerEntryRepository.save(debitEntry);

        WalletLedgerEntry creditEntry = WalletLedgerEntry.builder()
                .walletId(destWalletId)
                .entryType(EntryType.CREDIT)
                .amount(amount)
                .balanceAfter(newDestBalance)
                .referenceId(referenceId)
                .description("Transfer from wallet #" + source.getWalletNumber() + " (" + description + ")")
                .build();
        ledgerEntryRepository.save(creditEntry);
    }

    @Transactional
    public void executeCompensatingReversal(Long sourceWalletId, Long destWalletId, BigDecimal amount, String originalRef, String reversalRef, String reason) {
        // Enforce deterministic locking order
        Long firstId = sourceWalletId.compareTo(destWalletId) < 0 ? sourceWalletId : destWalletId;
        Long secondId = sourceWalletId.compareTo(destWalletId) < 0 ? destWalletId : sourceWalletId;

        Wallet firstWallet = walletRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + firstId));
        Wallet secondWallet = walletRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + secondId));

        Wallet originalSource = sourceWalletId.equals(firstId) ? firstWallet : secondWallet;
        Wallet originalDest = destWalletId.equals(firstId) ? firstWallet : secondWallet;

        if (originalDest.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Cannot reverse transaction: destination wallet does not have sufficient balance for reversal");
        }

        // Compensating entries: debit originalDest, credit originalSource
        originalDest.setBalance(originalDest.getBalance().subtract(amount));
        walletRepository.save(originalDest);

        originalSource.setBalance(originalSource.getBalance().add(amount));
        walletRepository.save(originalSource);

        WalletLedgerEntry revDebit = WalletLedgerEntry.builder()
                .walletId(destWalletId)
                .entryType(EntryType.DEBIT)
                .amount(amount)
                .balanceAfter(originalDest.getBalance())
                .referenceId(reversalRef)
                .description("Compensating Reversal for " + originalRef + ": " + reason)
                .build();
        ledgerEntryRepository.save(revDebit);

        WalletLedgerEntry revCredit = WalletLedgerEntry.builder()
                .walletId(sourceWalletId)
                .entryType(EntryType.CREDIT)
                .amount(amount)
                .balanceAfter(originalSource.getBalance())
                .referenceId(reversalRef)
                .description("Compensating Reversal Credit for " + originalRef + ": " + reason)
                .build();
        ledgerEntryRepository.save(revCredit);
    }

    @Transactional(readOnly = true)
    public WalletLimitDto getWalletLimits(Long walletId) {
        WalletLimit limit = walletLimitRepository.findByWalletId(walletId)
                .orElseGet(() -> WalletLimit.builder()
                        .walletId(walletId)
                        .dailyLimit(new BigDecimal("100000.0000"))
                        .perTransactionLimit(new BigDecimal("50000.0000"))
                        .dailyUsed(BigDecimal.ZERO)
                        .lastResetDate(LocalDate.now())
                        .build());

        BigDecimal dailyUsed = limit.getLastResetDate().equals(LocalDate.now()) ? limit.getDailyUsed() : BigDecimal.ZERO;
        BigDecimal remaining = limit.getDailyLimit().subtract(dailyUsed).max(BigDecimal.ZERO);

        return new WalletLimitDto(
                limit.getId(),
                walletId,
                limit.getDailyLimit(),
                limit.getPerTransactionLimit(),
                dailyUsed,
                remaining,
                limit.getLastResetDate()
        );
    }

    @Transactional
    public WalletLimitDto updateWalletLimits(Long walletId, UpdateWalletLimitRequest request) {
        WalletLimit limit = walletLimitRepository.findByWalletId(walletId)
                .orElseGet(() -> WalletLimit.builder().walletId(walletId).build());

        limit.setDailyLimit(request.dailyLimit());
        limit.setPerTransactionLimit(request.perTransactionLimit());
        WalletLimit saved = walletLimitRepository.save(limit);

        return getWalletLimits(walletId);
    }

    private void checkAndUpdateLimit(Long walletId, BigDecimal amount) {
        WalletLimit limit = walletLimitRepository.findByWalletId(walletId)
                .orElseGet(() -> {
                    WalletLimit nl = WalletLimit.builder()
                            .walletId(walletId)
                            .dailyLimit(new BigDecimal("100000.0000"))
                            .perTransactionLimit(new BigDecimal("50000.0000"))
                            .dailyUsed(BigDecimal.ZERO)
                            .lastResetDate(LocalDate.now())
                            .build();
                    return walletLimitRepository.save(nl);
                });

        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw new BusinessException("Transaction amount " + amount + " exceeds per-transaction limit of " + limit.getPerTransactionLimit());
        }

        LocalDate today = LocalDate.now();
        BigDecimal currentUsed = limit.getLastResetDate().equals(today) ? limit.getDailyUsed() : BigDecimal.ZERO;

        if (currentUsed.add(amount).compareTo(limit.getDailyLimit()) > 0) {
            throw new BusinessException("Transaction would exceed daily transfer limit of " + limit.getDailyLimit() + ". Current daily used: " + currentUsed);
        }

        limit.setDailyUsed(currentUsed.add(amount));
        limit.setLastResetDate(today);
        walletLimitRepository.save(limit);
    }

    @Transactional(readOnly = true)
    public ReconciliationReportDto reconcileWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));

        List<WalletLedgerEntry> entries = ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(walletId);

        BigDecimal calculated = BigDecimal.ZERO;
        for (WalletLedgerEntry entry : entries) {
            if (entry.getEntryType() == EntryType.CREDIT) {
                calculated = calculated.add(entry.getAmount());
            } else if (entry.getEntryType() == EntryType.DEBIT) {
                calculated = calculated.subtract(entry.getAmount());
            }
        }

        BigDecimal discrepancy = wallet.getBalance().subtract(calculated);
        boolean isBalanced = discrepancy.compareTo(BigDecimal.ZERO) == 0;

        return new ReconciliationReportDto(
                wallet.getId(),
                wallet.getWalletNumber(),
                wallet.getBalance(),
                calculated,
                discrepancy,
                isBalanced,
                entries.size(),
                Instant.now()
        );
    }

    @Transactional(readOnly = true)
    public List<ReconciliationReportDto> reconcileAllWallets() {
        List<Wallet> wallets = walletRepository.findAll();
        List<ReconciliationReportDto> reports = new ArrayList<>();
        for (Wallet w : wallets) {
            reports.add(reconcileWallet(w.getId()));
        }
        return reports;
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
