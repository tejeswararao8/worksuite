package com.platform.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Generates, validates and parses JWT tokens.
 * Access token: short-lived (default 24h)
 * Refresh token: long-lived (default 7d)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenMs;
    private final long refreshTokenMs;

    public JwtTokenProvider(
        @Value("${platform.jwt.secret}") String secret,
        @Value("${platform.jwt.access-token-ms:86400000}") long accessTokenMs,
        @Value("${platform.jwt.refresh-token-ms:604800000}") long refreshTokenMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMs = accessTokenMs;
        this.refreshTokenMs = refreshTokenMs;
    }

    public String generateAccessToken(UUID userId, UUID companyId, String email, String roles) {
        return buildToken(userId, companyId, email, roles, accessTokenMs, "access");
    }

    public String generateRefreshToken(UUID userId, UUID companyId) {
        return buildToken(userId, companyId, null, null, refreshTokenMs, "refresh");
    }

    private String buildToken(UUID userId, UUID companyId, String email, String roles,
                               long expiryMs, String type) {
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("companyId", companyId.toString());
        claims.put("type", type);
        if (email != null) claims.put("email", email);
        if (roles != null) claims.put("roles", roles);

        return Jwts.builder()
            .subject(userId.toString())
            .claims(claims)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiryMs))
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
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }

    public UUID getCompanyId(String token) {
        return UUID.fromString(parseToken(token).get("companyId", String.class));
    }

    public String getTokenType(String token) {
        return parseToken(token).get("type", String.class);
    }
}
