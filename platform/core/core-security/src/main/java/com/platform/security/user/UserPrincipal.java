package com.platform.security.user;

import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantSecurityService;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Helper to access current authenticated user from Keycloak JWT.
 * Use UserPrincipal.currentUserId() anywhere in the application.
 * No UserDetails needed — Keycloak manages users.
 */
@Getter
public class UserPrincipal {

    private UserPrincipal() {}

    // ─────────────────────────────────────────────
    // Get current user ID from Keycloak JWT subject
    // Returns null if not authenticated or subject is not a valid UUID.
    // ─────────────────────────────────────────────
    public static UUID currentUserId() {
        Jwt jwt = extractJwt();
        if (jwt == null) return null;
        String subject = jwt.getSubject();
        if (subject == null) return null;
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ─────────────────────────────────────────────
    // Get current company ID from TenantContext
    // (set by JwtAuthFilter from companyId claim)
    // ─────────────────────────────────────────────
    public static UUID currentCompanyId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId != null ? UUID.fromString(tenantId) : null;
    }

    // ─────────────────────────────────────────────
    // Get current user email from Keycloak JWT
    // ─────────────────────────────────────────────
    public static String currentEmail() {
        Jwt jwt = extractJwt();
        return jwt != null ? jwt.getClaimAsString("email") : null;
    }

    // ─────────────────────────────────────────────
    // Get current user roles from Keycloak JWT
    // ─────────────────────────────────────────────
    public static Set<String> currentRoles() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Set.of();
        return auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }

    // ─────────────────────────────────────────────
    // Check if current user has a specific role.
    // Keycloak realm roles are stored as plain authority strings
    // (e.g. "HR_MANAGER"), NOT prefixed with "ROLE_".
    // The JwtAuthenticationConverter in SecurityConfig maps them directly.
    // ─────────────────────────────────────────────
    public static boolean hasRole(String role) {
        return currentRoles().contains(role);
    }

    // ─────────────────────────────────────────────
    // Check if current user has any of the given roles.
    // ─────────────────────────────────────────────
    public static boolean hasAnyRole(String... roles) {
        Set<String> current = currentRoles();
        for (String role : roles) {
            if (current.contains(role)) return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // Check if current user is a platform-level user
    // (companyId == PLATFORM_TENANT_ID).
    // ─────────────────────────────────────────────
    public static boolean isPlatformUser() {
        return TenantSecurityService.isPlatformTenant(
            TenantContext.getTenantId());
    }

    // ─────────────────────────────────────────────
    // Extract raw Keycloak JWT from SecurityContext
    // ─────────────────────────────────────────────
    private static Jwt extractJwt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }
}
