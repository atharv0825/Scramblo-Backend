package com.scramble.dto.Notification;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPreferenceRequest {

    private Boolean newPost;
    private Boolean comments;
    private Boolean likes;
    private Boolean summaryReady;
    private Boolean emailEnabled;
}
