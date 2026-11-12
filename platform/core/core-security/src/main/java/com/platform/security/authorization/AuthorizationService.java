package com.platform.security.authorization;

import com.platform.security.rbac.PlatformRoles;
import com.platform.security.user.UserPrincipal;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Resolves and enforces authorization scopes for the current authenticated user.
 *
 * <p>Usage in service layer:
 * <pre>
 *   authorizationService.requireScope(AuthorizationScope.DEPARTMENT);
 *   authorizationService.requireSameCompany(targetCompanyId);
 * </pre>
 *
 * <p>Role → maximum permitted scope mapping:
 * <ul>
 *   <li>PLATFORM_* roles          → PLATFORM</li>
 *   <li>TENANT_OWNER, TENANT_ADMIN, HR_ADMIN → ORGANIZATION</li>
 *   <li>HR_MANAGER                → DEPARTMENT</li>
 *   <li>EMPLOYEE                  → SELF</li>
 * </ul>
 */
@Service
public class AuthorizationService {

    /**
     * Returns the maximum scope the current user is permitted to use.
     * Never returns a scope broader than the user's role allows.
     */
    public AuthorizationScope effectiveScope() {
        if (UserPrincipal.hasAnyRole(
                PlatformRoles.PLATFORM_SUPER_ADMIN,
                PlatformRoles.PLATFORM_ADMIN,
                PlatformRoles.PLATFORM_SUPPORT,
                PlatformRoles.PLATFORM_IMPLEMENTATION)) {
            return AuthorizationScope.PLATFORM;
        }
        if (UserPrincipal.hasAnyRole(
                PlatformRoles.TENANT_OWNER,
                PlatformRoles.TENANT_ADMIN,
                PlatformRoles.HR_ADMIN)) {
            return AuthorizationScope.ORGANIZATION;
        }
        if (UserPrincipal.hasRole(PlatformRoles.HR_MANAGER)) {
            return AuthorizationScope.DEPARTMENT;
        }
        return AuthorizationScope.SELF;
    }

    /**
     * Throws {@link TenantSecurityException} if the current user's effective scope
     * is narrower than the requested scope.
     */
    public void requireScope(AuthorizationScope required) {
        AuthorizationScope effective = effectiveScope();
        if (effective.ordinal() < required.ordinal()) {
            throw new TenantSecurityException(
                    "Insufficient scope: requires " + required + " but user has " + effective);
        }
    }

    /**
     * Throws {@link TenantSecurityException} if {@code targetCompanyId} does not match
     * the current tenant context. Platform users are exempt.
     *
     * <p>This prevents a user from accessing another tenant's data by manipulating
     * request parameters even if they somehow bypass the JWT companyId check.
     */
    public void requireSameCompany(UUID targetCompanyId) {
        if (UserPrincipal.isPlatformUser()) return;

        UUID currentCompanyId = UserPrincipal.currentCompanyId();
        if (currentCompanyId == null || !currentCompanyId.equals(targetCompanyId)) {
            throw new TenantSecurityException("Cross-tenant access denied");
        }
    }

    /**
     * Returns true if the current user may access their own record only,
     * or if their scope is broad enough to access any record.
     *
     * @param resourceOwnerId the Keycloak user ID of the resource owner
     */
    public boolean canAccess(UUID resourceOwnerId) {
        AuthorizationScope scope = effectiveScope();
        if (scope.ordinal() >= AuthorizationScope.ORGANIZATION.ordinal()) return true;
        if (scope == AuthorizationScope.SELF) {
            UUID currentUser = UserPrincipal.currentUserId();
            return currentUser != null && currentUser.equals(resourceOwnerId);
        }
        // TEAM and DEPARTMENT require caller to pass additional context — default allow
        // and let the service layer apply the narrower filter.
        return true;
    }

    /**
     * Convenience: throws if the current user is not a platform user.
     */
    public void requirePlatformRole() {
        if (!UserPrincipal.isPlatformUser()) {
            throw new TenantSecurityException("Platform role required");
        }
        if (!UserPrincipal.hasAnyRole(
                PlatformRoles.PLATFORM_SUPER_ADMIN,
                PlatformRoles.PLATFORM_ADMIN,
                PlatformRoles.PLATFORM_SUPPORT,
                PlatformRoles.PLATFORM_IMPLEMENTATION)) {
            throw new TenantSecurityException("No platform role assigned");
        }
    }
}
