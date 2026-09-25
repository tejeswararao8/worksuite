package com.hrms.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /** All classes in any service package across all modules. */
    @Pointcut("within(com.hrms..service..*)")
    private void serviceLayer() {}

    /** All Spring @Repository beans. */
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    private void repositoryLayer() {}

    @Around("serviceLayer()")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        return logExecution(pjp, false);
    }

    @Around("repositoryLayer()")
    public Object logRepository(ProceedingJoinPoint pjp) throws Throwable {
        return logExecution(pjp, true);
    }

    private Object logExecution(ProceedingJoinPoint pjp, boolean isRepo) throws Throwable {
        String className  = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        long start = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug(">> {}.{}()", className, methodName);
        }

        try {
            Object result = pjp.proceed();
            if (log.isDebugEnabled()) {
                log.debug("<< {}.{}() | {}ms", className, methodName, System.currentTimeMillis() - start);
            }
            return result;
        } catch (Exception ex) {
            log.error("!! {}.{}() threw {} | {}ms | message={}",
                    className, methodName,
                    ex.getClass().getSimpleName(),
                    System.currentTimeMillis() - start,
                    ex.getMessage());
            throw ex;
        }
    }
}
