package com.smartpay.wallet.consumer;

import com.smartpay.common.entity.ProcessedEvent;
import com.smartpay.common.repository.ProcessedEventRepository;
import com.smartpay.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "smartpay.kafka.enabled", havingValue = "true")
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = KafkaConfig.TOPIC_TRANSACTION_EVENTS, groupId = "smartpay-consumer-group")
    @Transactional
    public void handleTransactionEvent(Map<String, Object> event) {
        if (event == null) return;

        String eventId = (String) event.get("eventId");
        String eventType = (String) event.get("eventType");

        // Consumer Idempotency Check
        if (eventId != null && processedEventRepository.existsByEventId(eventId)) {
            log.info("Duplicate Kafka event detected (eventId: {}). Idempotently skipping.", eventId);
            return;
        }

        log.info("Processing Kafka event {} of type {}", eventId, eventType);

        if (eventId != null) {
            ProcessedEvent processed = ProcessedEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType != null ? eventType : "TRANSACTION_EVENT")
                    .build();
            processedEventRepository.save(processed);
        }
    }
}
