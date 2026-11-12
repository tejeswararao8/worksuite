package com.platform.security.jwt;

import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantInterceptor;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.TenantSecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates Keycloak JWT on every request.
 *
 * <p>Security improvements over the original:
 * <ul>
 *   <li>The {@code companyId} JWT claim is validated against the database via
 *       {@link TenantSecurityService} before being trusted. A forged or stale
 *       claim for a suspended/missing tenant is rejected with 403.</li>
 *   <li>TenantContext is always cleared in the {@code finally} block, even on
 *       exceptions, preventing thread-pool context leakage.</li>
 *   <li>Requests without a {@code companyId} claim (platform-level tokens) are
 *       allowed through — downstream {@code @PreAuthorize} guards enforce access.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final TenantSecurityService tenantSecurityService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            try {
                // 1. Decode and cryptographically validate the Keycloak JWT.
                Jwt jwt = jwtDecoder.decode(token);

                // 2. Build Spring Security authentication from JWT claims.
                Authentication auth = jwtAuthenticationConverter.convert(jwt);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 3. Extract the companyId claim.
                String companyId = jwt.getClaimAsString("companyId");
                String userId    = jwt.getSubject();
                String email     = jwt.getClaimAsString("email");

                // 4. Validate the tenant against the database.
                //    This rejects suspended, cancelled, or non-existent tenants.
                //    Requests without a companyId (e.g. platform admin tokens that
                //    pre-date the claim) are allowed through without a tenant context;
                //    they must rely on platform-role @PreAuthorize guards.
                if (companyId != null) {
                    tenantSecurityService.validateActiveTenant(companyId);
                    TenantContext.setTenantId(companyId);
                    request.setAttribute(TenantInterceptor.TENANT_ATTRIBUTE, companyId);
                }

                request.setAttribute("userId", userId);
                request.setAttribute("email", email);

                log.debug("Authenticated user={} tenant={}", userId, companyId);

            } catch (TenantSecurityException e) {
                // Tenant is suspended, cancelled, or not found.
                // Clear any partial context and return 403 immediately.
                log.warn("Tenant security violation: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                TenantContext.clear();
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Access denied: tenant account is not accessible");
                return;

            } catch (JwtException e) {
                // Invalid or expired JWT — let Spring Security's 401 handling take over.
                log.debug("Invalid JWT: {}", e.getMessage());
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Always clear TenantContext to prevent thread-pool leakage.
            TenantContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
