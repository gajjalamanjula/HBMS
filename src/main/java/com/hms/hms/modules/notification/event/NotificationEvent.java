package com.hms.hms.modules.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class NotificationEvent {

    private UUID userId;

    private String title;

    private String message;
}