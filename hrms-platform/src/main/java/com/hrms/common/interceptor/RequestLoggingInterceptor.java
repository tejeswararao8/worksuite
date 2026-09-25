package com.hrms.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String ATTR_START = "reqStartTime";
    private static final String MDC_HANDLER = "handler";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        request.setAttribute(ATTR_START, System.currentTimeMillis());
        if (handler instanceof HandlerMethod hm) {
            String name = hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName();
            MDC.put(MDC_HANDLER, name);
            log.debug("Handling {} {} -> {}", request.getMethod(), request.getRequestURI(), name);
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        Long start = (Long) request.getAttribute(ATTR_START);
        if (start != null && handler instanceof HandlerMethod hm) {
            long ms = System.currentTimeMillis() - start;
            if (ex != null) {
                log.error("Completed {}#{} | status={} | {}ms | error={}",
                        hm.getBeanType().getSimpleName(), hm.getMethod().getName(),
                        response.getStatus(), ms, ex.getMessage());
            } else {
                log.debug("Completed {}#{} | status={} | {}ms",
                        hm.getBeanType().getSimpleName(), hm.getMethod().getName(),
                        response.getStatus(), ms);
            }
        }
        MDC.remove(MDC_HANDLER);
    }
}
