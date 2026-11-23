package com.smartpay.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "wallet_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false, unique = true)
    private Long walletId;

    @Column(name = "daily_limit", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal dailyLimit = new BigDecimal("100000.0000");

    @Column(name = "per_transaction_limit", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal perTransactionLimit = new BigDecimal("50000.0000");

    @Column(name = "daily_used", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal dailyUsed = BigDecimal.ZERO;

    @Column(name = "last_reset_date", nullable = false)
    @Builder.Default
    private LocalDate lastResetDate = LocalDate.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
