package com.scramble.service;

import com.scramble.dto.User.InterestRequest;
import com.scramble.dto.User.UpdateProfileRequest;
import com.scramble.dto.User.UserResponse;
import com.scramble.entity.DefaultProfileImage;
import com.scramble.entity.Tag;
import com.scramble.entity.User;
import com.scramble.entity.UserInterest;
import com.scramble.repository.DefaultProfileImageRepository;
import com.scramble.repository.TagRepository;
import com.scramble.repository.UserInterestRepository;
import com.scramble.repository.UserRepository;
import com.scramble.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserInterestRepository userInterestRepository;
    private final TagRepository tagRepository;
    private final SecurityUtils securityUtils;
    private final DefaultProfileImageRepository repository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public void setUserInterests(InterestRequest interestRequest) {

        List<String> tags = interestRequest.getTags();

        User user = securityUtils.getCurrentUser();

        userInterestRepository.deleteByUserId(user.getId());

        for (String tagName : tags) {

            String normalized = tagName.trim().toUpperCase();

            Tag tag = tagRepository.findByName(normalized)
                    .orElseGet(() -> tagRepository.save(
                            Tag.builder().name(normalized).build()
                    ));

            UserInterest ui = UserInterest.builder()
                    .user(user)
                    .tag(tag)
                    .build();

            userInterestRepository.save(ui);
        }
    }

    public UserResponse getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> tags = getUserInterestTags(userId);

        return mapUser(user, tags);
    }

    public List<String> getUserInterestTags(Long userId) {
        return userInterestRepository.findByUserId(userId)
                .stream()
                .map(ui -> ui.getTag().getName())
                .toList();
    }

    public UserResponse getUserDetails(){
        User user = securityUtils.getCurrentUser();
        List<String> tags = getUserInterestTags(user.getId());

        return mapUser(user, tags);
    }

    public void updateProfile(UpdateProfileRequest request) {

        User user = securityUtils.getCurrentUser();

        // BASIC INFO
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        if (request.getCoverImage() != null) {
            user.setCoverImage(request.getCoverImage());
        }

        // 🔥 SOCIAL LINKS
        if (request.getInstagram() != null) {
            user.setInstagram(request.getInstagram());
        }

        if (request.getTwitter() != null) {
            user.setTwitter(request.getTwitter());
        }

        if (request.getLinkedin() != null) {
            user.setLinkedin(request.getLinkedin());
        }

        if (request.getInterests() != null && !request.getInterests().isEmpty()) {

            userInterestRepository.deleteByUserId(user.getId());

            for (String tagName : request.getInterests()) {

                String normalized = tagName.trim().toUpperCase();

                Tag tag = tagRepository.findByName(normalized)
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder().name(normalized).build()
                        ));

                UserInterest ui = UserInterest.builder()
                        .user(user)
                        .tag(tag)
                        .build();

                userInterestRepository.save(ui);
            }
        }

        user.setProfileCompleted(true);
    }


    public void deleteProfileImage() {

        User user = securityUtils.getCurrentUser();

        if (user.getProfileImage() != null) {
            s3Service.deleteFile(user.getProfileImage());
            user.setProfileImage(null);
        }
    }



    public void deleteCoverImage() {

        User user = securityUtils.getCurrentUser();

        if (user.getCoverImage() != null) {
            s3Service.deleteFile(user.getCoverImage());
            user.setCoverImage(null);
        }
    }

    public List<DefaultProfileImage> getAllImages() {
        return repository.findAll();
    }

    private UserResponse mapUser(User user, List<String> tags) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .createdAt(user.getCreatedAt())
                .profileCompleted(user.isProfileCompleted())
                .tags(tags)
                .coverImage(user.getCoverImage())
                .instagram(user.getInstagram())
                .twitter(user.getTwitter())
                .linkedin(user.getLinkedin())
                .build();
    }

}
