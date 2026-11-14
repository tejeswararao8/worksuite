package com.platform.hr.platform.subscription;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TenantSubscriptionDto {

    private UUID   id;
    private UUID   tenantId;
    private String tenantName;
    private SubscriptionPlanDto plan;
    private TenantSubscription.SubscriptionStatus status;
    private TenantSubscription.BillingCycle billingCycle;
    private LocalDateTime startedAt;
    private LocalDateTime trialEndsAt;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime cancelledAt;
    private String notes;
    private String assignedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TenantSubscriptionDto from(TenantSubscription s) {
        return TenantSubscriptionDto.builder()
                .id(s.getId())
                .tenantId(s.getTenant().getId())
                .tenantName(s.getTenant().getName())
                .plan(SubscriptionPlanDto.from(s.getPlan()))
                .status(s.getStatus())
                .billingCycle(s.getBillingCycle())
                .startedAt(s.getStartedAt())
                .trialEndsAt(s.getTrialEndsAt())
                .currentPeriodStart(s.getCurrentPeriodStart())
                .currentPeriodEnd(s.getCurrentPeriodEnd())
                .cancelledAt(s.getCancelledAt())
                .notes(s.getNotes())
                .assignedBy(s.getAssignedBy())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
