package com.platform.security.membership;

import com.platform.security.rbac.PlatformRoles;
import com.platform.security.user.UserPrincipal;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.Tenant;
import com.platform.tenancy.membership.MembershipStatus;
import com.platform.tenancy.membership.MembershipValidationService;
import com.platform.tenancy.membership.UserTenantMembership;
import com.platform.tenancy.membership.UserTenantMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Membership management and tenant-selection endpoints.
 *
 * GET  /memberships/my-tenants  — returns tenants the current user can access.
 * POST /memberships             — platform/tenant admin creates a membership.
 * PUT  /memberships/{id}/status — platform/tenant admin updates membership status.
 *
 * Platform roles cannot be assigned via this endpoint.
 */
@RestController
@RequestMapping("/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipValidationService membershipValidationService;
    private final UserTenantMembershipRepository membershipRepository;
    private final TenantRepository tenantRepository;

    /**
     * Returns all tenants where the current user has an ACTIVE membership.
     * Used by the frontend tenant-selector — users can only see their own tenants.
     */
    @GetMapping("/my-tenants")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TenantSummary>> myTenants() {
        UUID userId = UserPrincipal.currentUserId();
        if (userId == null) return ResponseEntity.ok(List.of());

        List<TenantSummary> tenants = membershipValidationService
                .getActiveMemberships(userId.toString())
                .stream()
                .map(m -> new TenantSummary(
                        m.getTenant().getId(),
                        m.getTenant().getName(),
                        m.getTenant().getSlug(),
                        m.getRoleName()))
                .toList();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Creates a membership for a user in a tenant.
     * Platform roles cannot be assigned via this endpoint.
     * Tenant admins can only create memberships within their own tenant.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + PlatformRoles.PLATFORM_SUPER_ADMIN + "', '"
            + PlatformRoles.PLATFORM_ADMIN + "', '"
            + PlatformRoles.TENANT_OWNER + "', '"
            + PlatformRoles.TENANT_ADMIN + "')")
    public ResponseEntity<Map<String, Object>> createMembership(
            @RequestBody CreateMembershipRequest request) {

        if (isPlatformRole(request.roleName())) {
            throw new TenantSecurityException(
                    "Platform roles cannot be assigned via the membership API");
        }

        if (!UserPrincipal.isPlatformUser()) {
            UUID currentCompany = UserPrincipal.currentCompanyId();
            if (currentCompany == null || !currentCompany.equals(request.tenantId())) {
                throw new TenantSecurityException("Cannot create membership in another tenant");
            }
        }

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new TenantSecurityException("Tenant not found"));

        UserTenantMembership membership = UserTenantMembership.builder()
                .keycloakUserId(request.keycloakUserId())
                .tenant(tenant)
                .roleName(request.roleName())
                .membershipStatus(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        UserTenantMembership saved = membershipValidationService.createMembership(membership);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "status", saved.getMembershipStatus()));
    }

    /**
     * Updates membership status (ACTIVE / SUSPENDED / REVOKED).
     * Tenant admins can only update memberships within their own tenant.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('" + PlatformRoles.PLATFORM_SUPER_ADMIN + "', '"
            + PlatformRoles.PLATFORM_ADMIN + "', '"
            + PlatformRoles.TENANT_OWNER + "', '"
            + PlatformRoles.TENANT_ADMIN + "')")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        UserTenantMembership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new TenantSecurityException("Membership not found"));

        if (!UserPrincipal.isPlatformUser()) {
            UUID currentCompany = UserPrincipal.currentCompanyId();
            if (currentCompany == null
                    || !currentCompany.equals(membership.getTenant().getId())) {
                throw new TenantSecurityException("Cannot modify membership in another tenant");
            }
        }

        MembershipStatus newStatus = MembershipStatus.valueOf(body.get("status"));
        membership.setMembershipStatus(newStatus);
        membershipRepository.save(membership);
        return ResponseEntity.ok(Map.of("id", id, "status", newStatus));
    }

    private boolean isPlatformRole(String roleName) {
        return roleName != null && (
                roleName.equals(PlatformRoles.PLATFORM_SUPER_ADMIN) ||
                roleName.equals(PlatformRoles.PLATFORM_ADMIN) ||
                roleName.equals(PlatformRoles.PLATFORM_SUPPORT) ||
                roleName.equals(PlatformRoles.PLATFORM_IMPLEMENTATION));
    }

    public record TenantSummary(UUID id, String name, String slug, String roleName) {}

    public record CreateMembershipRequest(String keycloakUserId, UUID tenantId, String roleName) {}
}
