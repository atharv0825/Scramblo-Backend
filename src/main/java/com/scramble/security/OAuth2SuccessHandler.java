package com.scramble.security;

import com.scramble.entity.User;
import com.scramble.repository.UserRepository;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        String email = authentication.getName();

        // 🔍 STEP 1: CHECK USER IN DB
        Optional<User> existingUser = userRepository.findByEmail(email);

        boolean isNewUser = false;
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            isNewUser = true;
            user = new User();
            user.setEmail(email);

            user.setName(authentication.getName());

            user.setProfileCompleted(false);

            userRepository.save(user);
        }

        String roles = authentication.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        String token = jwtService.generateToken(email, roles);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);

        response.addCookie(cookie);

        String redirectUrl;

        if (isNewUser || !user.isProfileCompleted()) {
            redirectUrl = "http://localhost:5173/oauth/callback?token=" + token + "&newUser=true";
        } else {
            redirectUrl = "http://localhost:5173/oauth/callback?token=" + token + "&newUser=false";
        }

        response.sendRedirect(redirectUrl);
    }
}