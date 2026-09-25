package com.hrms.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class HttpLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID   = "traceId";
    public static final String USER_ID    = "userId";
    public static final String COMPANY_ID = "companyId";
    private static final String X_TRACE_ID = "X-Trace-Id";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        long start = System.currentTimeMillis();

        MDC.put(TRACE_ID, traceId);
        MDC.put("httpMethod", request.getMethod());
        MDC.put("requestUri", request.getRequestURI());
        response.setHeader(X_TRACE_ID, traceId);

        try {
            log.info(">>> {} {}", request.getMethod(), request.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            log.info("<<< {} {} | status={} | {}ms",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), System.currentTimeMillis() - start);
            MDC.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(X_TRACE_ID);
        return (incoming != null && !incoming.isBlank()) ? incoming : UUID.randomUUID().toString();
    }

    /** Called from JwtAuthenticationFilter after token validation to enrich MDC with auth context. */
    public static void enrichMdc(String userId, String companyId) {
        if (userId != null)    MDC.put(USER_ID, userId);
        if (companyId != null) MDC.put(COMPANY_ID, companyId);
    }
}
