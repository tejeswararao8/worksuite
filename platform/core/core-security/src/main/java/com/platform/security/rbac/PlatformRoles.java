package com.platform.security.rbac;

/**
 * Canonical role name constants for use in {@code @PreAuthorize} expressions.
 *
 * <p><strong>Platform roles</strong> — assigned to WorkSuite staff who operate
 * the platform itself. These users belong to the platform pseudo-tenant
 * ({@code 00000000-0000-0000-0000-000000000001}) and must never be granted
 * to tenant users via self-registration or tenant-level role assignment.
 *
 * <p><strong>Tenant roles</strong> — assigned to users within a specific
 * customer organization. These roles are scoped to a single tenant and cannot
 * access cross-tenant or platform-admin APIs.
 *
 * <p>Role names must match exactly what is configured in Keycloak realm roles.
 */
public final class PlatformRoles {

    private PlatformRoles() {}

    // ── Platform roles ────────────────────────────────────────────────────────

    /** Full platform access — can manage tenants, users, config, and all data. */
    public static final String PLATFORM_SUPER_ADMIN      = "PLATFORM_SUPER_ADMIN";

    /** Platform admin — can manage tenants and users but not system config. */
    public static final String PLATFORM_ADMIN            = "PLATFORM_ADMIN";

    /** Read-only platform support — can view tenant data for troubleshooting. */
    public static final String PLATFORM_SUPPORT          = "PLATFORM_SUPPORT";

    /** Implementation team — can configure tenant workspaces during onboarding. */
    public static final String PLATFORM_IMPLEMENTATION   = "PLATFORM_IMPLEMENTATION";

    // ── Tenant roles ──────────────────────────────────────────────────────────

    /** Tenant owner — full access within their organization. */
    public static final String TENANT_OWNER              = "TENANT_OWNER";

    /** Tenant admin — manages users, roles, and settings within their org. */
    public static final String TENANT_ADMIN              = "TENANT_ADMIN";

    /** HR administrator — full HR module access within their org. */
    public static final String HR_ADMIN                  = "HR_ADMIN";

    /** HR manager — manages team HR operations, limited admin access. */
    public static final String HR_MANAGER                = "HR_MANAGER";

    /** Regular employee — self-service access only. */
    public static final String EMPLOYEE                  = "EMPLOYEE";

    // ── Legacy role (seeded in V4, kept for backward compatibility) ───────────

    /** @deprecated Use {@link #PLATFORM_SUPER_ADMIN} for new code. */
    @Deprecated
    public static final String SUPER_ADMIN               = "SUPER_ADMIN";
}
