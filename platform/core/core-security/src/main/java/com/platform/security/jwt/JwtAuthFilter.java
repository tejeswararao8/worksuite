package com.platform.security.jwt;

import com.platform.tenancy.TenantInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Validates JWT on every request.
 * Sets SecurityContext and injects tenantId as request attribute for TenantInterceptor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token) && !tokenBlacklist.isBlacklisted(token)) {
            try {
                var claims = jwtTokenProvider.parseToken(token);
                String userId = claims.getSubject();
                String companyId = claims.get("companyId", String.class);
                String rolesStr = claims.get("roles", String.class);

                List<SimpleGrantedAuthority> authorities = rolesStr != null
                    ? Arrays.stream(rolesStr.split(","))
                        .map(String::trim)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
                    : List.of();

                var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Inject tenantId for TenantInterceptor
                request.setAttribute(TenantInterceptor.TENANT_ATTRIBUTE, companyId);
                request.setAttribute("userId", userId);

            } catch (Exception e) {
                log.debug("JWT processing failed: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
