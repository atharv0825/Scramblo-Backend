package com.scramble.service;

import com.scramble.dto.Auth.AuthResponse;
import com.scramble.dto.Auth.LogInRequest;
import com.scramble.dto.Auth.RegisterRequest;
import com.scramble.entity.AuthProvider;
import com.scramble.entity.NotificationPreference;
import com.scramble.entity.Role;
import com.scramble.entity.User;
import com.scramble.exceptions.AuthException;
import com.scramble.repository.NotificationPreferenceRepository;
import com.scramble.repository.UserRepository;
import com.scramble.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public AuthResponse register(RegisterRequest request){

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("User already exist");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();


        userRepository.save(user);


        NotificationPreference preference = NotificationPreference.builder()
                .userId(user.getId())
                .newPost(true)
                .comments(true)
                .likes(true)
                .summaryReady(true)
                .emailEnabled(false)
                .build();

        notificationPreferenceRepository.save(preference);

        String token = jwtService.generateToken(user.getEmail(),user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }



    public AuthResponse login(LogInRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("User not found", HttpStatus.NOT_FOUND));

        if(user.getProvider() != AuthProvider.LOCAL){
            throw new AuthException("Use OAuth login instead", HttpStatus.BAD_REQUEST);
        }

        if(!passwordEncoder.matches(request.getPassword() , user.getPassword())){
            throw new AuthException("Invalid credential", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
