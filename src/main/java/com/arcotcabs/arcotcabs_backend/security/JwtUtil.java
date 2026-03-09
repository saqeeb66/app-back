package com.arcotcabs.arcotcabs_backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET =
            "ARCOT_CABS_SECRET_2026_ARCOT_CABS_SECRET_2026";

    private static final long EXPIRATION =
            1000 * 60 * 60 * 24; // 24 hours

    @Bean
    public SecretKey jwtKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String userId, String role) {

        return Jwts.builder()
                .setSubject(userId)
                .addClaims(Map.of(
                        "role", "ROLE_" + role   // ✅ VERY IMPORTANT
                ))
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION)
                )
                .signWith(jwtKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
