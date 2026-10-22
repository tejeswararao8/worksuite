package com.platform.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AOP-based logging for all service methods.
 * Logs method entry/exit and flags slow executions.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Value("${platform.logging.slow-threshold-ms:500}")
    private long slowThresholdMs;

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object logServiceCall(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;

            if (duration > slowThresholdMs) {
                log.warn("SLOW [{}] {}ms", method, duration);
            } else {
                log.debug("OK [{}] {}ms", method, duration);
            }

            return result;
        } catch (Exception e) {
            log.error("ERROR [{}] {}", method, e.getMessage());
            throw e;
        }
    }
}
