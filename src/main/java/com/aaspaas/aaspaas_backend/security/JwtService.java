package com.aaspaas.aaspaas_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration
    ) {

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.accessTokenExpiration = accessTokenExpiration;
    }

    // =====================================================
    // Generate Access Token
    // =====================================================

    public String generateAccessToken(
            Long userId,
            String phone
    ) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + accessTokenExpiration
        );

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // =====================================================
    // Extract User ID
    // =====================================================

    public Long extractUserId(String token) {

        Claims claims = parseToken(token);

        return Long.valueOf(
                claims.getSubject()
        );
    }

    // =====================================================
    // Extract Phone
    // =====================================================

    public String extractPhone(String token) {

        Claims claims = parseToken(token);

        return claims.get(
                "phone",
                String.class
        );
    }

    // =====================================================
    // Validate Token
    // =====================================================

    public boolean isTokenValid(String token) {

        try {

            parseToken(token);

            return true;

        } catch (Exception ex) {

            return false;
        }
    }

    // =====================================================
    // Parse & Verify JWT
    // =====================================================

    private Claims parseToken(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =====================================================
    // Access Token Expiration
    // =====================================================

    public long getAccessTokenExpirationSeconds() {

        return accessTokenExpiration / 1000;
    }
}