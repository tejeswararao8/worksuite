package com.platform.security.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * In-memory token blacklist using Caffeine cache.
 * Blacklisted tokens expire automatically after refresh token TTL.
 * No Redis needed — Caffeine handles it locally.
 */
@Component
public class TokenBlacklist {

    private final Cache<String, Boolean> blacklist;

    public TokenBlacklist(
        @Value("${platform.jwt.refresh-token-ms:604800000}") long refreshTokenMs
    ) {
        this.blacklist = Caffeine.newBuilder()
            .expireAfterWrite(refreshTokenMs, TimeUnit.MILLISECONDS)
            .maximumSize(10_000)
            .build();
    }

    public void blacklist(String token) {
        blacklist.put(token, Boolean.TRUE);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(blacklist.getIfPresent(token));
    }
}
