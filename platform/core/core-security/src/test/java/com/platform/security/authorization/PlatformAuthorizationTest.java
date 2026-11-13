package com.platform.security.authorization;

import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.TenantSecurityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Verifies that platform API authorization requires BOTH:
 *   1. A platform role in the JWT (PLATFORM_SUPER_ADMIN / PLATFORM_ADMIN / etc.)
 *   2. An ACTIVE UserTenantMembership in the platform pseudo-tenant.
 *
 * The membership check is enforced by MembershipValidationService (called from
 * JwtAuthFilter) which sets TenantContext only after validation passes.
 * AuthorizationService.requirePlatformRole() then checks TenantContext.
 *
 * These tests simulate the post-filter state: TenantContext is set only when
 * membership validation has passed. When membership is absent or non-ACTIVE,
 * JwtAuthFilter returns 403 before the controller is reached, so TenantContext
 * is never set — requirePlatformRole() sees no platform context and throws.
 */
@DisplayName("Platform Authorization — membership + role required")
class PlatformAuthorizationTest {

    private final AuthorizationService service = new AuthorizationService();

    private static final UUID   COMPANY_A = UUID.randomUUID();
    private static final String USER_UUID = UUID.randomUUID().toString();

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    /** Simulates state AFTER JwtAuthFilter has validated membership and set TenantContext. */
    private void authenticateWithMembership(String tenantId, String... roles) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(USER_UUID)
                .claim("email", "user@test.com")
                .claim("companyId", tenantId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt, authorities));
        // TenantContext IS set — membership was validated by JwtAuthFilter
        TenantContext.setTenantId(tenantId);
    }

    /**
     * Simulates state when JwtAuthFilter rejected the request due to missing/non-ACTIVE
     * membership. The filter returns 403 before the controller runs, so TenantContext
     * is never set. We model this by setting auth but NOT setting TenantContext.
     */
    private void authenticateWithoutMembership(String... roles) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(USER_UUID)
                .claim("email", "user@test.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt, authorities));
        // TenantContext NOT set — membership validation failed in JwtAuthFilter
    }

    // ── 1. ACTIVE platform membership + valid platform role = allowed ─────────

    @Test
    @DisplayName("ACTIVE platform membership + PLATFORM_SUPER_ADMIN role = allowed")
    void activeMembership_platformSuperAdmin_allowed() {
        authenticateWithMembership(
                TenantSecurityService.PLATFORM_TENANT_ID.toString(),
                PlatformRoles.PLATFORM_SUPER_ADMIN);

        assertThatNoException().isThrownBy(() -> service.requirePlatformRole());
        assertThat(service.effectiveScope()).isEqualTo(AuthorizationScope.PLATFORM);
    }

    @Test
    @DisplayName("ACTIVE platform membership + PLATFORM_ADMIN role = allowed")
    void activeMembership_platformAdmin_allowed() {
        authenticateWithMembership(
                TenantSecurityService.PLATFORM_TENANT_ID.toString(),
                PlatformRoles.PLATFORM_ADMIN);

        assertThatNoException().isThrownBy(() -> service.requirePlatformRole());
    }

    @Test
    @DisplayName("ACTIVE platform membership + PLATFORM_SUPPORT role = allowed")
    void activeMembership_platformSupport_allowed() {
        authenticateWithMembership(
                TenantSecurityService.PLATFORM_TENANT_ID.toString(),
                PlatformRoles.PLATFORM_SUPPORT);

        assertThatNoException().isThrownBy(() -> service.requirePlatformRole());
    }

    @Test
    @DisplayName("ACTIVE platform membership + PLATFORM_IMPLEMENTATION role = allowed")
    void activeMembership_platformImplementation_allowed() {
        authenticateWithMembership(
                TenantSecurityService.PLATFORM_TENANT_ID.toString(),
                PlatformRoles.PLATFORM_IMPLEMENTATION);

        assertThatNoException().isThrownBy(() -> service.requirePlatformRole());
    }

    // ── 2. Platform role WITHOUT membership = denied ──────────────────────────
    // JwtAuthFilter never sets TenantContext when membership is absent/non-ACTIVE.

    @Test
    @DisplayName("Platform role without ACTIVE membership = denied (no TenantContext)")
    void platformRole_withoutMembership_denied() {
        // JWT has platform role but JwtAuthFilter blocked before setting TenantContext
        authenticateWithoutMembership(PlatformRoles.PLATFORM_SUPER_ADMIN);

        assertThatThrownBy(() -> service.requirePlatformRole())
                .isInstanceOf(TenantSecurityException.class);
    }

    // ── 3. Tenant-only user = denied ──────────────────────────────────────────

    @Test
    @DisplayName("Tenant employee with ACTIVE tenant membership = denied for platform API")
    void tenantEmployee_deniedForPlatformApi() {
        authenticateWithMembership(COMPANY_A.toString(), PlatformRoles.HR_ADMIN);

        assertThatThrownBy(() -> service.requirePlatformRole())
                .isInstanceOf(TenantSecurityException.class);
        assertThat(service.effectiveScope()).isEqualTo(AuthorizationScope.ORGANIZATION);
    }

    @Test
    @DisplayName("Tenant employee cannot reach PLATFORM scope")
    void tenantEmployee_cannotReachPlatformScope() {
        authenticateWithMembership(COMPANY_A.toString(), PlatformRoles.EMPLOYEE);

        assertThatThrownBy(() -> service.requireScope(AuthorizationScope.PLATFORM))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("Insufficient scope");
    }

    // ── 4. Platform role in tenant context = denied ───────────────────────────
    // A user who somehow has a platform role JWT claim but is in a regular tenant
    // context (e.g. forged companyId) must be denied.

    @Test
    @DisplayName("Platform role claim in regular tenant context = denied")
    void platformRoleClaim_inTenantContext_denied() {
        // companyId points to a regular tenant, not the platform pseudo-tenant
        authenticateWithMembership(COMPANY_A.toString(), PlatformRoles.PLATFORM_SUPER_ADMIN);

        // effectiveScope returns PLATFORM (role check only), but requirePlatformRole
        // also checks isPlatformUser() which checks TenantContext
        assertThatThrownBy(() -> service.requirePlatformRole())
                .isInstanceOf(TenantSecurityException.class);
    }

    // ── 5. Unauthenticated = no context, all checks fail ─────────────────────

    @Test
    @DisplayName("Unauthenticated request has no platform scope")
    void unauthenticated_noPlatformScope() {
        // No authentication set at all
        assertThat(service.effectiveScope()).isEqualTo(AuthorizationScope.SELF);
        assertThatThrownBy(() -> service.requirePlatformRole())
                .isInstanceOf(TenantSecurityException.class);
    }
}
