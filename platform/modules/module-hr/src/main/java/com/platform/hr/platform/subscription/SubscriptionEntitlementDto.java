package com.platform.hr.platform.subscription;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Resolved entitlements for a tenant based on their active subscription.
 * Returned to both platform admins and tenant users (tenant users see their own only).
 */
@Getter
@Builder
public class SubscriptionEntitlementDto {

    private boolean hasActiveSubscription;
    private TenantSubscription.SubscriptionStatus subscriptionStatus;
    private String planName;

    /** -1 = unlimited */
    private int maxEmployees;
    private long currentEmployeeCount;
    private boolean employeeLimitReached;

    /** -1 = unlimited */
    private int storageGb;

    private List<String> enabledModules;

    public boolean isModuleEnabled(String moduleKey) {
        if (enabledModules == null) return false;
        return enabledModules.stream().anyMatch(m -> m.equalsIgnoreCase(moduleKey));
    }
}
