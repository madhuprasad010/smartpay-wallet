package com.smartpay.wallet.consumer;

import com.smartpay.wallet.entity.EntryType;
import com.smartpay.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);
    private final WalletService walletService;

    @KafkaListener(topics = "smartpay.transaction.events", groupId = "wallet-service-group")
    public void handleTransactionEvent(Map<String, Object> event) {
        log.info("Received transaction event in wallet-service: {}", event);
        if (event == null) return;

        String eventType = (String) event.get("eventType");
        if ("TRANSACTION_COMPLETED".equals(eventType)) {
            String transactionRef = (String) event.get("transactionRef");
            Object sourceObj = event.get("sourceWalletId");
            Object destObj = event.get("destWalletId");
            Object amountObj = event.get("amount");

            if (sourceObj != null && destObj != null && amountObj != null) {
                Long sourceWalletId = ((Number) sourceObj).longValue();
                Long destWalletId = ((Number) destObj).longValue();
                BigDecimal amount = new BigDecimal(amountObj.toString());

                try {
                    // Double-entry bookkeeping: debit source wallet
                    walletService.postLedgerEntry(
                            sourceWalletId,
                            EntryType.DEBIT,
                            amount,
                            transactionRef,
                            "Transfer to wallet " + destWalletId
                    );

                    // Double-entry bookkeeping: credit destination wallet
                    walletService.postLedgerEntry(
                            destWalletId,
                            EntryType.CREDIT,
                            amount,
                            transactionRef,
                            "Transfer from wallet " + sourceWalletId
                    );
                    log.info("Successfully recorded double-entry ledger for transaction {}", transactionRef);
                } catch (Exception e) {
                    log.error("Failed to post ledger entries for transaction {}: {}", transactionRef, e.getMessage(), e);
                }
            }
        }
    }
}
