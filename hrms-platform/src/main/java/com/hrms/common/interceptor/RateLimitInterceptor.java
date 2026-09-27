package com.hrms.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int    MAX_REQUESTS_PER_MINUTE = 60;
    private static final int    WINDOW_SECONDS          = 60;
    private static final String KEY_PREFIX              = "rate:";

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // In-memory fallback
    private final Map<String, AtomicInteger> localCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        String key = KEY_PREFIX + resolveIdentifier(request);
        long count;

        if (redisTemplate != null) {
            Long c = redisTemplate.opsForValue().increment(key);
            if (c == null) return true;
            if (c == 1) redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
            count = c;
        } else {
            count = localCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        }

        response.setHeader("X-RateLimit-Limit",     String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, MAX_REQUESTS_PER_MINUTE - count)));

        if (count > MAX_REQUESTS_PER_MINUTE) {
            response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
            response.sendError(429, "Too many requests — slow down");
            log.warn("Rate limit exceeded for key={}", key);
            return false;
        }
        return true;
    }

    private String resolveIdentifier(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }
        String ip = request.getHeader("X-Forwarded-For");
        return "ip:" + (ip != null ? ip.split(",")[0].trim() : request.getRemoteAddr());
    }
}
