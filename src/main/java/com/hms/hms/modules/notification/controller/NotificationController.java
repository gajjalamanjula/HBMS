package com.hms.hms.modules.notification.controller;

import com.hms.hms.modules.notification.dto.NotificationResponse;
import com.hms.hms.modules.notification.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public void sendNotification(@RequestBody SendNotificationRequest request) {

        notificationService.sendNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage()
        );
    }

    @GetMapping("/{userId}")
    public List<NotificationResponse> getNotifications(@PathVariable UUID userId) {

        return notificationService.getUserNotifications(userId);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {

        notificationService.markAsRead(id);
    }

    @PutMapping("/read-all")
    public void markAllAsRead(@RequestParam UUID userId) {

        notificationService.markAllAsRead(userId);
    }

    @GetMapping("/unread-count")
    public Long getUnreadCount(@RequestParam UUID userId) {

        return notificationService.getUnreadCount(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id) {

        notificationService.deleteNotification(id);
    }

    @DeleteMapping("/clear-all")
    public void clearAll(@RequestParam UUID userId) {

        notificationService.clearAll(userId);
    }

    @Data
    public static class SendNotificationRequest {

        private UUID userId;
        private String title;
        private String message;
    }
}