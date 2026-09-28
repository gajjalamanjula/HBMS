package com.hms.hms.modules.notification.repository;

import com.hms.hms.modules.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(UUID userId);

    Long countByUserIdAndIsReadFalse(UUID userId);

    void deleteByUserId(UUID userId);
}