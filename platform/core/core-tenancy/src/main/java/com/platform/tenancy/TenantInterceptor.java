package com.platform.tenancy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts every HTTP request.
 * Extracts tenantId from request attribute (set by JwtAuthFilter after token validation)
 * and stores it in TenantContext for the duration of the request.
 */
@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    public static final String TENANT_ATTRIBUTE = "tenantId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = (String) request.getAttribute(TENANT_ATTRIBUTE);
        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
            log.debug("Tenant set: {}", tenantId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
