package com.smartpay.beneficiary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "beneficiaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "account_number", nullable = false, length = 64)
    private String accountNumber;

    @Column(name = "bank_code", length = 30)
    private String bankCode;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String status; // VERIFIED, PENDING, REJECTED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
