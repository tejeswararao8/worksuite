package com.platform.security.jwt;

import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantInterceptor;
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
 * Extracts userId + companyId from Keycloak token claims.
 * Sets SecurityContext and injects tenantId for TenantInterceptor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            try {
                // Decode and validate Keycloak JWT
                Jwt jwt = jwtDecoder.decode(token);

                // Extract claims from Keycloak token
                String userId    = jwt.getSubject();                          // Keycloak user UUID
                String companyId = jwt.getClaimAsString("companyId");         // Custom claim we add in Keycloak
                String email     = jwt.getClaimAsString("email");

                // Build Spring Security authentication from JWT
                Authentication auth = jwtAuthenticationConverter.convert(jwt);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Set tenant context directly + request attribute for interceptor
                if (companyId != null) {
                    TenantContext.setTenantId(companyId);
                    request.setAttribute(TenantInterceptor.TENANT_ATTRIBUTE, companyId);
                }
                request.setAttribute("userId", userId);
                request.setAttribute("email", email);

                log.debug("Authenticated user: {} tenant: {}", userId, companyId);

            } catch (JwtException e) {
                log.debug("Invalid Keycloak JWT: {}", e.getMessage());
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
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
