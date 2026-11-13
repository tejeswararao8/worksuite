package com.platform.hr.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.api.GlobalExceptionHandler;
import com.platform.hr.platform.employees.*;
import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.membership.MembershipStatus;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Security filter-chain INTEGRATION tests for the Platform Employee API.
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
 * <p>Contrast with {@code PlatformEmployeeServiceTest} (unit tests) which test
 * service logic in isolation with Mockito, no Spring context.
 */
@WebMvcTest
@ContextConfiguration(classes = {
        PlatformEmployeeController.class,
        PlatformEmployeeSecurityIT.TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
@DisplayName("PlatformEmployeeController — Security Integration Tests")
class PlatformEmployeeSecurityIT {

    private static final String BASE = "/api/v1/platform/employees";

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
                            "PLATFORM_SUPPORT", "PLATFORM_IMPLEMENTATION")
                    // Accept endpoint is authenticated-only (invitee may not yet be a platform member)
                    .requestMatchers("/api/v1/platform/employees/invitations/accept")
                        .authenticated()
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
            return http.build();
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean PlatformEmployeeService employeeService;

    private static final UUID MEMBERSHIP_ID = UUID.randomUUID();
    private static final UUID INVITATION_ID = UUID.randomUUID();

    private PlatformEmployeeDto activeDto() {
        return PlatformEmployeeDto.builder()
                .membershipId(MEMBERSHIP_ID)
                .keycloakUserId(UUID.randomUUID().toString())
                .roleName(PlatformRoles.PLATFORM_ADMIN)
                .membershipStatus(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private InvitationDto pendingInvitationDto() {
        return InvitationDto.builder()
                .id(INVITATION_ID)
                .email("new@platform.com")
                .firstName("New").lastName("User")
                .roleName(PlatformRoles.PLATFORM_ADMIN)
                .status(PlatformInvitation.InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── 1. Unauthenticated returns 401 ────────────────────────────────────────

    @Test
    @DisplayName("Unauthenticated GET /platform/employees returns 401")
    void unauthenticated_list_returns401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated POST /platform/employees/invite returns 401")
    void unauthenticated_invite_returns401() throws Exception {
        mockMvc.perform(post(BASE + "/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@x.com\",\"firstName\":\"X\",\"lastName\":\"X\",\"roleName\":\"PLATFORM_ADMIN\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── 2. Tenant employee cannot access platform employee APIs ───────────────

    @Test
    @DisplayName("HR_ADMIN cannot list platform employees — returns 403")
    void hrAdmin_cannotListEmployees() throws Exception {
        mockMvc.perform(get(BASE)
                        .with(jwt().authorities(new SimpleGrantedAuthority("HR_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE cannot access platform employee detail — returns 403")
    void employee_cannotGetById() throws Exception {
        mockMvc.perform(get(BASE + "/{id}", MEMBERSHIP_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE"))))
                .andExpect(status().isForbidden());
    }

    // ── 3. PLATFORM_SUPPORT cannot invite or change roles ─────────────────────

    @Test
    @DisplayName("PLATFORM_SUPPORT cannot invite — returns 403")
    void platformSupport_cannotInvite() throws Exception {
        mockMvc.perform(post(BASE + "/invite")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPPORT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@x.com\",\"firstName\":\"X\",\"lastName\":\"X\",\"roleName\":\"PLATFORM_SUPPORT\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PLATFORM_SUPPORT cannot change role — returns 403")
    void platformSupport_cannotChangeRole() throws Exception {
        mockMvc.perform(put(BASE + "/{id}/role", MEMBERSHIP_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPPORT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"PLATFORM_ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    // ── 4. PLATFORM_IMPLEMENTATION cannot invite or change roles ──────────────

    @Test
    @DisplayName("PLATFORM_IMPLEMENTATION cannot invite — returns 403")
    void platformImplementation_cannotInvite() throws Exception {
        mockMvc.perform(post(BASE + "/invite")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_IMPLEMENTATION)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@x.com\",\"firstName\":\"X\",\"lastName\":\"X\",\"roleName\":\"PLATFORM_SUPPORT\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PLATFORM_IMPLEMENTATION cannot change role — returns 403")
    void platformImplementation_cannotChangeRole() throws Exception {
        mockMvc.perform(put(BASE + "/{id}/role", MEMBERSHIP_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_IMPLEMENTATION)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"PLATFORM_ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    // ── 5. PLATFORM_SUPPORT and PLATFORM_IMPLEMENTATION can list ──────────────

    @Test
    @DisplayName("PLATFORM_SUPPORT can list platform employees — returns 200")
    void platformSupport_canListEmployees() throws Exception {
        when(employeeService.listEmployees(anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(activeDto())));

        mockMvc.perform(get(BASE)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPPORT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PLATFORM_IMPLEMENTATION can list platform employees — returns 200")
    void platformImplementation_canListEmployees() throws Exception {
        when(employeeService.listEmployees(anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_IMPLEMENTATION))))
                .andExpect(status().isOk());
    }

    // ── 6. PLATFORM_ADMIN can invite ──────────────────────────────────────────

    @Test
    @DisplayName("PLATFORM_ADMIN can invite a new platform employee — returns 201")
    void platformAdmin_canInvite() throws Exception {
        when(employeeService.invite(any())).thenReturn(pendingInvitationDto());

        mockMvc.perform(post(BASE + "/invite")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@platform.com\",\"firstName\":\"New\",\"lastName\":\"User\",\"roleName\":\"PLATFORM_ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── 7. PLATFORM_ADMIN cannot grant PLATFORM_SUPER_ADMIN ──────────────────

    @Test
    @DisplayName("PLATFORM_ADMIN cannot grant PLATFORM_SUPER_ADMIN — service throws 400")
    void platformAdmin_cannotGrantSuperAdmin() throws Exception {
        when(employeeService.invite(any()))
                .thenThrow(new TenantSecurityException(
                        "Only PLATFORM_SUPER_ADMIN can grant PLATFORM_SUPER_ADMIN role"));

        mockMvc.perform(post(BASE + "/invite")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@x.com\",\"firstName\":\"X\",\"lastName\":\"X\",\"roleName\":\"PLATFORM_SUPER_ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    // ── 8. PLATFORM_SUPER_ADMIN can perform all operations ───────────────────

    @Test
    @DisplayName("PLATFORM_SUPER_ADMIN can suspend an employee — returns 200")
    void platformSuperAdmin_canSuspend() throws Exception {
        PlatformEmployeeDto suspended = PlatformEmployeeDto.builder()
                .membershipId(MEMBERSHIP_ID)
                .keycloakUserId(UUID.randomUUID().toString())
                .roleName(PlatformRoles.PLATFORM_ADMIN)
                .membershipStatus(MembershipStatus.SUSPENDED)
                .createdAt(LocalDateTime.now())
                .build();

        when(employeeService.suspendEmployee(MEMBERSHIP_ID)).thenReturn(suspended);

        mockMvc.perform(post(BASE + "/{id}/suspend", MEMBERSHIP_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPER_ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.membershipStatus").value("SUSPENDED"));
    }

    @Test
    @DisplayName("PLATFORM_SUPER_ADMIN can revoke an employee — returns 200")
    void platformSuperAdmin_canRevoke() throws Exception {
        PlatformEmployeeDto revoked = PlatformEmployeeDto.builder()
                .membershipId(MEMBERSHIP_ID)
                .keycloakUserId(UUID.randomUUID().toString())
                .roleName(PlatformRoles.PLATFORM_ADMIN)
                .membershipStatus(MembershipStatus.REVOKED)
                .createdAt(LocalDateTime.now())
                .build();

        when(employeeService.revokeEmployee(MEMBERSHIP_ID)).thenReturn(revoked);

        mockMvc.perform(post(BASE + "/{id}/revoke", MEMBERSHIP_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPER_ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.membershipStatus").value("REVOKED"));
    }

    // ── 9. PLATFORM_ADMIN cannot revoke (SUPER_ADMIN only) ───────────────────

    @Test
    @DisplayName("PLATFORM_ADMIN cannot revoke employee — returns 403")
    void platformAdmin_cannotRevoke() throws Exception {
        mockMvc.perform(post(BASE + "/{id}/revoke", MEMBERSHIP_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN))))
                .andExpect(status().isForbidden());
    }

    // ── 10. Invitation acceptance cannot be repeated ──────────────────────────

    @Test
    @DisplayName("Accepting an already-accepted invitation returns 400")
    void accept_alreadyAccepted_returns400() throws Exception {
        when(employeeService.acceptInvitation(anyString()))
                .thenThrow(new TenantException("This invitation has already been accepted"));

        mockMvc.perform(post(BASE + "/invitations/accept")
                        .param("token", "sometoken")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN))))
                .andExpect(status().isBadRequest());
    }

    // ── 11. Expired invitation cannot be accepted ─────────────────────────────

    @Test
    @DisplayName("Accepting an expired invitation returns 400")
    void accept_expired_returns400() throws Exception {
        when(employeeService.acceptInvitation(anyString()))
                .thenThrow(new TenantException("This invitation has expired"));

        mockMvc.perform(post(BASE + "/invitations/accept")
                        .param("token", "expiredtoken")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN))))
                .andExpect(status().isBadRequest());
    }

    // ── 12. Revoked invitation cannot be accepted ─────────────────────────────

    @Test
    @DisplayName("Accepting a revoked invitation returns 400")
    void accept_revoked_returns400() throws Exception {
        when(employeeService.acceptInvitation(anyString()))
                .thenThrow(new TenantException("This invitation has been revoked"));

        mockMvc.perform(post(BASE + "/invitations/accept")
                        .param("token", "revokedtoken")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN))))
                .andExpect(status().isBadRequest());
    }

    // ── 13. Email mismatch returns 403 ────────────────────────────────────────

    @Test
    @DisplayName("Accepting with wrong email returns 403")
    void accept_emailMismatch_returns403() throws Exception {
        when(employeeService.acceptInvitation(anyString()))
                .thenThrow(new TenantSecurityException(
                        "Authenticated user email does not match the invitation email"));

        mockMvc.perform(post(BASE + "/invitations/accept")
                        .param("token", "mismatchtoken")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN))))
                .andExpect(status().isForbidden());
    }

    // ── 14. Last active super-admin cannot be suspended ───────────────────────

    @Test
    @DisplayName("Suspending the last PLATFORM_SUPER_ADMIN returns 400")
    void suspend_lastSuperAdmin_returns400() throws Exception {
        when(employeeService.suspendEmployee(MEMBERSHIP_ID))
                .thenThrow(new TenantException(
                        "Cannot remove or demote the last active PLATFORM_SUPER_ADMIN."));

        mockMvc.perform(post(BASE + "/{id}/suspend", MEMBERSHIP_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPER_ADMIN))))
                .andExpect(status().isBadRequest());
    }

    // ── 15. Last active super-admin cannot be revoked ─────────────────────────

    @Test
    @DisplayName("Revoking the last PLATFORM_SUPER_ADMIN returns 400")
    void revoke_lastSuperAdmin_returns400() throws Exception {
        when(employeeService.revokeEmployee(MEMBERSHIP_ID))
                .thenThrow(new TenantException(
                        "Cannot remove or demote the last active PLATFORM_SUPER_ADMIN."));

        mockMvc.perform(post(BASE + "/{id}/revoke", MEMBERSHIP_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPER_ADMIN))))
                .andExpect(status().isBadRequest());
    }

    // ── 16. Last active super-admin cannot be demoted ─────────────────────────

    @Test
    @DisplayName("Demoting the last PLATFORM_SUPER_ADMIN returns 400")
    void changeRole_lastSuperAdmin_returns400() throws Exception {
        when(employeeService.changeRole(eq(MEMBERSHIP_ID), anyString()))
                .thenThrow(new TenantException(
                        "Cannot remove or demote the last active PLATFORM_SUPER_ADMIN."));

        mockMvc.perform(put(BASE + "/{id}/role", MEMBERSHIP_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPER_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"PLATFORM_ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── 17. Valid acceptance returns 200 ──────────────────────────────────────

    @Test
    @DisplayName("Valid invitation acceptance returns 200 with ACTIVE membership")
    void accept_valid_returns200() throws Exception {
        when(employeeService.acceptInvitation("validtoken")).thenReturn(activeDto());

        mockMvc.perform(post(BASE + "/invitations/accept")
                        .param("token", "validtoken")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.membershipStatus").value("ACTIVE"));
    }
}
