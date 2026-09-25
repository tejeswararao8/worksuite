package com.hrms.common.interceptor;

import com.hrms.common.util.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Slf4j
@Component
public class TenantValidationInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/auth/", "/swagger-ui", "/api-docs", "/actuator"
    );

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;

        String uri = request.getRequestURI();
        boolean isPublic = PUBLIC_PREFIXES.stream().anyMatch(uri::contains);
        if (isPublic) return true;

        if (TenantContext.getCurrentTenant() == null) {
            log.warn("Request to {} rejected — no tenant context", uri);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenant context missing");
            return false;
        }
        return true;
    }
}
