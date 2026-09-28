package com.hms.hms.modules.notification.service;

import com.hms.hms.modules.notification.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void sendNotification(UUID userId, String title, String message);

    List<NotificationResponse> getUserNotifications(UUID userId);

    void markAsRead(Long id);

    void markAllAsRead(UUID userId);

    Long getUnreadCount(UUID userId);

    void deleteNotification(Long id);

    void clearAll(UUID userId);
}