package com.platform.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        String ipAddress = getIpAddress();
        Object result = pjp.proceed();

        try {
            String entityId = extractEntityId(result);
            String newValue = objectMapper.writeValueAsString(result);
            auditService.log(auditable.entity(), entityId, auditable.action(), null, newValue, ipAddress);
        } catch (Exception e) {
            log.warn("Audit logging failed: {}", e.getMessage());
        }

        return result;
    }

    private String extractEntityId(Object result) {
        if (result == null) return "unknown";
        try {
            var idMethod = result.getClass().getMethod("getId");
            Object id = idMethod.invoke(result);
            return id != null ? id.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getIpAddress() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String ip = req.getHeader("X-Forwarded-For");
                return ip != null ? ip.split(",")[0].trim() : req.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
