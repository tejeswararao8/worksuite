package com.platform.hr.platform.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Request body for assigning a plan to a tenant (or changing plan). */
@Getter
@Setter
@NoArgsConstructor
public class AssignSubscriptionRequest {

    @NotNull
    private UUID planId;

    private TenantSubscription.BillingCycle billingCycle = TenantSubscription.BillingCycle.MONTHLY;

    /** Whether to start a trial period instead of activating immediately. */
    private boolean startTrial = false;

    private String notes;
}
