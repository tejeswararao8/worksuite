package com.platform.hr.platform.subscription;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resolved entitlements for a tenant based on their active subscription.
 *
 * <p>Returned to both platform admins (for any tenant) and tenant users (own tenant only).
 * All fields are computed server-side — nothing is accepted from the browser.
 */
@Getter
@Builder
public class SubscriptionEntitlementDto {

    private boolean hasActiveSubscription;
    private TenantSubscription.SubscriptionStatus subscriptionStatus;
    private String planName;

    // ── Employee limits ───────────────────────────────────────────────────────

    /** -1 = unlimited */
    private int maxEmployees;
    private long currentEmployeeCount;
    private boolean employeeLimitReached;

    // ── Storage ───────────────────────────────────────────────────────────────

    /** Storage quota in GB. -1 = unlimited. */
    private int storageGb;

    /** Bytes currently used by this tenant. */
    private long storageUsedBytes;

    /** True when storageGb != -1 and storageUsedBytes >= storageGb * 1_073_741_824. */
    private boolean storageQuotaReached;

    // ── Modules ───────────────────────────────────────────────────────────────

    private List<String> enabledModules;

    // ── Dates ─────────────────────────────────────────────────────────────────

    private LocalDateTime subscriptionStartedAt;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime trialEndsAt;
    private LocalDateTime cancelledAt;

    // ── Grace period ──────────────────────────────────────────────────────────

    /**
     * True when the subscription is PAST_DUE and still within the grace period.
     *
     * <p>Grace period policy: PAST_DUE subscriptions retain full access for
     * {@link #GRACE_PERIOD_DAYS} days after the current period end. After that,
     * the subscription should be moved to SUSPENDED by a scheduled job (Phase 3C.6).
     * This flag is informational — enforcement is done by checking the status.
     */
    private boolean inGracePeriod;

    /** Number of days remaining in the grace period, or 0 if not applicable. */
    private int gracePeriodDaysRemaining;

    /**
     * Default grace period for PAST_DUE subscriptions (days).
     *
     * <p><strong>Do not use this constant for enforcement logic.</strong>
     * The authoritative value is {@code platform.subscription.grace-period-days}
     * in application.yml, injected into {@code SubscriptionService} and
     * {@code SubscriptionLifecycleScheduler} via {@code @Value}.
     * This constant exists only as a fallback default reference.
     */
    public static final int GRACE_PERIOD_DAYS = 7;

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean isModuleEnabled(String moduleKey) {
        if (enabledModules == null) return false;
        return enabledModules.stream().anyMatch(m -> m.equalsIgnoreCase(moduleKey));
    }

    /**
     * Returns true if the tenant can perform subscription-restricted operations.
     *
     * <p>Allowed states: TRIAL, ACTIVE, PAST_DUE (within grace period).
     * Blocked states: SUSPENDED, EXPIRED, CANCELLED, no subscription.
     */
    public boolean isOperationallyActive() {
        if (!hasActiveSubscription || subscriptionStatus == null) return false;
        return switch (subscriptionStatus) {
            case TRIAL, ACTIVE -> true;
            case PAST_DUE -> inGracePeriod;
            case SUSPENDED, EXPIRED, CANCELLED -> false;
        };
    }
}
