package com.platform.hr.platform.subscription;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SubscriptionHistoryDto {

    private UUID   id;
    private UUID   subscriptionId;
    private UUID   tenantId;
    private UUID   planId;
    private TenantSubscription.SubscriptionStatus fromStatus;
    private TenantSubscription.SubscriptionStatus toStatus;
    private UUID   fromPlanId;
    private UUID   toPlanId;
    private String actorId;
    private String reason;
    private LocalDateTime occurredAt;

    public static SubscriptionHistoryDto from(SubscriptionHistory h) {
        return SubscriptionHistoryDto.builder()
                .id(h.getId())
                .subscriptionId(h.getSubscriptionId())
                .tenantId(h.getTenantId())
                .planId(h.getPlanId())
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .fromPlanId(h.getFromPlanId())
                .toPlanId(h.getToPlanId())
                .actorId(h.getActorId())
                .reason(h.getReason())
                .occurredAt(h.getOccurredAt())
                .build();
    }
}
