package com.platform.security;

import com.platform.security.rbac.PlatformRoles;
import com.platform.security.user.UserPrincipal;
import com.platform.tenancy.TenantContext;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserPrincipal")
class UserPrincipalTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    private static final String TEST_USER_UUID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    private void setAuthentication(String... roles) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(TEST_USER_UUID)
                .claim("email", "test@example.com")
                .claim("companyId", "tenant-uuid-456")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt, authorities));
    }

    // ── hasRole ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("hasRole matches plain authority string (no ROLE_ prefix)")
    void hasRole_matchesPlainAuthority() {
        setAuthentication(PlatformRoles.HR_ADMIN);
        assertThat(UserPrincipal.hasRole(PlatformRoles.HR_ADMIN)).isTrue();
    }

    @Test
    @DisplayName("hasRole does NOT match with ROLE_ prefix")
    void hasRole_doesNotMatchWithRolePrefix() {
        setAuthentication(PlatformRoles.HR_ADMIN);
        // Old buggy behavior was to prepend ROLE_ — this must NOT match
        assertThat(UserPrincipal.hasRole("ROLE_" + PlatformRoles.HR_ADMIN)).isFalse();
    }

    @Test
    @DisplayName("hasRole returns false for absent role")
    void hasRole_falseForAbsentRole() {
        setAuthentication(PlatformRoles.HR_MANAGER);
        assertThat(UserPrincipal.hasRole(PlatformRoles.PLATFORM_SUPER_ADMIN)).isFalse();
    }

    // ── hasAnyRole ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("hasAnyRole returns true when at least one role matches")
    void hasAnyRole_trueWhenOneMatches() {
        setAuthentication(PlatformRoles.HR_MANAGER);
        assertThat(UserPrincipal.hasAnyRole(
                PlatformRoles.PLATFORM_SUPER_ADMIN,
                PlatformRoles.HR_MANAGER)).isTrue();
    }

    @Test
    @DisplayName("hasAnyRole returns false when none match")
    void hasAnyRole_falseWhenNoneMatch() {
        setAuthentication(PlatformRoles.EMPLOYEE);
        assertThat(UserPrincipal.hasAnyRole(
                PlatformRoles.PLATFORM_SUPER_ADMIN,
                PlatformRoles.HR_ADMIN)).isFalse();
    }

    // ── isPlatformUser ────────────────────────────────────────────────────────

    @Test
    @DisplayName("isPlatformUser returns true when TenantContext holds platform tenant ID")
    void isPlatformUser_trueForPlatformTenant() {
        TenantContext.setTenantId(TenantSecurityService.PLATFORM_TENANT_ID.toString());
        assertThat(UserPrincipal.isPlatformUser()).isTrue();
    }

    @Test
    @DisplayName("isPlatformUser returns false for regular tenant")
    void isPlatformUser_falseForRegularTenant() {
        TenantContext.setTenantId("11111111-1111-1111-1111-111111111111");
        assertThat(UserPrincipal.isPlatformUser()).isFalse();
    }

    @Test
    @DisplayName("isPlatformUser returns false when TenantContext is empty")
    void isPlatformUser_falseWhenNoContext() {
        assertThat(UserPrincipal.isPlatformUser()).isFalse();
    }

    // ── currentUserId / currentEmail ──────────────────────────────────────────

    @Test
    @DisplayName("currentUserId returns JWT subject as UUID")
    void currentUserId_returnsSubject() {
        setAuthentication(PlatformRoles.HR_ADMIN);
        UUID userId = UserPrincipal.currentUserId();
        assertThat(userId).isNotNull();
        assertThat(userId.toString()).isEqualTo(TEST_USER_UUID);
    }

    @Test
    @DisplayName("currentEmail returns email claim")
    void currentEmail_returnsEmailClaim() {
        setAuthentication(PlatformRoles.HR_ADMIN);
        assertThat(UserPrincipal.currentEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("currentRoles returns empty set when not authenticated")
    void currentRoles_emptyWhenNotAuthenticated() {
        assertThat(UserPrincipal.currentRoles()).isEmpty();
    }
}
