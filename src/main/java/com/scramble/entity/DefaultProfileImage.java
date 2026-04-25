package com.scramble.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "default_profile_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String url;
}