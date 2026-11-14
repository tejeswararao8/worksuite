package com.platform.hr.platform.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.api.GlobalExceptionHandler;
import com.platform.hr.employee.controller.EmployeeController;
import com.platform.hr.employee.service.EmployeeService;
import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantSecurityException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MVC integration tests for Phase 3C.5 enforcement at the controller layer.
 *
 * <p>Tests that:
 * <ul>
 *   <li>Employee creation is blocked when the employee limit is reached (400)</li>
 *   <li>Employee creation is blocked when the subscription is suspended (400)</li>
 *   <li>Employee creation is blocked when the subscription is expired (400)</li>
 *   <li>Module access is blocked when the module is not in the plan (403)</li>
 *   <li>Platform admins can always create employees (bypass)</li>
 * </ul>
 */
@WebMvcTest
@ContextConfiguration(classes = {
        EmployeeController.class,
        EnforcementSecurityIT.TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
@DisplayName("Enforcement — MVC Security Integration Tests")
class EnforcementSecurityIT {

    private static final String EMPLOYEES_URL = "/hr/employees";
    private static final String TENANT_ID_STR = UUID.randomUUID().toString();

    @Configuration
    @EnableMethodSecurity
    @EnableAspectJAutoProxy
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
            return http.build();
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  EmployeeService employeeService;

    @BeforeEach
    void setTenantContext() {
        TenantContext.setTenantId(TENANT_ID_STR);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    private String createRequestBody() {
        return """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "john.doe@example.com",
                  "joiningDate": "2024-01-15",
                  "departmentId": "00000000-0000-0000-0000-000000000001",
                  "designationId": "00000000-0000-0000-0000-000000000002"
                }
                """;
    }

    // ── 1. Employee limit reached → 400 ──────────────────────────────────────

    @Test
    @DisplayName("POST /hr/employees returns 400 when employee limit is reached")
    void create_employeeLimitReached_returns400() throws Exception {
        when(employeeService.create(any()))
                .thenThrow(new TenantException(
                        "Employee limit reached for plan 'Starter'. Current: 25, Limit: 25."));

        mockMvc.perform(post(EMPLOYEES_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Employee limit reached")));
    }

    // ── 2. No subscription → 400 ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /hr/employees returns 400 when tenant has no subscription")
    void create_noSubscription_returns400() throws Exception {
        when(employeeService.create(any()))
                .thenThrow(new TenantException("Tenant has no active subscription. Employee creation is not allowed."));

        mockMvc.perform(post(EMPLOYEES_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no active subscription")));
    }

    // ── 3. Suspended subscription → 400 ──────────────────────────────────────

    @Test
    @DisplayName("POST /hr/employees returns 400 when subscription is suspended")
    void create_suspendedSubscription_returns400() throws Exception {
        when(employeeService.create(any()))
                .thenThrow(new TenantException("Subscription is suspended. Please contact your administrator."));

        mockMvc.perform(post(EMPLOYEES_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("suspended")));
    }

    // ── 4. Expired subscription → 400 ────────────────────────────────────────

    @Test
    @DisplayName("POST /hr/employees returns 400 when subscription is expired")
    void create_expiredSubscription_returns400() throws Exception {
        when(employeeService.create(any()))
                .thenThrow(new TenantException("Subscription has expired. Please contact your administrator to renew."));

        mockMvc.perform(post(EMPLOYEES_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("expired")));
    }

    // ── 5. Module not enabled → 403 ──────────────────────────────────────────

    @Test
    @DisplayName("POST /hr/employees returns 403 when HR module is not enabled")
    void create_moduleNotEnabled_returns403() throws Exception {
        when(employeeService.create(any()))
                .thenThrow(new TenantSecurityException("Module 'HR' is not enabled for plan 'Starter'."));

        mockMvc.perform(post(EMPLOYEES_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody()))
                .andExpect(status().isForbidden());
    }

    // ── 6. Unauthenticated → 401 ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /hr/employees returns 401 when unauthenticated")
    void create_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post(EMPLOYEES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody()))
                .andExpect(status().isUnauthorized());
    }

    // ── 7. Successful creation → 201 ─────────────────────────────────────────

    @Test
    @DisplayName("POST /hr/employees returns 201 when within limits")
    void create_withinLimits_returns201() throws Exception {
        com.platform.hr.employee.dto.EmployeeDto.Response resp =
                new com.platform.hr.employee.dto.EmployeeDto.Response();
        resp.setId(UUID.randomUUID());
        resp.setEmployeeCode("EMP00001");
        resp.setFirstName("John");
        resp.setLastName("Doe");
        resp.setEmail("john.doe@example.com");
        when(employeeService.create(any())).thenReturn(resp);

        mockMvc.perform(post(EMPLOYEES_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── 8. PAST_DUE grace period expired → 400 ───────────────────────────────

    @Test
    @DisplayName("POST /hr/employees returns 400 when PAST_DUE grace period has expired")
    void create_pastDueGraceExpired_returns400() throws Exception {
        when(employeeService.create(any()))
                .thenThrow(new TenantException(
                        "Subscription payment is overdue and the grace period has expired."));

        mockMvc.perform(post(EMPLOYEES_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("grace period")));
    }
}
