package com.scramble.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preference")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Boolean newPost = true;

    @Column(nullable = false)
    private Boolean comments = true;

    @Column(nullable = false)
    private Boolean likes = true;

    @Column(nullable = false)
    private Boolean summaryReady = true;

    @Column(nullable = false)
    private Boolean emailEnabled = false;
}