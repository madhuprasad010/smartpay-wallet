package com.smartpay;

import com.smartpay.common.exception.BusinessException;
import com.smartpay.transaction.dto.TransactionDto;
import com.smartpay.transaction.dto.TransferRequest;
import com.smartpay.transaction.entity.TransactionStatus;
import com.smartpay.transaction.entity.TransactionType;
import com.smartpay.transaction.service.TransactionService;
import com.smartpay.wallet.dto.CreateWalletRequest;
import com.smartpay.wallet.dto.ReconciliationReportDto;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FinancialCorrectnessTests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletService walletService;

    @Test
    @DisplayName("Financial Correctness: Reject duplicate idempotency key when payload differs")
    void testDuplicateIdempotencyKeyWithDifferentPayload() {
        WalletDto sender = walletService.createWallet(new CreateWalletRequest(501L, Currency.INR));
        WalletDto receiver = walletService.createWallet(new CreateWalletRequest(502L, Currency.INR));

        walletService.postLedgerEntry(sender.id(), EntryType.CREDIT, new BigDecimal("1000.00"), "INIT-001", "Seed");

        String sharedKey = "IDEMP-CONFLICT-" + UUID.randomUUID();
        TransferRequest req1 = new TransferRequest(
                sharedKey,
                sender.id(),
                receiver.id(),
                new BigDecimal("100.00"),
                "INR",
                "First transfer"
        );
        TransactionDto txn1 = transactionService.processTransfer(req1);
        assertNotNull(txn1);

        // Attempt second transfer with same key but DIFFERENT amount ($200 instead of $100)
        TransferRequest req2 = new TransferRequest(
                sharedKey,
                sender.id(),
                receiver.id(),
                new BigDecimal("200.00"),
                "INR",
                "Second altered transfer"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.processTransfer(req2));
        assertTrue(ex.getMessage().contains("previously used with a different request payload"));
    }

    @Test
    @DisplayName("Financial Correctness: Reject negative or zero amount transfers")
    void testNegativeOrZeroTransfer() {
        WalletDto sender = walletService.createWallet(new CreateWalletRequest(601L, Currency.INR));
        WalletDto receiver = walletService.createWallet(new CreateWalletRequest(602L, Currency.INR));

        walletService.postLedgerEntry(sender.id(), EntryType.CREDIT, new BigDecimal("500.00"), "INIT-002", "Seed");

        TransferRequest zeroReq = new TransferRequest(
                "IDEMP-ZERO-" + UUID.randomUUID(),
                sender.id(),
                receiver.id(),
                BigDecimal.ZERO,
                "INR",
                "Zero amount"
        );
        assertThrows(BusinessException.class, () -> transactionService.processTransfer(zeroReq));

        TransferRequest negReq = new TransferRequest(
                "IDEMP-NEG-" + UUID.randomUUID(),
                sender.id(),
                receiver.id(),
                new BigDecimal("-50.00"),
                "INR",
                "Negative amount"
        );
        assertThrows(BusinessException.class, () -> transactionService.processTransfer(negReq));
    }

    @Test
    @DisplayName("Financial Correctness: Reject transfer when wallet is frozen")
    void testFrozenWalletTransfer() {
        WalletDto sender = walletService.createWallet(new CreateWalletRequest(701L, Currency.INR));
        WalletDto receiver = walletService.createWallet(new CreateWalletRequest(702L, Currency.INR));

        walletService.postLedgerEntry(sender.id(), EntryType.CREDIT, new BigDecimal("500.00"), "INIT-003", "Seed");
        walletService.freezeWallet(sender.id());

        TransferRequest req = new TransferRequest(
                "IDEMP-FROZEN-" + UUID.randomUUID(),
                sender.id(),
                receiver.id(),
                new BigDecimal("50.00"),
                "INR",
                "From frozen wallet"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.processTransfer(req));
        assertTrue(ex.getMessage().contains("not active"));
    }

    @Test
    @DisplayName("Financial Correctness: Reversal creates compensating entries and restores balances")
    void testCompensatingReversal() {
        WalletDto sender = walletService.createWallet(new CreateWalletRequest(801L, Currency.INR));
        WalletDto receiver = walletService.createWallet(new CreateWalletRequest(802L, Currency.INR));

        walletService.postLedgerEntry(sender.id(), EntryType.CREDIT, new BigDecimal("1000.00"), "INIT-004", "Seed");

        // Transfer 300
        TransferRequest req = new TransferRequest(
                "IDEMP-REV-" + UUID.randomUUID(),
                sender.id(),
                receiver.id(),
                new BigDecimal("300.00"),
                "INR",
                "Transfer before reversal"
        );
        TransactionDto txn = transactionService.processTransfer(req);
        assertEquals(TransactionStatus.COMPLETED, txn.status());

        // Sender: 700, Receiver: 300
        assertEquals(new BigDecimal("700.00"), walletService.getWalletById(sender.id()).balance());
        assertEquals(new BigDecimal("300.00"), walletService.getWalletById(receiver.id()).balance());

        // Execute reversal
        TransactionDto reversalTxn = transactionService.reverseTransaction(txn.transactionRef(), "Chargeback dispute");
        assertNotNull(reversalTxn);
        assertEquals(TransactionType.REVERSAL, reversalTxn.type());
        assertEquals(TransactionStatus.COMPLETED, reversalTxn.status());
        assertEquals(new BigDecimal("300.00"), reversalTxn.amount());

        // Balances restored
        assertEquals(0, new BigDecimal("1000.00").compareTo(walletService.getWalletById(sender.id()).balance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(walletService.getWalletById(receiver.id()).balance()));
    }

    @Test
    @DisplayName("Financial Correctness: Double-entry ledger reconciliation confirms balanced state")
    void testLedgerReconciliation() {
        WalletDto wallet = walletService.createWallet(new CreateWalletRequest(901L, Currency.INR));

        // Credit 1000, Debit 200, Credit 500 => Balance: 1300
        walletService.postLedgerEntry(wallet.id(), EntryType.CREDIT, new BigDecimal("1000.00"), "DEP-1", "Seed 1");
        walletService.postLedgerEntry(wallet.id(), EntryType.DEBIT, new BigDecimal("200.00"), "WDL-1", "Withdrawal 1");
        walletService.postLedgerEntry(wallet.id(), EntryType.CREDIT, new BigDecimal("500.00"), "DEP-2", "Seed 2");

        ReconciliationReportDto report = walletService.reconcileWallet(wallet.id());

        assertNotNull(report);
        assertTrue(report.balanced());
        assertEquals(new BigDecimal("1300.00"), report.projectedBalance());
        assertEquals(new BigDecimal("1300.00"), report.calculatedLedgerBalance());
        assertEquals(0, BigDecimal.ZERO.compareTo(report.discrepancy()));
        assertEquals(3, report.totalLedgerEntries());
    }
}
