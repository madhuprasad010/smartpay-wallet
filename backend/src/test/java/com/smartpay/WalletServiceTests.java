package com.smartpay;

import com.smartpay.common.exception.BusinessException;
import com.smartpay.wallet.dto.CreateWalletRequest;
import com.smartpay.wallet.dto.WalletDto;
import com.smartpay.wallet.dto.WalletLedgerEntryDto;
import com.smartpay.wallet.entity.Currency;
import com.smartpay.wallet.entity.EntryType;
import com.smartpay.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WalletServiceTests {

    @Autowired
    private WalletService walletService;

    @Test
    @DisplayName("Should create active wallet with zero initial balance")
    void testCreateWallet() {
        WalletDto wallet = walletService.createWallet(new CreateWalletRequest(101L, Currency.USD));

        assertNotNull(wallet);
        assertNotNull(wallet.id());
        assertTrue(wallet.walletNumber().startsWith("WLT-"));
        assertEquals(Currency.USD, wallet.currency());
        assertEquals(BigDecimal.ZERO, wallet.balance());
    }

    @Test
    @DisplayName("Should credit wallet and create immutable double-entry ledger entry")
    void testCreditWallet() {
        WalletDto wallet = walletService.createWallet(new CreateWalletRequest(102L, Currency.USD));

        BigDecimal depositAmount = new BigDecimal("500.00");
        WalletDto updated = walletService.postLedgerEntry(
                wallet.id(),
                EntryType.CREDIT,
                depositAmount,
                "DEP-TEST-001",
                "Test initial deposit"
        );

        assertEquals(new BigDecimal("500.00"), updated.balance());

        List<WalletLedgerEntryDto> ledger = walletService.getLedgerEntries(wallet.id());
        assertFalse(ledger.isEmpty());
        WalletLedgerEntryDto entry = ledger.getFirst();
        assertEquals(EntryType.CREDIT, entry.entryType());
        assertEquals(depositAmount, entry.amount());
        assertEquals(new BigDecimal("500.00"), entry.balanceAfter());
        assertEquals("DEP-TEST-001", entry.referenceId());
    }

    @Test
    @DisplayName("Should fail when debiting more than available balance")
    void testInsufficientBalanceDebit() {
        WalletDto wallet = walletService.createWallet(new CreateWalletRequest(103L, Currency.USD));

        // Credit 100
        walletService.postLedgerEntry(wallet.id(), EntryType.CREDIT, new BigDecimal("100.00"), "DEP-1", "Dep");

        // Try to debit 150
        assertThrows(BusinessException.class, () ->
                walletService.postLedgerEntry(wallet.id(), EntryType.DEBIT, new BigDecimal("150.00"), "WDL-1", "Overdraw")
        );
    }
}
