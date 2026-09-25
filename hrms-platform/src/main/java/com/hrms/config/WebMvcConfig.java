package com.hrms.config;

import com.hrms.common.interceptor.AuditTrailInterceptor;
import com.hrms.common.interceptor.RateLimitInterceptor;
import com.hrms.common.interceptor.RequestLoggingInterceptor;
import com.hrms.common.interceptor.TenantValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor   requestLoggingInterceptor;
    private final TenantValidationInterceptor tenantValidationInterceptor;
    private final RateLimitInterceptor        rateLimitInterceptor;
    private final AuditTrailInterceptor       auditTrailInterceptor;

    private static final String[] EXCLUDE_PATHS = {
            "/swagger-ui/**", "/api-docs/**", "/actuator/**"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1. Request logging — runs first, covers all paths
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS)
                .order(1);

        // 2. Tenant validation — after logging, before business logic
        registry.addInterceptor(tenantValidationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS)
                .order(2);

        // 3. Rate limiting — applied to all authenticated endpoints
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS)
                .order(3);

        // 4. Audit trail — captures write-operation context for AuditService
        registry.addInterceptor(auditTrailInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS)
                .order(4);
    }
}
