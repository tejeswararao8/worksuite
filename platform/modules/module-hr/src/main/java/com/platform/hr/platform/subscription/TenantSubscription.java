package com.platform.hr.platform.subscription;

import com.platform.tenancy.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The active (or most recent) subscription for a tenant.
 *
 * <p>Only one non-terminal subscription per tenant is allowed at a time,
 * enforced by a partial unique index on (tenant_id) WHERE status IN
 * ('TRIAL','ACTIVE','PAST_DUE','SUSPENDED').
 */
@Entity
@Table(name = "tenant_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 10)
    @Builder.Default
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Keycloak user ID of the platform admin who assigned this subscription. */
    @Column(name = "assigned_by", nullable = false, length = 255)
    private String assignedBy;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isTerminal() {
        return status == SubscriptionStatus.EXPIRED || status == SubscriptionStatus.CANCELLED;
    }

    public boolean isTrialExpired() {
        return status == SubscriptionStatus.TRIAL
                && trialEndsAt != null
                && LocalDateTime.now().isAfter(trialEndsAt);
    }

    /**
     * Returns true if a transition from the current status to {@code next} is valid.
     *
     * <p>State machine:
     * <pre>
     *   TRIAL      → ACTIVE | EXPIRED | CANCELLED | SUSPENDED
     *   ACTIVE     → PAST_DUE | SUSPENDED | CANCELLED | EXPIRED
     *   PAST_DUE   → ACTIVE | SUSPENDED | CANCELLED | EXPIRED
     *   SUSPENDED  → ACTIVE | CANCELLED | EXPIRED
     *   EXPIRED    → (terminal — no transitions)
     *   CANCELLED  → (terminal — no transitions)
     * </pre>
     */
    public boolean canTransitionTo(SubscriptionStatus next) {
        return switch (status) {
            case TRIAL     -> next == SubscriptionStatus.ACTIVE
                           || next == SubscriptionStatus.EXPIRED
                           || next == SubscriptionStatus.CANCELLED
                           || next == SubscriptionStatus.SUSPENDED;
            case ACTIVE    -> next == SubscriptionStatus.PAST_DUE
                           || next == SubscriptionStatus.SUSPENDED
                           || next == SubscriptionStatus.CANCELLED
                           || next == SubscriptionStatus.EXPIRED;
            case PAST_DUE  -> next == SubscriptionStatus.ACTIVE
                           || next == SubscriptionStatus.SUSPENDED
                           || next == SubscriptionStatus.CANCELLED
                           || next == SubscriptionStatus.EXPIRED;
            case SUSPENDED -> next == SubscriptionStatus.ACTIVE
                           || next == SubscriptionStatus.CANCELLED
                           || next == SubscriptionStatus.EXPIRED;
            case EXPIRED, CANCELLED -> false;
        };
    }

    public enum SubscriptionStatus {
        TRIAL, ACTIVE, PAST_DUE, SUSPENDED, EXPIRED, CANCELLED
    }

    public enum BillingCycle {
        MONTHLY, ANNUAL
    }
}
