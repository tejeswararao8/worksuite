package com.hrms.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private static final String PREFIX = "blacklist:";

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // In-memory fallback when Redis is not available
    private final Map<String, Instant> localBlacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, long ttlSeconds) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(PREFIX + token, "1", Duration.ofSeconds(ttlSeconds));
        } else {
            localBlacklist.put(token, Instant.now().plusSeconds(ttlSeconds));
        }
    }

    public boolean isBlacklisted(String token) {
        if (redisTemplate != null) {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
        }
        Instant expiry = localBlacklist.get(token);
        if (expiry == null) return false;
        if (Instant.now().isAfter(expiry)) {
            localBlacklist.remove(token);
            return false;
        }
        return true;
    }
}
