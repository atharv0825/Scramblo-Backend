package com.scramble.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtDecoder jwtDecoder; // ✅ ADD THIS

    public SecurityConfig(CustomAuthenticationProvider provider,
                          CustomOAuth2UserService userService,
                          OAuth2SuccessHandler successHandler,
                          JwtDecoder jwtDecoder) { // ✅ ADD THIS
        this.customAuthenticationProvider = provider;
        this.customOAuth2UserService = userService;
        this.oAuth2SuccessHandler = successHandler;
        this.jwtDecoder = jwtDecoder; // ✅ ADD THIS
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(customAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/api/users/default-images",
                                "/api/articles/trending",
                                "/api/articles/recent"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(user ->
                                user.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt -> jwt.decoder(jwtDecoder))
                );

        return http.build();
    }
}