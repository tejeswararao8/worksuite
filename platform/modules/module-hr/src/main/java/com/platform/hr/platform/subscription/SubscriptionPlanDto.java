package com.platform.hr.platform.subscription;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SubscriptionPlanDto {

    private UUID   id;
    private String name;
    private String description;
    private int    monthlyPriceCents;
    private int    annualPriceCents;
    private int    trialDays;
    private int    maxEmployees;
    private int    storageGb;
    private List<String> enabledModules;
    private SubscriptionPlan.PlanStatus planStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubscriptionPlanDto from(SubscriptionPlan p) {
        return SubscriptionPlanDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .monthlyPriceCents(p.getMonthlyPriceCents())
                .annualPriceCents(p.getAnnualPriceCents())
                .trialDays(p.getTrialDays())
                .maxEmployees(p.getMaxEmployees())
                .storageGb(p.getStorageGb())
                .enabledModules(p.getEnabledModuleList())
                .planStatus(p.getPlanStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
