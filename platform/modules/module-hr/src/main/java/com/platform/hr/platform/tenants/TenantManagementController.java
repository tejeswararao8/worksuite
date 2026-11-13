package com.platform.hr.platform.tenants;

import com.platform.api.ApiResponse;
import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.Tenant;
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
 * Platform tenant management REST API.
 *
 * <p>All endpoints require a platform role and ACTIVE platform membership
 * (enforced by {@code SecurityConfig} URL guard + {@code JwtAuthFilter}).
 *
 * <p>Destructive actions (approve, reject, suspend, reactivate) additionally
 * require {@code PLATFORM_SUPER_ADMIN} or {@code PLATFORM_ADMIN} via
 * method-level {@code @PreAuthorize}.
 */
@RestController
@RequestMapping("/api/v1/platform/tenants")
@RequiredArgsConstructor
public class TenantManagementController {

    private final TenantManagementService tenantService;

    private static final String ADMIN_ROLES =
            "hasAnyAuthority('" + PlatformRoles.PLATFORM_SUPER_ADMIN + "', '" + PlatformRoles.PLATFORM_ADMIN + "')";

    private static final String ALL_PLATFORM_ROLES =
            "hasAnyAuthority('" + PlatformRoles.PLATFORM_SUPER_ADMIN + "', '" +
            PlatformRoles.PLATFORM_ADMIN + "', '" +
            PlatformRoles.PLATFORM_SUPPORT + "', '" +
            PlatformRoles.PLATFORM_IMPLEMENTATION + "')";

    // ── List / Search ─────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<Page<TenantDto>>> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String search,
            @RequestParam(required = false)    Tenant.TenantStatus status) {
        return ResponseEntity.ok(ApiResponse.success(tenantService.listTenants(page, size, search, status)));
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<TenantDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(tenantService.getById(id)));
    }

    // ── Register (creates as PENDING_REVIEW) ──────────────────────────────────

    @PostMapping("/register")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantDto>> register(
            @Valid @RequestBody TenantRegistrationRequest req) {
        TenantDto created = tenantService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tenant registered and pending review", created));
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/approve")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantDto>> approve(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) TenantActionRequest req) {
        String reason = req != null ? req.getReason() : null;
        return ResponseEntity.ok(ApiResponse.success("Tenant approved", tenantService.approve(id, reason)));
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/reject")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantDto>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody TenantActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tenant rejected", tenantService.reject(id, req.getReason())));
    }

    // ── Suspend ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/suspend")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantDto>> suspend(
            @PathVariable UUID id,
            @Valid @RequestBody TenantActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tenant suspended", tenantService.suspend(id, req.getReason())));
    }

    // ── Reactivate ────────────────────────────────────────────────────────────

    @PostMapping("/{id}/reactivate")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<TenantDto>> reactivate(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) TenantActionRequest req) {
        String reason = req != null ? req.getReason() : null;
        return ResponseEntity.ok(ApiResponse.success("Tenant reactivated", tenantService.reactivate(id, reason)));
    }

    // ── Lifecycle history ─────────────────────────────────────────────────────

    @GetMapping("/{id}/history")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<List<TenantLifecycleEventDto>>> history(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(tenantService.getHistory(id)));
    }
}
