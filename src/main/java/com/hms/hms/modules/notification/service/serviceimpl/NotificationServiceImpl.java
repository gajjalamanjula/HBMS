package com.hms.hms.modules.notification.service.serviceimpl;

import com.hms.hms.modules.notification.dto.NotificationResponse;
import com.hms.hms.modules.notification.entity.Notification;
import com.hms.hms.modules.notification.repository.NotificationRepository;
import com.hms.hms.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void sendNotification(UUID userId, String title, String message) {

        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> getUserNotifications(UUID userId) {

        return notificationRepository.findByUserId(userId)
                .stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(UUID userId) {

        List<Notification> notifications = notificationRepository.findByUserId(userId);

        notifications.forEach(n -> n.setRead(true));

        notificationRepository.saveAll(notifications);
    }

    @Override
    public Long getUnreadCount(UUID userId) {

        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void deleteNotification(Long id) {

        notificationRepository.deleteById(id);
    }

    @Override
    public void clearAll(UUID userId) {

        notificationRepository.deleteByUserId(userId);
    }
}