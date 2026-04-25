package com.scramble.repository;

import com.scramble.entity.DefaultProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultProfileImageRepository extends JpaRepository<DefaultProfileImage, Long> {

    // Fetch random default profile image
    @Query(value = "SELECT * FROM default_profile_images ORDER BY RAND() LIMIT 1", nativeQuery = true)
    DefaultProfileImage findRandomImage();
}