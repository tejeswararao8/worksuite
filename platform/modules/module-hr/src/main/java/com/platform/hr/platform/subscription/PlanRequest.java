package com.platform.hr.platform.subscription;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for creating or updating a subscription plan. */
@Getter
@Setter
@NoArgsConstructor
public class PlanRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 2000)
    private String description;

    /** Monthly price in cents — must be non-negative. */
    @Min(0)
    private int monthlyPriceCents;

    /** Annual price in cents — must be non-negative. */
    @Min(0)
    private int annualPriceCents;

    @Min(0)
    private int trialDays = 14;

    /** -1 = unlimited */
    @Min(-1)
    private int maxEmployees = 50;

    /** -1 = unlimited */
    @Min(-1)
    private int storageGb = 5;

    /** Comma-separated module keys, e.g. "HR,PAYROLL" */
    @NotBlank
    private String enabledModules = "HR";
}
