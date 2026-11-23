package com.smartpay.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpay.config.KafkaConfig;
import com.smartpay.config.KafkaEventPublisher;
import com.smartpay.transaction.entity.OutboxEvent;
import com.smartpay.transaction.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaEventPublisher kafkaPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events for publishing", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                String topic = resolveTopic(event.getAggregateType());
                Object payload = objectMapper.readValue(event.getPayload(), Map.class);

                kafkaPublisher.send(topic, event.getAggregateId(), payload);

                event.setStatus("PUBLISHED");
                event.setSentAt(Instant.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.warn("Failed to publish outbox event id {}: {}", event.getId(), e.getMessage());
            }
        }
    }

    private String resolveTopic(String aggregateType) {
        if ("USER".equalsIgnoreCase(aggregateType)) {
            return KafkaConfig.TOPIC_USER_EVENTS;
        } else if ("WALLET".equalsIgnoreCase(aggregateType)) {
            return KafkaConfig.TOPIC_WALLET_EVENTS;
        } else if ("PAYMENT".equalsIgnoreCase(aggregateType)) {
            return KafkaConfig.TOPIC_PAYMENT_EVENTS;
        } else if ("NOTIFICATION".equalsIgnoreCase(aggregateType)) {
            return KafkaConfig.TOPIC_NOTIFICATION_EVENTS;
        }
        return KafkaConfig.TOPIC_TRANSACTION_EVENTS;
    }
}
