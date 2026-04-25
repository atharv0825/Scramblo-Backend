package com.scramble.kafka;

import com.scramble.dto.Notification.NotificationEvent;
import com.scramble.service.NotificationService;
import jdk.jfr.Registered;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "FOLLOW", groupId = "notification-group")
    public void handleFollow(NotificationEvent event) {
        notificationService.createNotification(event);
    }

    @KafkaListener(topics = "COMMENT", groupId = "notification-group")
    public void handleComment(NotificationEvent event) {
        notificationService.createNotification(event);
    }

    @KafkaListener(topics = "LIKE", groupId = "notification-group")
    public void handleLike(NotificationEvent event) {
        notificationService.createNotification(event);
    }

    @KafkaListener(topics = "NEW_POST", groupId = "notification-group")
    public void handleNewPost(NotificationEvent event) {
        notificationService.createNotification(event);
    }

}