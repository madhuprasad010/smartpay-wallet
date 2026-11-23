package com.smartpay;

import com.smartpay.payment.dto.DepositRequest;
import com.smartpay.payment.dto.PaymentDto;
import com.smartpay.payment.dto.WithdrawalRequest;
import com.smartpay.payment.entity.PaymentStatus;
import com.smartpay.payment.service.PaymentService;
import com.smartpay.wallet.dto.CreateWalletRequest;
import com.smartpay.wallet.dto.WalletDto;
import com.smartpay.wallet.entity.Currency;
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
class PaymentServiceTests {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WalletService walletService;

    @Test
    @DisplayName("Payment Adapter: Simulate deposit into wallet")
    void testSimulateDeposit() {
        WalletDto wallet = walletService.createWallet(new CreateWalletRequest(1001L, Currency.INR));

        DepositRequest request = new DepositRequest(
                wallet.id(),
                new BigDecimal("1500.00"),
                "INR",
                "UPI_SIMULATED",
                "IDEMP-DEP-" + UUID.randomUUID(),
                "Simulated UPI Top-up"
        );

        PaymentDto payment = paymentService.simulateDeposit(request);

        assertNotNull(payment);
        assertEquals(PaymentStatus.SUCCESS, payment.status());
        assertEquals("DEPOSIT", payment.type());
        assertEquals(new BigDecimal("1500.00"), payment.amount());

        // Check wallet balance
        WalletDto updated = walletService.getWalletById(wallet.id());
        assertEquals(new BigDecimal("1500.00"), updated.balance());
    }

    @Test
    @DisplayName("Payment Adapter: Simulate withdrawal from wallet")
    void testSimulateWithdrawal() {
        WalletDto wallet = walletService.createWallet(new CreateWalletRequest(1002L, Currency.INR));

        // Initial deposit of 2000
        paymentService.simulateDeposit(new DepositRequest(
                wallet.id(),
                new BigDecimal("2000.00"),
                "INR",
                "NETBANKING",
                "IDEMP-DEP-" + UUID.randomUUID(),
                "Seed"
        ));

        // Payout of 800
        WithdrawalRequest wReq = new WithdrawalRequest(
                wallet.id(),
                new BigDecimal("800.00"),
                "INR",
                "987654321098",
                "SBIN0001234",
                "IDEMP-WTH-" + UUID.randomUUID(),
                "Simulated payout"
        );

        PaymentDto payout = paymentService.simulateWithdrawal(wReq);
        assertNotNull(payout);
        assertEquals(PaymentStatus.SUCCESS, payout.status());
        assertEquals("WITHDRAWAL", payout.type());

        WalletDto updated = walletService.getWalletById(wallet.id());
        assertEquals(new BigDecimal("1200.00"), updated.balance());
    }
}
