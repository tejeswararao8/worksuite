package com.platform.hr.platform.subscription;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit record of every subscription status or plan change.
 * Never updated or deleted.
 */
@Entity
@Table(name = "subscription_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private TenantSubscription.SubscriptionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private TenantSubscription.SubscriptionStatus toStatus;

    @Column(name = "from_plan_id")
    private UUID fromPlanId;

    @Column(name = "to_plan_id", nullable = false)
    private UUID toPlanId;

    @Column(name = "actor_id", nullable = false, length = 255)
    private String actorId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    protected void prePersist() {
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }
}
