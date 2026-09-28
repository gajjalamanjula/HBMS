package com.hms.hms.modules.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private boolean isRead;

    private LocalDateTime createdAt;
}