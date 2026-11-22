package com.smartpay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventPublisher.class);

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    public void send(String exchange, String routingKey, Object message) {
        if (rabbitTemplate != null) {
            try {
                rabbitTemplate.convertAndSend(exchange, routingKey, message);
            } catch (Exception e) {
                log.warn("RabbitMQ dispatch failed for exchange {}: {}", exchange, e.getMessage());
            }
        } else {
            log.debug("RabbitMQ is disabled; skipping dispatch for exchange {}", exchange);
        }
    }
}
