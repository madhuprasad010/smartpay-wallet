package com.smartpay.wallet.repository;

import com.smartpay.wallet.entity.WalletLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletLedgerEntryRepository extends JpaRepository<WalletLedgerEntry, Long> {
    List<WalletLedgerEntry> findByWalletIdOrderByCreatedAtDesc(Long walletId);
}
