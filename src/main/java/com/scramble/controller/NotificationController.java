    package com.scramble.controller;

    import com.scramble.dto.Notification.NotificationPreferenceRequest;
    import com.scramble.entity.Notification;
    import com.scramble.entity.NotificationPreference;
    import com.scramble.service.NotificationService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/notifications")
    @RequiredArgsConstructor
    public class NotificationController {

        private final NotificationService notificationService;

        @GetMapping
        public List<Notification> getUserNotifications() {
            return notificationService.getNotifications();
        }

        @PutMapping("/{id}/read")
        public void markAsRead(@PathVariable Long id) {
            notificationService.markAsRead(id);
        }

        @GetMapping("/unread-count")
        public long unreadCount() {
            return notificationService.getUnreadCount();
        }

        @GetMapping("/preferences")
        public NotificationPreference getPreferences() {
            return notificationService.getOrCreate();
        }

        @PutMapping("/preferences")
        public NotificationPreference updatePreferences(
                @RequestBody NotificationPreferenceRequest request) {
            return notificationService.updatePreferences(request);
        }
    }