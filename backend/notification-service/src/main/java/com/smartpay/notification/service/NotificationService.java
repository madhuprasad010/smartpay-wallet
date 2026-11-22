package com.smartpay.notification.service;

import com.smartpay.notification.config.RabbitMQConfig;
import com.smartpay.notification.dto.NotificationDto;
import com.smartpay.notification.dto.SendNotificationRequest;
import com.smartpay.notification.entity.Notification;
import com.smartpay.notification.entity.NotificationStatus;
import com.smartpay.notification.exception.ResourceNotFoundException;
import com.smartpay.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public NotificationDto createAndSend(SendNotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.userId())
                .type(request.type())
                .channel(request.channel())
                .subject(request.subject())
                .body(request.body())
                .status(NotificationStatus.SENT)
                .retryCount(0)
                .sentAt(Instant.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        // Queue in RabbitMQ for background delivery
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    "notification.email.send",
                    Map.of(
                            "notificationId", saved.getId(),
                            "userId", saved.getUserId(),
                            "subject", saved.getSubject(),
                            "body", saved.getBody()
                    )
            );
        } catch (Exception e) {
            log.warn("Failed to push to RabbitMQ: {}", e.getMessage());
        }

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public NotificationDto markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        n.setStatus(NotificationStatus.READ);
        return mapToDto(notificationRepository.save(n));
    }

    private NotificationDto mapToDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getUserId(),
                n.getType(),
                n.getChannel(),
                n.getSubject(),
                n.getBody(),
                n.getStatus(),
                n.getCreatedAt(),
                n.getSentAt()
        );
    }
}
