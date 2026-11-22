package com.smartpay;

import com.smartpay.common.exception.BusinessException;
import com.smartpay.transaction.dto.TransactionDto;
import com.smartpay.transaction.dto.TransferRequest;
import com.smartpay.transaction.entity.TransactionStatus;
import com.smartpay.transaction.service.TransactionService;
import com.smartpay.wallet.dto.CreateWalletRequest;
import com.smartpay.wallet.dto.WalletDto;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceTests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletService walletService;

    @Test
    @DisplayName("Should execute atomic peer-to-peer transfer with double-entry ledger posting")
    void testAtomicTransfer() {
        WalletDto sender = walletService.createWallet(new CreateWalletRequest(201L, Currency.USD));
        WalletDto receiver = walletService.createWallet(new CreateWalletRequest(202L, Currency.USD));

        // Credit sender with $1000.00
        walletService.postLedgerEntry(sender.id(), EntryType.CREDIT, new BigDecimal("1000.00"), "INIT-001", "Seed deposit");

        // Transfer $250.00 to receiver
        String key = "IDEMP-" + UUID.randomUUID();
        TransferRequest request = new TransferRequest(
                key,
                sender.id(),
                receiver.id(),
                new BigDecimal("250.00"),
                "USD",
                "Payment for services"
        );

        TransactionDto txn = transactionService.processTransfer(request);

        assertNotNull(txn);
        assertEquals(TransactionStatus.COMPLETED, txn.status());
        assertEquals(new BigDecimal("250.00"), txn.amount());
        assertTrue(txn.transactionRef().startsWith("TXN-"));

        // Verify balances
        WalletDto senderAfter = walletService.getWalletById(sender.id());
        WalletDto receiverAfter = walletService.getWalletById(receiver.id());

        assertEquals(new BigDecimal("750.00"), senderAfter.balance());
        assertEquals(new BigDecimal("250.00"), receiverAfter.balance());
    }

    @Test
    @DisplayName("Should strictly enforce idempotency on repeated transfer requests")
    void testIdempotentTransferReplay() {
        WalletDto sender = walletService.createWallet(new CreateWalletRequest(301L, Currency.USD));
        WalletDto receiver = walletService.createWallet(new CreateWalletRequest(302L, Currency.USD));

        walletService.postLedgerEntry(sender.id(), EntryType.CREDIT, new BigDecimal("500.00"), "INIT-002", "Seed deposit");

        String fixedIdempotencyKey = "IDEMP-FIXED-KEY-999";
        TransferRequest request = new TransferRequest(
                fixedIdempotencyKey,
                sender.id(),
                receiver.id(),
                new BigDecimal("100.00"),
                "USD",
                "Idempotent transfer test"
        );

        // First attempt
        TransactionDto txn1 = transactionService.processTransfer(request);
        assertNotNull(txn1);

        // Replay attempt with same idempotency key
        TransactionDto txn2 = transactionService.processTransfer(request);
        assertNotNull(txn2);

        // Must return identical transaction record
        assertEquals(txn1.id(), txn2.id());
        assertEquals(txn1.transactionRef(), txn2.transactionRef());
        assertEquals(txn1.idempotencyKey(), txn2.idempotencyKey());

        // Balance should ONLY have been deducted ONCE ($500 - $100 = $400)
        WalletDto senderAfter = walletService.getWalletById(sender.id());
        WalletDto receiverAfter = walletService.getWalletById(receiver.id());

        assertEquals(new BigDecimal("400.00"), senderAfter.balance());
        assertEquals(new BigDecimal("100.00"), receiverAfter.balance());
    }

    @Test
    @DisplayName("Should reject transfer when balance is insufficient and preserve balances")
    void testInsufficientBalanceTransfer() {
        WalletDto sender = walletService.createWallet(new CreateWalletRequest(401L, Currency.USD));
        WalletDto receiver = walletService.createWallet(new CreateWalletRequest(402L, Currency.USD));

        // Sender only has $50.00
        walletService.postLedgerEntry(sender.id(), EntryType.CREDIT, new BigDecimal("50.00"), "INIT-003", "Seed");

        TransferRequest request = new TransferRequest(
                "IDEMP-" + UUID.randomUUID(),
                sender.id(),
                receiver.id(),
                new BigDecimal("100.00"),
                "USD",
                "Should fail"
        );

        assertThrows(BusinessException.class, () -> transactionService.processTransfer(request));

        // Balances must remain unchanged
        assertEquals(new BigDecimal("50.00"), walletService.getWalletById(sender.id()).balance());
        assertEquals(BigDecimal.ZERO, walletService.getWalletById(receiver.id()).balance());
    }
}
