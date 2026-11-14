package com.platform.hr.platform.subscription;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A platform-managed subscription plan.
 *
 * <p>Prices are stored server-side in integer cents and are NEVER accepted
 * from the browser. The frontend reads prices from this entity via the API.
 */
@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Monthly price in smallest currency unit (e.g. USD cents). */
    @Column(name = "monthly_price_cents", nullable = false)
    private int monthlyPriceCents;

    /** Annual price in smallest currency unit. */
    @Column(name = "annual_price_cents", nullable = false)
    private int annualPriceCents;

    @Column(name = "trial_days", nullable = false)
    private int trialDays;

    /** Maximum number of employees. -1 means unlimited. */
    @Column(name = "max_employees", nullable = false)
    private int maxEmployees;

    /** Storage quota in GB. -1 means unlimited. */
    @Column(name = "storage_gb", nullable = false)
    private int storageGb;

    /**
     * Comma-separated list of enabled module keys (e.g. "HR,PAYROLL,RECRUITMENT").
     * Never null — defaults to "HR".
     */
    @Column(name = "enabled_modules", nullable = false, columnDefinition = "TEXT")
    private String enabledModules;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", nullable = false, length = 20)
    @Builder.Default
    private PlanStatus planStatus = PlanStatus.ACTIVE;

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

    public List<String> getEnabledModuleList() {
        if (enabledModules == null || enabledModules.isBlank()) return List.of();
        return Arrays.stream(enabledModules.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public boolean isModuleEnabled(String moduleKey) {
        return getEnabledModuleList().stream()
                .anyMatch(m -> m.equalsIgnoreCase(moduleKey));
    }

    public enum PlanStatus {
        ACTIVE, INACTIVE
    }
}
