package com.smartpay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, String key, Object payload) {
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(topic, key, payload);
            } catch (Exception e) {
                log.warn("Kafka dispatch failed for topic {}: {}", topic, e.getMessage());
            }
        } else {
            log.debug("Kafka is disabled; skipping dispatch for topic {}", topic);
        }
    }
}
