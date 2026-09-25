package com.hrms.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(UUID userId, UUID companyId, String email) {
        return buildToken(userId, companyId, email, expirationMs, "ACCESS");
    }

    public String generateRefreshToken(UUID userId, UUID companyId, String email) {
        return buildToken(userId, companyId, email, refreshExpirationMs, "REFRESH");
    }

    private String buildToken(UUID userId, UUID companyId, String email, long expiry, String type) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("companyId", companyId.toString())
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiry))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return parseToken(token).getSubject();
    }

    public UUID getCompanyIdFromToken(String token) {
        return UUID.fromString(parseToken(token).get("companyId", String.class));
    }
}
