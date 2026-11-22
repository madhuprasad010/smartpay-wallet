package com.smartpay.notification.controller;

import com.smartpay.notification.dto.ApiResponse;
import com.smartpay.notification.dto.NotificationDto;
import com.smartpay.notification.dto.SendNotificationRequest;
import com.smartpay.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDto>> send(@Valid @RequestBody SendNotificationRequest request) {
        NotificationDto result = notificationService.createAndSend(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Notification queued", result));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getByUser(@PathVariable Long userId) {
        List<NotificationDto> list = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markRead(@PathVariable Long id) {
        NotificationDto updated = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}
