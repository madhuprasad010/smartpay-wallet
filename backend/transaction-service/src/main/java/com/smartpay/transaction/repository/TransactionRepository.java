package com.smartpay.transaction.repository;

import com.smartpay.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    Optional<Transaction> findByTransactionRef(String transactionRef);
    Page<Transaction> findBySourceWalletIdOrDestWalletId(Long sourceWalletId, Long destWalletId, Pageable pageable);
}
