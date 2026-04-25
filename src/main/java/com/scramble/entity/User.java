package com.scramble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    private String bio;
    private String profileImage;
    private String coverImage;

    private int followersCount;
    private int followingCount;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;    // GOOGLE, GITHUB, LOCAL

    @Column(unique = true)
    private String providerId;   // OAuth ID

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean isActive = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean profileCompleted = false;
    private String instagram;
    private String twitter;
    private String linkedin;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}