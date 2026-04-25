package com.scramble.security;

import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(String email, String roles) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(() -> "HS256").build(),
                        claims
                )
        ).getTokenValue();
    }
}