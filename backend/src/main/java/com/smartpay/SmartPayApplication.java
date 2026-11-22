package com.smartpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication(exclude = {
        KafkaAutoConfiguration.class,
        RabbitAutoConfiguration.class
})
public class SmartPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPayApplication.class, args);
    }
}
