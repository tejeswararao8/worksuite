package com.platform.hr.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.api.GlobalExceptionHandler;
import com.platform.hr.platform.tenants.TenantActionRequest;
import com.platform.hr.platform.tenants.TenantDto;
import com.platform.hr.platform.tenants.TenantManagementController;
import com.platform.hr.platform.tenants.TenantManagementService;
import com.platform.tenancy.Tenant;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantSecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Security filter-chain INTEGRATION tests for the Tenant Management API.
 *
 * <p>These are INTEGRATION tests (not unit tests):
 * <ul>
 *   <li>Load a minimal Spring Security filter chain via {@code @WebMvcTest}.</li>
 *   <li>Exercise the full HTTP request/response cycle including authentication,
 *       authorization, and controller dispatch.</li>
 *   <li>Service layer is mocked — no database involved.</li>
 *   <li>Uses {@code @ContextConfiguration} to explicitly load only the controller
 *       and a test-only security config, avoiding {@code @SpringBootApplication}
 *       component-scan interference.</li>
 * </ul>
 *
 * <p>Contrast with {@code TenantManagementServiceTest} (unit tests) which test
 * service logic in isolation with Mockito, no Spring context.
 */
@WebMvcTest
@ContextConfiguration(classes = {
        TenantManagementController.class,
        TenantManagementSecurityIT.TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
@DisplayName("TenantManagementController — Security Integration Tests")
class TenantManagementSecurityIT {

    private static final String BASE = "/api/v1/platform/tenants";

    /**
     * Minimal security config for tests.
     * Replicates the URL-guard and method-security rules from the real SecurityConfig
     * but omits JwtAuthFilter (which cannot be mocked — its doFilter is final).
     * The jwt() MockMvc post-processor injects the test principal directly.
     */
    @Configuration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/platform/**")
                        .hasAnyAuthority(
                            "PLATFORM_SUPER_ADMIN", "PLATFORM_ADMIN",
                            "PLATFORM_SUPPORT", "PLATFORM_IMPLEMENTATION", "SUPER_ADMIN")
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
            return http.build();
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean TenantManagementService tenantService;

    private static final UUID TENANT_ID = UUID.randomUUID();

    private TenantDto pendingDto() {
        return TenantDto.builder()
                .id(TENANT_ID).name("Acme Corp").slug("acme")
                .email("acme@example.com")
                .status(Tenant.TenantStatus.PENDING_REVIEW)
                .isActive(true).createdAt(LocalDateTime.now()).build();
    }

    // ── 1. Unauthenticated returns 401 ────────────────────────────────────────

    @Test
    @DisplayName("Unauthenticated GET /api/v1/platform/tenants returns 401")
    void unauthenticated_list_returns401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated POST /api/v1/platform/tenants/{id}/approve returns 401")
    void unauthenticated_approve_returns401() throws Exception {
        mockMvc.perform(post(BASE + "/{id}/approve", TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── 2. Tenant user cannot access platform tenant APIs ─────────────────────

    @Test
    @DisplayName("HR_ADMIN cannot list platform tenants — returns 403")
    void hrAdmin_cannotListTenants() throws Exception {
        mockMvc.perform(get(BASE)
                        .with(jwt().authorities(new SimpleGrantedAuthority("HR_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE cannot access platform tenant detail — returns 403")
    void employee_cannotGetTenantById() throws Exception {
        mockMvc.perform(get(BASE + "/{id}", TENANT_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE"))))
                .andExpect(status().isForbidden());
    }

    // ── 3. Authorized platform user can list tenants ──────────────────────────

    @Test
    @DisplayName("PLATFORM_SUPER_ADMIN can list tenants — returns 200")
    void platformSuperAdmin_canListTenants() throws Exception {
        when(tenantService.listTenants(anyInt(), anyInt(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(pendingDto())));

        mockMvc.perform(get(BASE)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_SUPER_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PLATFORM_SUPPORT can list tenants — returns 200")
    void platformSupport_canListTenants() throws Exception {
        when(tenantService.listTenants(anyInt(), anyInt(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_SUPPORT"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PLATFORM_IMPLEMENTATION can list tenants — returns 200")
    void platformImplementation_canListTenants() throws Exception {
        when(tenantService.listTenants(anyInt(), anyInt(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_IMPLEMENTATION"))))
                .andExpect(status().isOk());
    }

    // ── 4. Unauthorized platform role cannot approve ──────────────────────────

    @Test
    @DisplayName("PLATFORM_SUPPORT cannot approve tenant — returns 403")
    void platformSupport_cannotApproveTenant() throws Exception {
        mockMvc.perform(post(BASE + "/{id}/approve", TENANT_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_SUPPORT")))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PLATFORM_IMPLEMENTATION cannot reject tenant — returns 403")
    void platformImplementation_cannotRejectTenant() throws Exception {
        TenantActionRequest req = new TenantActionRequest();
        req.setReason("some reason");

        mockMvc.perform(post(BASE + "/{id}/reject", TENANT_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_IMPLEMENTATION")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PLATFORM_SUPPORT cannot suspend tenant — returns 403")
    void platformSupport_cannotSuspendTenant() throws Exception {
        TenantActionRequest req = new TenantActionRequest();
        req.setReason("policy violation");

        mockMvc.perform(post(BASE + "/{id}/suspend", TENANT_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_SUPPORT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ── 5. Authorized platform admin can approve ──────────────────────────────

    @Test
    @DisplayName("PLATFORM_ADMIN can approve a pending tenant — returns 200")
    void platformAdmin_canApproveTenant() throws Exception {
        TenantDto approved = TenantDto.builder()
                .id(TENANT_ID).name("Acme Corp").slug("acme")
                .email("acme@example.com")
                .status(Tenant.TenantStatus.ACTIVE)
                .isActive(true).createdAt(LocalDateTime.now()).build();

        when(tenantService.approve(eq(TENANT_ID), any())).thenReturn(approved);

        mockMvc.perform(post(BASE + "/{id}/approve", TENANT_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("PLATFORM_SUPER_ADMIN can reject a pending tenant — returns 200")
    void platformSuperAdmin_canRejectTenant() throws Exception {
        TenantDto rejected = TenantDto.builder()
                .id(TENANT_ID).name("Acme Corp").slug("acme")
                .email("acme@example.com")
                .status(Tenant.TenantStatus.REJECTED)
                .isActive(false).createdAt(LocalDateTime.now()).build();

        when(tenantService.reject(eq(TENANT_ID), eq("Incomplete docs"))).thenReturn(rejected);

        TenantActionRequest req = new TenantActionRequest();
        req.setReason("Incomplete docs");

        mockMvc.perform(post(BASE + "/{id}/reject", TENANT_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_SUPER_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    // ── 6. Invalid lifecycle transitions return 400 ───────────────────────────

    @Test
    @DisplayName("Invalid transition (ACTIVE -> REJECTED) returns 400")
    void invalidTransition_returns400() throws Exception {
        when(tenantService.reject(eq(TENANT_ID), any()))
                .thenThrow(new TenantException("Invalid status transition: ACTIVE -> REJECTED"));

        TenantActionRequest req = new TenantActionRequest();
        req.setReason("some reason");

        mockMvc.perform(post(BASE + "/{id}/reject", TENANT_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_SUPER_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── 7. Platform pseudo-tenant cannot be managed ───────────────────────────

    @Test
    @DisplayName("Platform pseudo-tenant management returns 400")
    void platformPseudoTenant_cannotBeManaged() throws Exception {
        UUID platformId = TenantSecurityService.PLATFORM_TENANT_ID;
        when(tenantService.getById(platformId))
                .thenThrow(new TenantException("Platform pseudo-tenant cannot be managed via this API"));

        mockMvc.perform(get(BASE + "/{id}", platformId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_SUPER_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    // ── 8. Reject without reason returns 400 ─────────────────────────────────

    @Test
    @DisplayName("Reject without reason returns 400 from service")
    void reject_withoutReason_returns400() throws Exception {
        when(tenantService.reject(eq(TENANT_ID), isNull()))
                .thenThrow(new TenantException("Rejection reason is required"));

        TenantActionRequest req = new TenantActionRequest();
        // reason is null

        mockMvc.perform(post(BASE + "/{id}/reject", TENANT_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_SUPER_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
