package com.platform.audit;

import java.lang.annotation.*;

/**
 * Annotate any service method to auto-capture audit log.
 * AuditAspect intercepts and logs CREATE/UPDATE/DELETE automatically.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String entity();
    AuditLog.AuditAction action();
}
