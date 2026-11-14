package com.platform.hr.platform.subscription;

import com.platform.api.ApiResponse;
import com.platform.security.rbac.PlatformRoles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Platform subscription management REST API.
 *
 * <p>All endpoints require a platform role (enforced by SecurityConfig URL guard).
 * Plan and subscription mutations require PLATFORM_ADMIN or PLATFORM_SUPER_ADMIN.
 * Read operations are available to all platform roles.
 *
 * <p>Prices are NEVER accepted from the browser — only plan IDs are accepted
 * for subscription assignment. Prices are read from the database only.
 */
@RestController
@RequestMapping("/api/v1/platform/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    private static final String ADMIN_ROLES =
            "hasAnyAuthority('" + PlatformRoles.PLATFORM_SUPER_ADMIN + "', '" +
            PlatformRoles.PLATFORM_ADMIN + "')";

    private static final String ALL_PLATFORM_ROLES =
            "hasAnyAuthority('" + PlatformRoles.PLATFORM_SUPER_ADMIN + "', '" +
            PlatformRoles.PLATFORM_ADMIN + "', '" +
            PlatformRoles.PLATFORM_SUPPORT + "', '" +
            PlatformRoles.PLATFORM_IMPLEMENTATION + "')";

    // ── Plans ─────────────────────────────────────────────────────────────────

    @GetMapping("/plans")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<Page<SubscriptionPlanDto>>> listPlans(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    SubscriptionPlan.PlanStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.listPlans(page, size, status)));
    }

    @GetMapping("/plans/{planId}")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> getPlan(@PathVariable UUID planId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getPlan(planId)));
    }

    @PostMapping("/plans")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> createPlan(
            @Valid @RequestBody PlanRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plan created", subscriptionService.createPlan(req)));
    }

    @PutMapping("/plans/{planId}")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> updatePlan(
            @PathVariable UUID planId,
            @Valid @RequestBody PlanRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Plan updated",
                subscriptionService.updatePlan(planId, req)));
    }

    @PostMapping("/plans/{planId}/deactivate")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> deactivatePlan(@PathVariable UUID planId) {
        return ResponseEntity.ok(ApiResponse.success("Plan deactivated",
                subscriptionService.deactivatePlan(planId)));
    }

    @PostMapping("/plans/{planId}/activate")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> activatePlan(@PathVariable UUID planId) {
        return ResponseEntity.ok(ApiResponse.success("Plan activated",
                subscriptionService.activatePlan(planId)));
    }

    // ── Tenant subscriptions ──────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<Page<TenantSubscriptionDto>>> listAllSubscriptions(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.listAllSubscriptions(page, size)));
    }

    @GetMapping("/{subscriptionId}")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> getSubscription(
            @PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.getSubscription(subscriptionId)));
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> getActiveForTenant(
            @PathVariable UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.getActiveSubscriptionForTenant(tenantId)));
    }

    @GetMapping("/tenant/{tenantId}/history")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<Page<TenantSubscriptionDto>>> getTenantSubscriptionHistory(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.getSubscriptionHistoryForTenant(tenantId, page, size)));
    }

    @GetMapping("/tenant/{tenantId}/audit")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<List<SubscriptionHistoryDto>>> getAuditHistory(
            @PathVariable UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.getHistory(tenantId)));
    }

    @GetMapping("/tenant/{tenantId}/entitlements")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<SubscriptionEntitlementDto>> getEntitlements(
            @PathVariable UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.getEntitlements(tenantId)));
    }

    @PostMapping("/tenant/{tenantId}/assign")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> assignSubscription(
            @PathVariable UUID tenantId,
            @Valid @RequestBody AssignSubscriptionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription assigned",
                        subscriptionService.assignSubscription(tenantId, req)));
    }

    @PostMapping("/{subscriptionId}/activate")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> activate(
            @PathVariable UUID subscriptionId,
            @RequestBody(required = false) SubscriptionActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Subscription activated",
                subscriptionService.activate(subscriptionId, req)));
    }

    @PostMapping("/{subscriptionId}/cancel")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> cancel(
            @PathVariable UUID subscriptionId,
            @RequestBody(required = false) SubscriptionActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled",
                subscriptionService.cancel(subscriptionId, req)));
    }

    @PostMapping("/{subscriptionId}/suspend")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> suspend(
            @PathVariable UUID subscriptionId,
            @RequestBody(required = false) SubscriptionActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Subscription suspended",
                subscriptionService.suspend(subscriptionId, req)));
    }

    @PostMapping("/{subscriptionId}/reactivate")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> reactivate(
            @PathVariable UUID subscriptionId,
            @RequestBody(required = false) SubscriptionActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Subscription reactivated",
                subscriptionService.reactivate(subscriptionId, req)));
    }

    @PostMapping("/{subscriptionId}/renew")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> renew(
            @PathVariable UUID subscriptionId,
            @RequestBody(required = false) SubscriptionActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Subscription renewed",
                subscriptionService.renew(subscriptionId, req)));
    }

    @PostMapping("/{subscriptionId}/mark-past-due")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> markPastDue(
            @PathVariable UUID subscriptionId,
            @RequestBody(required = false) SubscriptionActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Subscription marked past due",
                subscriptionService.markPastDue(subscriptionId, req)));
    }

    @PostMapping("/{subscriptionId}/change-plan")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantSubscriptionDto>> changePlan(
            @PathVariable UUID subscriptionId,
            @RequestParam UUID planId,
            @RequestBody(required = false) SubscriptionActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Plan changed",
                subscriptionService.changePlan(subscriptionId, planId,
                        req != null ? req.getReason() : null)));
    }
}
