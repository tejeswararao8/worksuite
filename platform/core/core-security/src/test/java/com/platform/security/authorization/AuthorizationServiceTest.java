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

@DisplayName("AuthorizationService")
class AuthorizationServiceTest {

    private final AuthorizationService service = new AuthorizationService();

    private static final UUID   COMPANY_A = UUID.randomUUID();
    private static final UUID   COMPANY_B = UUID.randomUUID();
    private static final String USER_UUID = UUID.randomUUID().toString();

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    private void authenticate(String tenantId, String... roles) {
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
        TenantContext.setTenantId(tenantId);
    }

    // ── Scope resolution ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Platform roles resolve to PLATFORM scope")
    void platformRole_resolvesPlatformScope() {
        authenticate(TenantSecurityService.PLATFORM_TENANT_ID.toString(),
                PlatformRoles.PLATFORM_ADMIN);
        assertThat(service.effectiveScope()).isEqualTo(AuthorizationScope.PLATFORM);
    }

    @Test
    @DisplayName("HR_ADMIN resolves to ORGANIZATION scope")
    void hrAdmin_resolvesOrganizationScope() {
        authenticate(COMPANY_A.toString(), PlatformRoles.HR_ADMIN);
        assertThat(service.effectiveScope()).isEqualTo(AuthorizationScope.ORGANIZATION);
    }

    @Test
    @DisplayName("HR_MANAGER resolves to DEPARTMENT scope")
    void hrManager_resolvesDepartmentScope() {
        authenticate(COMPANY_A.toString(), PlatformRoles.HR_MANAGER);
        assertThat(service.effectiveScope()).isEqualTo(AuthorizationScope.DEPARTMENT);
    }

    @Test
    @DisplayName("EMPLOYEE resolves to SELF scope")
    void employee_resolvesSelfScope() {
        authenticate(COMPANY_A.toString(), PlatformRoles.EMPLOYEE);
        assertThat(service.effectiveScope()).isEqualTo(AuthorizationScope.SELF);
    }

    // ── requireScope enforcement ──────────────────────────────────────────────

    @Test
    @DisplayName("EMPLOYEE cannot request ORGANIZATION scope")
    void employee_cannotRequestOrganizationScope() {
        authenticate(COMPANY_A.toString(), PlatformRoles.EMPLOYEE);
        assertThatThrownBy(() -> service.requireScope(AuthorizationScope.ORGANIZATION))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("Insufficient scope");
    }

    @Test
    @DisplayName("HR_ADMIN can request ORGANIZATION scope")
    void hrAdmin_canRequestOrganizationScope() {
        authenticate(COMPANY_A.toString(), PlatformRoles.HR_ADMIN);
        assertThatNoException()
                .isThrownBy(() -> service.requireScope(AuthorizationScope.ORGANIZATION));
    }

    // ── Cross-tenant resource access ──────────────────────────────────────────

    @Test
    @DisplayName("Tenant user cannot access another tenant's resources")
    void tenantUser_cannotAccessOtherTenant() {
        authenticate(COMPANY_A.toString(), PlatformRoles.HR_ADMIN);
        assertThatThrownBy(() -> service.requireSameCompany(COMPANY_B))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("Cross-tenant");
    }

    @Test
    @DisplayName("Tenant user can access own tenant's resources")
    void tenantUser_canAccessOwnTenant() {
        authenticate(COMPANY_A.toString(), PlatformRoles.HR_ADMIN);
        assertThatNoException().isThrownBy(() -> service.requireSameCompany(COMPANY_A));
    }

    @Test
    @DisplayName("Platform user bypasses cross-tenant check")
    void platformUser_bypassesCrossTenantCheck() {
        authenticate(TenantSecurityService.PLATFORM_TENANT_ID.toString(),
                PlatformRoles.PLATFORM_SUPER_ADMIN);
        assertThatNoException().isThrownBy(() -> service.requireSameCompany(COMPANY_B));
    }

    // ── Platform role separation ──────────────────────────────────────────────

    @Test
    @DisplayName("requirePlatformRole passes for platform user with platform role")
    void requirePlatformRole_passesForPlatformUser() {
        authenticate(TenantSecurityService.PLATFORM_TENANT_ID.toString(),
                PlatformRoles.PLATFORM_ADMIN);
        assertThatNoException().isThrownBy(() -> service.requirePlatformRole());
    }

    @Test
    @DisplayName("requirePlatformRole fails for tenant user")
    void requirePlatformRole_failsForTenantUser() {
        authenticate(COMPANY_A.toString(), PlatformRoles.HR_ADMIN);
        assertThatThrownBy(() -> service.requirePlatformRole())
                .isInstanceOf(TenantSecurityException.class);
    }

    // ── Tenant role permissions ───────────────────────────────────────────────

    @Test
    @DisplayName("TENANT_OWNER resolves to ORGANIZATION scope")
    void tenantOwner_resolvesOrganizationScope() {
        authenticate(COMPANY_A.toString(), PlatformRoles.TENANT_OWNER);
        assertThat(service.effectiveScope()).isEqualTo(AuthorizationScope.ORGANIZATION);
    }
}
