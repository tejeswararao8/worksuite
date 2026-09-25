package com.hrms.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int    MAX_REQUESTS_PER_MINUTE = 60;
    private static final int    WINDOW_SECONDS          = 60;
    private static final String KEY_PREFIX              = "rate:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        String key = KEY_PREFIX + resolveIdentifier(request);

        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) return true;

        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        response.setHeader("X-RateLimit-Limit",     String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, MAX_REQUESTS_PER_MINUTE - count)));

        if (count > MAX_REQUESTS_PER_MINUTE) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            response.setHeader("Retry-After", String.valueOf(ttl != null ? ttl : WINDOW_SECONDS));
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
