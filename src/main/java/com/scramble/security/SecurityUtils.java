package com.scramble.security;

import com.scramble.entity.User;
import com.scramble.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        Object principal = authentication.getPrincipal();

        // ✅ Email login
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUser();
        }

        // ✅ OAuth login
        if (principal instanceof OAuth2User oAuth2User) {
            return (User) oAuth2User.getAttributes().get("user");
        }

        // ✅ JWT (your current case)
        if (principal instanceof Jwt jwt) {
            String email = jwt.getSubject();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        throw new RuntimeException("Unknown authentication type: " + principal.getClass());
    }
}