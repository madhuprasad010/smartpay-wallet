package com.smartpay.transaction.repository;

import com.smartpay.transaction.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(String status);
    List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
