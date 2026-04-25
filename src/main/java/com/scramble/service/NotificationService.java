package com.scramble.service;

import com.scramble.dto.Notification.NotificationEvent;
import com.scramble.dto.Notification.NotificationPreferenceRequest;
import com.scramble.entity.Notification;
import com.scramble.entity.NotificationPreference;
import com.scramble.entity.User;
import com.scramble.repository.NotificationPreferenceRepository;
import com.scramble.repository.NotificationRepository;
import com.scramble.repository.UserRepository;
import com.scramble.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationPreferenceRepository repository;
    private final SecurityUtils securityUtils;

    public List<Notification> getNotifications(){
        User user = securityUtils.getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public void createNotification(NotificationEvent event){

        NotificationPreference pref = repository
                .findByUserId(event.getUserId())
                .orElse(null);

        if (pref != null && !isAllowed(event.getType(), pref)) {
            return;
        }


        User user = userRepository.findById(event.getUserId())
                .orElseThrow();

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(event.getType());
        notification.setMessage(event.getMessage());
        notification.setActorId(event.getActorId());
        notification.setArticleId(event.getArticleId());
        notification.setRead(false);

        notificationRepository.save(notification);


        messagingTemplate.convertAndSend(
                "/topic/notifications/" + user.getId(),
                notification
        );
    }


    public NotificationPreference getOrCreate() {
        User user = securityUtils.getCurrentUser();

        return repository.findByUserId(user.getId())
                .orElseGet(() -> {
                    NotificationPreference pref = NotificationPreference.builder()
                            .userId(user.getId())
                            .build(); // defaults applied
                    return repository.save(pref);
                });
    }

    public NotificationPreference updatePreferences(NotificationPreferenceRequest request) {

        NotificationPreference pref = getOrCreate();

        if (request.getNewPost() != null)
            pref.setNewPost(request.getNewPost());
        if (request.getComments() != null)
            pref.setComments(request.getComments());
        if (request.getLikes() != null)
            pref.setLikes(request.getLikes());
        if (request.getSummaryReady() != null)
            pref.setSummaryReady(request.getSummaryReady());
        if (request.getEmailEnabled() != null)
            pref.setEmailEnabled(request.getEmailEnabled());

        return repository.save(pref);
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow();

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public long getUnreadCount() {
        User user = securityUtils.getCurrentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    private boolean isAllowed(String type, NotificationPreference pref) {

        return switch (type) {
            case "NEW_POST" -> Boolean.TRUE.equals(pref.getNewPost());
            case "COMMENT" -> Boolean.TRUE.equals(pref.getComments());
            case "LIKE" -> Boolean.TRUE.equals(pref.getLikes());
            case "SUMMARY_READY" -> Boolean.TRUE.equals(pref.getSummaryReady());
            default -> true;
        };
    }


}
