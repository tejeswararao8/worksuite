package com.hrms.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Slf4j
@Component
public class AuditTrailInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private static final ThreadLocal<AuditContext> AUDIT_CONTEXT = new ThreadLocal<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        if (WRITE_METHODS.contains(request.getMethod())) {
            AUDIT_CONTEXT.set(new AuditContext(
                    request.getMethod(),
                    request.getRequestURI(),
                    resolveClientIp(request)
            ));
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        AUDIT_CONTEXT.remove();
    }

    public static AuditContext current() {
        return AUDIT_CONTEXT.get();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    public record AuditContext(String httpMethod, String requestUri, String clientIp) {}
}
