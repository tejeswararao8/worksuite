package com.platform.hr.platform.subscription;

import com.platform.tenancy.Tenant;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.TenantSecurityService;
import com.platform.tenancy.membership.MembershipStatus;
import com.platform.tenancy.membership.UserTenantMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Platform subscription management service.
 *
 * <p>Security invariants:
 * <ul>
 *   <li>Plan prices are NEVER accepted from the caller — only plan IDs.</li>
 *   <li>Tenant IDs are validated against the database — never trusted from the browser.</li>
 *   <li>The platform pseudo-tenant cannot have a subscription.</li>
 *   <li>All state transitions are validated by the entity state machine.</li>
 *   <li>Every change is recorded in subscription_history.</li>
 *   <li>Employee limit and storage quota checks use pessimistic locking to prevent
 *       concurrent over-allocation.</li>
 * </ul>
 *
 * <p>Subscription status enforcement policy:
 * <ul>
 *   <li>TRIAL / ACTIVE: full access within plan limits.</li>
 *   <li>PAST_DUE: full access for {@link SubscriptionEntitlementDto#GRACE_PERIOD_DAYS} days
 *       after period end, then blocked (SUSPENDED by scheduler in Phase 3C.6).</li>
 *   <li>SUSPENDED / EXPIRED / CANCELLED: subscription-restricted operations blocked.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final long BYTES_PER_GB = 1_073_741_824L;

    @Value("${platform.subscription.grace-period-days:7}")
    private int gracePeriodDays;

    private final SubscriptionPlanRepository planRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantMembershipRepository membershipRepository;

    // ── Plan management ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<SubscriptionPlanDto> listPlans(int page, int size, SubscriptionPlan.PlanStatus status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<SubscriptionPlan> plans = status != null
                ? planRepository.findByPlanStatus(status, pageable)
                : planRepository.findAll(pageable);
        return plans.map(SubscriptionPlanDto::from);
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanDto getPlan(UUID planId) {
        return SubscriptionPlanDto.from(requirePlan(planId));
    }

    @Transactional
    public SubscriptionPlanDto createPlan(PlanRequest req) {
        if (planRepository.existsByName(req.getName())) {
            throw new TenantException("A plan with name '" + req.getName() + "' already exists");
        }
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(req.getName())
                .description(req.getDescription())
                .monthlyPriceCents(req.getMonthlyPriceCents())
                .annualPriceCents(req.getAnnualPriceCents())
                .trialDays(req.getTrialDays())
                .maxEmployees(req.getMaxEmployees())
                .storageGb(req.getStorageGb())
                .enabledModules(req.getEnabledModules())
                .planStatus(SubscriptionPlan.PlanStatus.ACTIVE)
                .build();
        plan = planRepository.save(plan);
        log.info("Subscription plan created: id={} name={}", plan.getId(), plan.getName());
        return SubscriptionPlanDto.from(plan);
    }

    @Transactional
    public SubscriptionPlanDto updatePlan(UUID planId, PlanRequest req) {
        SubscriptionPlan plan = requirePlan(planId);
        plan.setName(req.getName());
        plan.setDescription(req.getDescription());
        plan.setMonthlyPriceCents(req.getMonthlyPriceCents());
        plan.setAnnualPriceCents(req.getAnnualPriceCents());
        plan.setTrialDays(req.getTrialDays());
        plan.setMaxEmployees(req.getMaxEmployees());
        plan.setStorageGb(req.getStorageGb());
        plan.setEnabledModules(req.getEnabledModules());
        planRepository.save(plan);
        log.info("Subscription plan updated: id={}", planId);
        return SubscriptionPlanDto.from(plan);
    }

    @Transactional
    public SubscriptionPlanDto deactivatePlan(UUID planId) {
        SubscriptionPlan plan = requirePlan(planId);
        plan.setPlanStatus(SubscriptionPlan.PlanStatus.INACTIVE);
        planRepository.save(plan);
        log.info("Subscription plan deactivated: id={}", planId);
        return SubscriptionPlanDto.from(plan);
    }

    @Transactional
    public SubscriptionPlanDto activatePlan(UUID planId) {
        SubscriptionPlan plan = requirePlan(planId);
        plan.setPlanStatus(SubscriptionPlan.PlanStatus.ACTIVE);
        planRepository.save(plan);
        log.info("Subscription plan activated: id={}", planId);
        return SubscriptionPlanDto.from(plan);
    }

    // ── Tenant subscription management ────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TenantSubscriptionDto> listAllSubscriptions(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return subscriptionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(TenantSubscriptionDto::from);
    }

    @Transactional(readOnly = true)
    public TenantSubscriptionDto getSubscription(UUID subscriptionId) {
        return TenantSubscriptionDto.from(requireSubscription(subscriptionId));
    }

    @Transactional(readOnly = true)
    public TenantSubscriptionDto getActiveSubscriptionForTenant(UUID tenantId) {
        requireTenant(tenantId);
        return subscriptionRepository.findActiveByTenantId(tenantId)
                .map(TenantSubscriptionDto::from)
                .orElseThrow(() -> new TenantException("No active subscription for tenant: " + tenantId));
    }

    @Transactional(readOnly = true)
    public Page<TenantSubscriptionDto> getSubscriptionHistoryForTenant(UUID tenantId, int page, int size) {
        requireTenant(tenantId);
        PageRequest pageable = PageRequest.of(page, size);
        return subscriptionRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable)
                .map(TenantSubscriptionDto::from);
    }

    /**
     * Assigns a plan to a tenant. If the tenant already has a non-terminal subscription,
     * it is cancelled and replaced. This covers both initial assignment and plan changes.
     */
    @Transactional
    public TenantSubscriptionDto assignSubscription(UUID tenantId, AssignSubscriptionRequest req) {
        Tenant tenant = requireTenant(tenantId);
        SubscriptionPlan plan = requireActivePlan(req.getPlanId());

        subscriptionRepository.findActiveByTenantId(tenantId).ifPresent(existing -> {
            UUID oldPlanId = existing.getPlan().getId();
            existing.setStatus(TenantSubscription.SubscriptionStatus.CANCELLED);
            existing.setCancelledAt(LocalDateTime.now());
            subscriptionRepository.save(existing);
            appendHistory(existing, TenantSubscription.SubscriptionStatus.CANCELLED,
                    oldPlanId, plan.getId(), "Replaced by new subscription assignment");
            log.info("Previous subscription cancelled on reassignment: id={}", existing.getId());
        });

        LocalDateTime now = LocalDateTime.now();
        TenantSubscription.SubscriptionStatus initialStatus = req.isStartTrial()
                ? TenantSubscription.SubscriptionStatus.TRIAL
                : TenantSubscription.SubscriptionStatus.ACTIVE;

        LocalDateTime trialEndsAt = req.isStartTrial()
                ? now.plusDays(plan.getTrialDays())
                : null;

        LocalDateTime periodEnd = req.getBillingCycle() == TenantSubscription.BillingCycle.ANNUAL
                ? now.plusYears(1)
                : now.plusMonths(1);

        TenantSubscription sub = TenantSubscription.builder()
                .tenant(tenant)
                .plan(plan)
                .status(initialStatus)
                .billingCycle(req.getBillingCycle() != null
                        ? req.getBillingCycle()
                        : TenantSubscription.BillingCycle.MONTHLY)
                .startedAt(now)
                .trialEndsAt(trialEndsAt)
                .currentPeriodStart(now)
                .currentPeriodEnd(periodEnd)
                .notes(req.getNotes())
                .assignedBy(currentActorId())
                .build();

        sub = subscriptionRepository.save(sub);
        appendHistory(sub, initialStatus, null, plan.getId(), "Initial subscription assignment");
        log.info("Subscription assigned: tenantId={} planId={} status={}", tenantId, plan.getId(), initialStatus);
        return TenantSubscriptionDto.from(sub);
    }

    @Transactional
    public TenantSubscriptionDto activate(UUID subscriptionId, SubscriptionActionRequest req) {
        return transition(subscriptionId, TenantSubscription.SubscriptionStatus.ACTIVE,
                req != null ? req.getReason() : null);
    }

    @Transactional
    public TenantSubscriptionDto cancel(UUID subscriptionId, SubscriptionActionRequest req) {
        TenantSubscription sub = requireSubscription(subscriptionId);
        UUID planId = sub.getPlan().getId();
        transition(sub, TenantSubscription.SubscriptionStatus.CANCELLED,
                req != null ? req.getReason() : null, planId, planId);
        sub.setCancelledAt(LocalDateTime.now());
        subscriptionRepository.save(sub);
        log.info("Subscription cancelled: id={}", subscriptionId);
        return TenantSubscriptionDto.from(sub);
    }

    @Transactional
    public TenantSubscriptionDto suspend(UUID subscriptionId, SubscriptionActionRequest req) {
        return transition(subscriptionId, TenantSubscription.SubscriptionStatus.SUSPENDED,
                req != null ? req.getReason() : null);
    }

    @Transactional
    public TenantSubscriptionDto reactivate(UUID subscriptionId, SubscriptionActionRequest req) {
        return transition(subscriptionId, TenantSubscription.SubscriptionStatus.ACTIVE,
                req != null ? req.getReason() : null);
    }

    @Transactional
    public TenantSubscriptionDto markPastDue(UUID subscriptionId, SubscriptionActionRequest req) {
        return transition(subscriptionId, TenantSubscription.SubscriptionStatus.PAST_DUE,
                req != null ? req.getReason() : null);
    }

    @Transactional
    public TenantSubscriptionDto renew(UUID subscriptionId, SubscriptionActionRequest req) {
        TenantSubscription sub = requireSubscription(subscriptionId);
        if (sub.getStatus() != TenantSubscription.SubscriptionStatus.ACTIVE
                && sub.getStatus() != TenantSubscription.SubscriptionStatus.PAST_DUE) {
            throw new TenantException("Only ACTIVE or PAST_DUE subscriptions can be renewed");
        }
        LocalDateTime newStart = LocalDateTime.now();
        LocalDateTime newEnd = sub.getBillingCycle() == TenantSubscription.BillingCycle.ANNUAL
                ? newStart.plusYears(1)
                : newStart.plusMonths(1);
        sub.setCurrentPeriodStart(newStart);
        sub.setCurrentPeriodEnd(newEnd);
        sub.setStatus(TenantSubscription.SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(sub);
        appendHistory(sub, TenantSubscription.SubscriptionStatus.ACTIVE,
                sub.getPlan().getId(), sub.getPlan().getId(),
                req != null ? req.getReason() : "Renewal");
        log.info("Subscription renewed: id={} newPeriodEnd={}", subscriptionId, newEnd);
        return TenantSubscriptionDto.from(sub);
    }

    @Transactional
    public TenantSubscriptionDto changePlan(UUID subscriptionId, UUID newPlanId, String reason) {
        TenantSubscription sub = requireSubscription(subscriptionId);
        if (sub.isTerminal()) {
            throw new TenantException("Cannot change plan on a terminal subscription");
        }
        SubscriptionPlan newPlan = requireActivePlan(newPlanId);
        UUID oldPlanId = sub.getPlan().getId();
        sub.setPlan(newPlan);
        subscriptionRepository.save(sub);
        appendHistory(sub, sub.getStatus(), oldPlanId, newPlanId,
                reason != null ? reason : "Plan change");
        log.info("Subscription plan changed: id={} from={} to={}", subscriptionId, oldPlanId, newPlanId);
        return TenantSubscriptionDto.from(sub);
    }

    // ── Entitlement resolution ────────────────────────────────────────────────

    /**
     * Returns the fully resolved entitlements for a tenant.
     * Used by backend services to enforce limits — never relies on frontend data.
     */
    @Transactional(readOnly = true)
    public SubscriptionEntitlementDto getEntitlements(UUID tenantId) {
        long employeeCount = membershipRepository
                .countByTenantIdAndMembershipStatus(tenantId, MembershipStatus.ACTIVE);

        return subscriptionRepository.findActiveByTenantId(tenantId)
                .map(sub -> buildEntitlements(sub, employeeCount))
                .orElseGet(() -> SubscriptionEntitlementDto.builder()
                        .hasActiveSubscription(false)
                        .subscriptionStatus(null)
                        .planName(null)
                        .maxEmployees(0)
                        .currentEmployeeCount(employeeCount)
                        .employeeLimitReached(true)
                        .storageGb(0)
                        .storageUsedBytes(0L)
                        .storageQuotaReached(true)
                        .enabledModules(List.of())
                        .inGracePeriod(false)
                        .gracePeriodDaysRemaining(0)
                        .build());
    }

    private SubscriptionEntitlementDto buildEntitlements(TenantSubscription sub, long employeeCount) {
        SubscriptionPlan plan = sub.getPlan();

        boolean employeeLimitReached = plan.getMaxEmployees() != -1
                && employeeCount >= plan.getMaxEmployees();

        long storageUsedBytes = sub.getStorageUsedBytes();
        long storageQuotaBytes = plan.getStorageGb() == -1 ? Long.MAX_VALUE
                : (long) plan.getStorageGb() * BYTES_PER_GB;
        boolean storageQuotaReached = plan.getStorageGb() != -1
                && storageUsedBytes >= storageQuotaBytes;

        // Grace period: PAST_DUE subscriptions retain access for gracePeriodDays
        // after the current period end. Configured via platform.subscription.grace-period-days.
        boolean inGracePeriod = false;
        int graceDaysRemaining = 0;
        if (sub.getStatus() == TenantSubscription.SubscriptionStatus.PAST_DUE
                && sub.getCurrentPeriodEnd() != null) {
            LocalDateTime graceDeadline = sub.getCurrentPeriodEnd()
                    .plusDays(gracePeriodDays);
            if (LocalDateTime.now().isBefore(graceDeadline)) {
                inGracePeriod = true;
                graceDaysRemaining = (int) ChronoUnit.DAYS.between(
                        LocalDateTime.now(), graceDeadline);
            }
        }

        return SubscriptionEntitlementDto.builder()
                .hasActiveSubscription(true)
                .subscriptionStatus(sub.getStatus())
                .planName(plan.getName())
                .maxEmployees(plan.getMaxEmployees())
                .currentEmployeeCount(employeeCount)
                .employeeLimitReached(employeeLimitReached)
                .storageGb(plan.getStorageGb())
                .storageUsedBytes(storageUsedBytes)
                .storageQuotaReached(storageQuotaReached)
                .enabledModules(plan.getEnabledModuleList())
                .subscriptionStartedAt(sub.getStartedAt())
                .currentPeriodEnd(sub.getCurrentPeriodEnd())
                .trialEndsAt(sub.getTrialEndsAt())
                .cancelledAt(sub.getCancelledAt())
                .inGracePeriod(inGracePeriod)
                .gracePeriodDaysRemaining(graceDaysRemaining)
                .build();
    }

    // ── Entitlement enforcement ───────────────────────────────────────────────

    /**
     * Throws if the tenant's subscription does not permit business operations.
     *
     * <p>TRIAL and ACTIVE always pass. PAST_DUE passes within the grace period.
     * SUSPENDED, EXPIRED, CANCELLED, and no-subscription all throw.
     *
     * <p>Platform admins bypass this check — they are identified by the platform
     * pseudo-tenant ID and never have a tenant subscription.
     */
    @Transactional(readOnly = true)
    public void enforceSubscriptionActive(UUID tenantId) {
        if (TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) return;
        SubscriptionEntitlementDto ent = getEntitlements(tenantId);
        if (!ent.isOperationallyActive()) {
            String status = ent.getSubscriptionStatus() != null
                    ? ent.getSubscriptionStatus().name() : "NONE";
            throw new TenantException(
                    "Subscription is not active (status: " + status + "). " +
                    "Please contact your administrator to resolve billing.");
        }
    }

    /**
     * Throws if the tenant has reached their employee limit.
     * Uses a pessimistic write lock to prevent concurrent over-allocation.
     * Must be called inside an existing transaction.
     */
    @Transactional
    public void enforceEmployeeLimit(UUID tenantId) {
        if (TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) return;

        // Pessimistic lock: serialize concurrent employee creation for this tenant
        TenantSubscription sub = subscriptionRepository
                .findActiveByTenantIdForUpdate(tenantId)
                .orElseThrow(() -> new TenantException(
                        "Tenant has no active subscription. Employee creation is not allowed."));

        enforceSubscriptionActiveStatus(sub);

        SubscriptionPlan plan = sub.getPlan();
        if (plan.getMaxEmployees() == -1) return; // unlimited

        long employeeCount = membershipRepository
                .countByTenantIdAndMembershipStatus(tenantId, MembershipStatus.ACTIVE);

        if (employeeCount >= plan.getMaxEmployees()) {
            throw new TenantException(
                    "Employee limit reached for plan '" + plan.getName() + "'. " +
                    "Current: " + employeeCount +
                    ", Limit: " + plan.getMaxEmployees() + ". Please upgrade your plan.");
        }
    }

    /**
     * Throws if the given module is not enabled for the tenant's plan.
     * Platform admins (platform pseudo-tenant) bypass this check.
     */
    @Transactional(readOnly = true)
    public void enforceModuleAccess(UUID tenantId, String moduleKey) {
        if (TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) return;

        SubscriptionEntitlementDto ent = getEntitlements(tenantId);
        if (!ent.isHasActiveSubscription()) {
            throw new TenantException("Tenant has no active subscription.");
        }
        enforceSubscriptionActive(tenantId);
        if (!ent.isModuleEnabled(moduleKey)) {
            throw new TenantSecurityException(
                    "Module '" + moduleKey + "' is not enabled for plan '" + ent.getPlanName() + "'.");
        }
    }

    /**
     * Checks storage quota before accepting an upload.
     * Uses a pessimistic write lock to prevent concurrent over-allocation.
     *
     * @param tenantId   the tenant performing the upload
     * @param fileSizeBytes the actual file size measured server-side (never from browser)
     * @throws TenantException if the upload would exceed the storage quota
     */
    @Transactional
    public void enforceStorageQuota(UUID tenantId, long fileSizeBytes) {
        if (TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) return;

        TenantSubscription sub = subscriptionRepository
                .findActiveByTenantIdForUpdate(tenantId)
                .orElseThrow(() -> new TenantException(
                        "Tenant has no active subscription. File upload is not allowed."));

        enforceSubscriptionActiveStatus(sub);

        SubscriptionPlan plan = sub.getPlan();
        if (plan.getStorageGb() == -1) return; // unlimited

        long quotaBytes = (long) plan.getStorageGb() * BYTES_PER_GB;
        long usedBytes = sub.getStorageUsedBytes();

        if (usedBytes + fileSizeBytes > quotaBytes) {
            long remainingMb = Math.max(0, (quotaBytes - usedBytes) / (1024 * 1024));
            throw new TenantException(
                    "Storage quota exceeded for plan '" + plan.getName() + "'. " +
                    "Quota: " + plan.getStorageGb() + " GB, " +
                    "Remaining: " + remainingMb + " MB. Please upgrade your plan.");
        }
    }

    /**
     * Records storage usage after a successful upload.
     * Increments storage_used_bytes atomically.
     *
     * @param tenantId      the tenant that uploaded the file
     * @param fileSizeBytes the actual file size (measured server-side)
     */
    @Transactional
    public void recordStorageUsed(UUID tenantId, long fileSizeBytes) {
        if (TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) return;
        if (fileSizeBytes <= 0) return;
        int updated = subscriptionRepository.incrementStorageUsed(tenantId, fileSizeBytes);
        if (updated == 0) {
            log.warn("recordStorageUsed: no active subscription found for tenant={}", tenantId);
        }
    }

    /**
     * Releases storage quota after a file is deleted.
     * Decrements storage_used_bytes atomically, flooring at 0.
     *
     * @param tenantId      the tenant that owns the file
     * @param fileSizeBytes the size of the deleted file
     */
    @Transactional
    public void releaseStorage(UUID tenantId, long fileSizeBytes) {
        if (TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) return;
        if (fileSizeBytes <= 0) return;
        subscriptionRepository.decrementStorageUsed(tenantId, fileSizeBytes);
    }

    // ── Subscription history ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SubscriptionHistoryDto> getHistory(UUID tenantId) {
        requireTenant(tenantId);
        return historyRepository.findByTenantIdOrderByOccurredAtDesc(tenantId)
                .stream().map(SubscriptionHistoryDto::from).toList();
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Enforces that a subscription entity is in an operationally active state.
     * Reuses the same grace-period logic as getEntitlements().
     */
    private void enforceSubscriptionActiveStatus(TenantSubscription sub) {
        TenantSubscription.SubscriptionStatus status = sub.getStatus();
        switch (status) {
            case TRIAL, ACTIVE -> { /* allowed */ }
            case PAST_DUE -> {
                if (sub.getCurrentPeriodEnd() != null) {
                    LocalDateTime graceDeadline = sub.getCurrentPeriodEnd()
                            .plusDays(gracePeriodDays);
                    if (LocalDateTime.now().isBefore(graceDeadline)) return; // within grace
                }
                throw new TenantException(
                        "Subscription payment is overdue and the grace period has expired. " +
                        "Please contact your administrator to resolve billing.");
            }
            case SUSPENDED ->
                throw new TenantException(
                        "Subscription is suspended. Please contact your administrator.");
            case EXPIRED ->
                throw new TenantException(
                        "Subscription has expired. Please contact your administrator to renew.");
            case CANCELLED ->
                throw new TenantException(
                        "Subscription has been cancelled. Please contact your administrator.");
        }
    }

    private TenantSubscriptionDto transition(UUID subscriptionId,
                                              TenantSubscription.SubscriptionStatus next,
                                              String reason) {
        TenantSubscription sub = requireSubscription(subscriptionId);
        UUID planId = sub.getPlan().getId();
        transition(sub, next, reason, planId, planId);
        return TenantSubscriptionDto.from(sub);
    }

    private void transition(TenantSubscription sub,
                             TenantSubscription.SubscriptionStatus next,
                             String reason,
                             UUID fromPlanId,
                             UUID toPlanId) {
        if (!sub.canTransitionTo(next)) {
            throw new TenantException(
                    "Invalid subscription transition: " + sub.getStatus() + " → " + next);
        }
        TenantSubscription.SubscriptionStatus prev = sub.getStatus();
        sub.setStatus(next);
        subscriptionRepository.save(sub);
        appendHistory(sub, next, fromPlanId, toPlanId, reason);
        log.info("Subscription transitioned: id={} {} → {}", sub.getId(), prev, next);
    }

    private void appendHistory(TenantSubscription sub,
                                TenantSubscription.SubscriptionStatus toStatus,
                                UUID fromPlanId,
                                UUID toPlanId,
                                String reason) {
        SubscriptionHistory h = SubscriptionHistory.builder()
                .subscriptionId(sub.getId())
                .tenantId(sub.getTenant().getId())
                .planId(sub.getPlan().getId())
                .fromStatus(sub.getStatus() == toStatus ? null : sub.getStatus())
                .toStatus(toStatus)
                .fromPlanId(fromPlanId)
                .toPlanId(toPlanId)
                .actorId(currentActorId())
                .reason(reason)
                .build();
        historyRepository.save(h);
    }

    private Tenant requireTenant(UUID tenantId) {
        if (TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) {
            throw new TenantException("Platform pseudo-tenant cannot have a subscription");
        }
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantException("Tenant not found: " + tenantId));
    }

    private SubscriptionPlan requirePlan(UUID planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new TenantException("Subscription plan not found: " + planId));
    }

    private SubscriptionPlan requireActivePlan(UUID planId) {
        SubscriptionPlan plan = requirePlan(planId);
        if (plan.getPlanStatus() != SubscriptionPlan.PlanStatus.ACTIVE) {
            throw new TenantException("Subscription plan is not active: " + planId);
        }
        return plan;
    }

    private TenantSubscription requireSubscription(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new TenantException("Subscription not found: " + subscriptionId));
    }

    private String currentActorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return "system";
    }
}
