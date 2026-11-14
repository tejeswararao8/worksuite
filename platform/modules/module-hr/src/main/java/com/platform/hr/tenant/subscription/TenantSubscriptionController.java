package com.platform.hr.tenant.subscription;

import com.platform.api.ApiResponse;
import com.platform.hr.platform.subscription.SubscriptionEntitlementDto;
import com.platform.hr.platform.subscription.SubscriptionService;
import com.platform.hr.platform.subscription.TenantSubscriptionDto;
import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Tenant-facing subscription endpoints.
 *
 * <p>Tenant users can view their own subscription and entitlements.
 * They cannot modify subscriptions — that is platform-admin only.
 *
 * <p>The tenant ID is read from the JWT {@code companyId} claim via TenantContext,
 * never from a request parameter, preventing cross-tenant data access.
 */
@RestController
@RequestMapping("/api/v1/tenant/subscription")
@RequiredArgsConstructor
public class TenantSubscriptionController {

    private final SubscriptionService subscriptionService;

    private static final String TENANT_ROLES =
            "hasAnyAuthority('" + PlatformRoles.TENANT_OWNER + "', '" +
            PlatformRoles.TENANT_ADMIN + "', '" +
            PlatformRoles.HR_ADMIN + "', '" +
            PlatformRoles.HR_MANAGER + "', '" +
            PlatformRoles.EMPLOYEE + "')";

    @GetMapping
    @PreAuthorize(TENANT_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> getMySubscription() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.getActiveSubscriptionForTenant(tenantId)));
    }

    @GetMapping("/entitlements")
    @PreAuthorize(TENANT_ROLES)
    public ResponseEntity<ApiResponse<SubscriptionEntitlementDto>> getMyEntitlements() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.getEntitlements(tenantId)));
    }
}
