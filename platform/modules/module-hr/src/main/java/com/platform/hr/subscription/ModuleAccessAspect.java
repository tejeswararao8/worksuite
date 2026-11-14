package com.platform.hr.subscription;

import com.platform.hr.platform.subscription.SubscriptionService;
import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * AOP aspect that enforces module-level subscription access.
 *
 * <p>Intercepts any method annotated with {@link EnforceModule} and calls
 * {@link SubscriptionService#enforceModuleAccess(UUID, String)} before the
 * method body executes.
 *
 * <p>The tenant ID is read from {@code TenantContext} (set by {@code TenantInterceptor}
 * from the JWT {@code companyId} claim). Platform admins bypass the check.
 *
 * <p>If the module is not enabled, a {@code TenantSecurityException} is thrown,
 * which {@code GlobalExceptionHandler} maps to HTTP 403.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ModuleAccessAspect {

    private final SubscriptionService subscriptionService;

    @Before("@annotation(enforceModule)")
    public void checkModuleAccess(EnforceModule enforceModule) {
        String tenantIdStr = TenantContext.getTenantId();
        if (tenantIdStr == null) {
            // No tenant context — request is either unauthenticated or platform-only.
            // Spring Security handles authentication; we skip the module check here.
            return;
        }

        UUID tenantId;
        try {
            tenantId = UUID.fromString(tenantIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("ModuleAccessAspect: malformed tenantId in context: {}", tenantIdStr);
            return;
        }

        // Platform admins bypass module checks — enforceModuleAccess handles this internally.
        subscriptionService.enforceModuleAccess(tenantId, enforceModule.value());
    }
}
