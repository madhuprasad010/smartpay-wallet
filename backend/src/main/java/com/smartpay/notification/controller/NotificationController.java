package com.smartpay.notification.controller;

import com.smartpay.common.dto.ApiResponse;
import com.smartpay.notification.dto.NotificationDto;
import com.smartpay.notification.dto.SendNotificationRequest;
import com.smartpay.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationDto>> send(@Valid @RequestBody SendNotificationRequest request) {
        NotificationDto notification = notificationService.createAndSend(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Notification queued and sent", notification));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getByUser(@PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getByUserPaged(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationDto> page = notificationService.getUserNotificationsPaged(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markRead(@PathVariable Long id) {
        NotificationDto updated = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", updated));
    }

    @PostMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
