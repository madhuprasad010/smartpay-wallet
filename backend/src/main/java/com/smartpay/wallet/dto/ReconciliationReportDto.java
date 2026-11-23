package com.smartpay.wallet.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ReconciliationReportDto(
        Long walletId,
        String walletNumber,
        BigDecimal projectedBalance,
        BigDecimal calculatedLedgerBalance,
        BigDecimal discrepancy,
        boolean balanced,
        int totalLedgerEntries,
        Instant reconciledAt
) {}
