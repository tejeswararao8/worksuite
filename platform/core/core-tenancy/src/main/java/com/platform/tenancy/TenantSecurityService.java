package com.platform.tenancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Server-side tenant security validation.
 *
 * <p>The JWT {@code companyId} claim is a hint, not a proof. This service
 * validates that:
 * <ol>
 *   <li>The tenant actually exists in the database.</li>
 *   <li>The tenant is ACTIVE (not SUSPENDED, CANCELLED, etc.).</li>
 *   <li>The tenant's {@code isActive} flag is true.</li>
 * </ol>
 *
 * <p>Platform users (companyId == PLATFORM_TENANT_ID) bypass tenant-status
 * checks because they operate across all tenants.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantSecurityService {

    /** The fixed UUID of the platform-admin pseudo-tenant seeded in V4. */
    public static final UUID PLATFORM_TENANT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final TenantRepository tenantRepository;

    /**
     * Validates that {@code tenantId} refers to an existing, active tenant.
     *
     * @param tenantId the UUID string from the JWT {@code companyId} claim
     * @throws TenantSecurityException if the tenant is missing, suspended, or inactive
     */
    @Transactional(readOnly = true)
    public void validateActiveTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new TenantSecurityException("No tenant identity in token");
        }

        UUID id;
        try {
            id = UUID.fromString(tenantId);
        } catch (IllegalArgumentException e) {
            throw new TenantSecurityException("Malformed tenant ID in token: " + tenantId);
        }

        // Platform users are always allowed — they are not bound to a single tenant.
        if (PLATFORM_TENANT_ID.equals(id)) {
            return;
        }

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantSecurityException(
                        "Tenant not found: " + tenantId));

        if (!Boolean.TRUE.equals(tenant.getIsActive())) {
            log.warn("Access attempt on inactive tenant: {}", tenantId);
            throw new TenantSecurityException("Tenant account is inactive: " + tenantId);
        }

        if (tenant.getStatus() == Tenant.TenantStatus.PENDING_REVIEW) {
            log.warn("Access attempt on pending-review tenant: {}", tenantId);
            throw new TenantSecurityException("Tenant account is pending review: " + tenantId);
        }

        if (tenant.getStatus() == Tenant.TenantStatus.REJECTED) {
            log.warn("Access attempt on rejected tenant: {}", tenantId);
            throw new TenantSecurityException("Tenant account has been rejected: " + tenantId);
        }

        if (tenant.getStatus() == Tenant.TenantStatus.SUSPENDED) {
            log.warn("Access attempt on suspended tenant: {}", tenantId);
            throw new TenantSecurityException("Tenant account is suspended: " + tenantId);
        }

        if (tenant.getStatus() == Tenant.TenantStatus.CANCELLED) {
            log.warn("Access attempt on cancelled tenant: {}", tenantId);
            throw new TenantSecurityException("Tenant account is cancelled: " + tenantId);
        }
    }

    /**
     * Returns true if the given tenantId belongs to the platform pseudo-tenant.
     */
    public static boolean isPlatformTenant(String tenantId) {
        if (tenantId == null) return false;
        try {
            return PLATFORM_TENANT_ID.equals(UUID.fromString(tenantId));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
