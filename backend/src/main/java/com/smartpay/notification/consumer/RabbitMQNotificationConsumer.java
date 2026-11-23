package com.smartpay.notification.consumer;

import com.smartpay.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "smartpay.rabbitmq.enabled", havingValue = "true")
public class RabbitMQNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQNotificationConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processNotification(Map<String, Object> message) {
        log.info("Received durable background notification job via RabbitMQ: {}", message);
        // Process background notification asynchronously (e.g. simulated dispatch)
    }

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void processAuditMessage(Map<String, Object> message) {
        log.info("Received audit log job via RabbitMQ: {}", message);
    }
}
