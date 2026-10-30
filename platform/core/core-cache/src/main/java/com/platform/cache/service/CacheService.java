package com.platform.cache.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ─────────────────────────────────────────────
    // SET - Store value with TTL
    // ─────────────────────────────────────────────
    public void set(String key, Object value, long minutes) {
        redisTemplate.opsForValue().set(key, value, minutes, TimeUnit.MINUTES);
        log.debug("Cache SET → key: {}, ttl: {} mins", key, minutes);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, 30, TimeUnit.MINUTES);
        log.debug("Cache SET → key: {}", key);
    }

    // ─────────────────────────────────────────────
    // GET - Retrieve value
    // ─────────────────────────────────────────────
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;
        return type.cast(value);
    }

    // ─────────────────────────────────────────────
    // DELETE - Remove value
    // ─────────────────────────────────────────────
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("Cache DELETE → key: {}", key);
    }

    public void deleteAll(Collection<String> keys) {
        redisTemplate.delete(keys);
        log.debug("Cache DELETE ALL → keys: {}", keys);
    }

    // ─────────────────────────────────────────────
    // EXISTS - Check if key exists
    // ─────────────────────────────────────────────
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ─────────────────────────────────────────────
    // EXPIRE - Update TTL
    // ─────────────────────────────────────────────
    public void expire(String key, long minutes) {
        redisTemplate.expire(key, minutes, TimeUnit.MINUTES);
    }

    // ─────────────────────────────────────────────
    // INCREMENT - Counter (rate limiting, attempts)
    // ─────────────────────────────────────────────
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long incrementBy(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ─────────────────────────────────────────────
    // PATTERN DELETE - Delete all keys matching pattern
    // Example: deleteByPattern("employees:*")
    // ─────────────────────────────────────────────
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Cache DELETE PATTERN → pattern: {}, count: {}", pattern, keys.size());
        }
    }

    // ─────────────────────────────────────────────
    // SESSION - User session management
    // ─────────────────────────────────────────────
    public void saveSession(String userId, Object sessionData) {
        set("session:" + userId, sessionData, 60); // 1 hour
    }

    public Object getSession(String userId) {
        return get("session:" + userId);
    }

    public void deleteSession(String userId) {
        delete("session:" + userId);
    }

    // ─────────────────────────────────────────────
    // RATE LIMIT - Login attempt protection
    // ─────────────────────────────────────────────
    public boolean isRateLimited(String identifier, int maxAttempts, long windowMinutes) {
        String key = "ratelimit:" + identifier;
        Long attempts = increment(key);
        if (attempts == 1) {
            expire(key, windowMinutes);
        }
        return attempts > maxAttempts;
    }

    public void resetRateLimit(String identifier) {
        delete("ratelimit:" + identifier);
    }
}
