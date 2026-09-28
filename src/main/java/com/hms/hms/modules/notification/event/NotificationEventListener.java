package com.hms.hms.modules.notification.event;

import com.hms.hms.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.SimpMessagingTemplate;
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {

        notificationService.sendNotification(
                event.getUserId(),
                event.getTitle(),
                event.getMessage()
        );
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + event.getUserId(),
                event
        );
    }
}