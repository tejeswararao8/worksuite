package com.platform.hr.platform.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.api.GlobalExceptionHandler;
import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.TenantException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@WebMvcTest
@ContextConfiguration(classes = {
        SubscriptionController.class,
        SubscriptionSecurityIT.TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
@DisplayName("SubscriptionController — Security Integration Tests")
class SubscriptionSecurityIT {

    private static final String BASE  = "/api/v1/platform/subscriptions";
    private static final UUID   PLAN_ID = UUID.randomUUID();
    private static final UUID   SUB_ID  = UUID.randomUUID();
    private static final UUID   TENANT_ID = UUID.randomUUID();

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
                        .hasAnyAuthority("PLATFORM_SUPER_ADMIN", "PLATFORM_ADMIN",
                                "PLATFORM_SUPPORT", "PLATFORM_IMPLEMENTATION")
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
            return http.build();
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  SubscriptionService subscriptionService;

    private SubscriptionPlanDto planDto() {
        return SubscriptionPlanDto.builder()
                .id(PLAN_ID).name("Growth").description("desc")
                .monthlyPriceCents(7900).annualPriceCents(79000)
                .trialDays(14).maxEmployees(100).storageGb(20)
                .enabledModules(List.of("HR", "PAYROLL"))
                .planStatus(SubscriptionPlan.PlanStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    private TenantSubscriptionDto subDto() {
        return TenantSubscriptionDto.builder()
                .id(SUB_ID).tenantId(TENANT_ID).tenantName("Acme")
                .plan(planDto())
                .status(TenantSubscription.SubscriptionStatus.ACTIVE)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .startedAt(LocalDateTime.now())
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .assignedBy("admin-id")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    // ── 1. Unauthenticated returns 401 ────────────────────────────────────────

    @Test
    @DisplayName("Unauthenticated GET /subscriptions/plans returns 401")
    void unauthenticated_listPlans_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/plans"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated POST /subscriptions/plans returns 401")
    void unauthenticated_createPlan_returns401() throws Exception {
        mockMvc.perform(post(BASE + "/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"enabledModules\":\"HR\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── 2. Tenant user cannot access platform subscription APIs ───────────────

    @Test
    @DisplayName("HR_ADMIN cannot list subscription plans — returns 403")
    void hrAdmin_cannotListPlans() throws Exception {
        mockMvc.perform(get(BASE + "/plans")
                        .with(jwt().authorities(new SimpleGrantedAuthority("HR_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE cannot assign subscription — returns 403")
    void employee_cannotAssignSubscription() throws Exception {
        mockMvc.perform(post(BASE + "/tenant/" + TENANT_ID + "/assign")
                        .with(jwt().authorities(new SimpleGrantedAuthority("EMPLOYEE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":\"" + PLAN_ID + "\"}"))
                .andExpect(status().isForbidden());
    }

    // ── 3. PLATFORM_SUPPORT can read but not mutate ───────────────────────────

    @Test
    @DisplayName("PLATFORM_SUPPORT can list plans — returns 200")
    void platformSupport_canListPlans() throws Exception {
        when(subscriptionService.listPlans(anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(planDto())));

        mockMvc.perform(get(BASE + "/plans")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPPORT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PLATFORM_SUPPORT cannot create plan — returns 403")
    void platformSupport_cannotCreatePlan() throws Exception {
        mockMvc.perform(post(BASE + "/plans")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPPORT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"enabledModules\":\"HR\",\"monthlyPriceCents\":0,\"annualPriceCents\":0,\"trialDays\":14,\"maxEmployees\":50,\"storageGb\":5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PLATFORM_SUPPORT cannot assign subscription — returns 403")
    void platformSupport_cannotAssignSubscription() throws Exception {
        mockMvc.perform(post(BASE + "/tenant/" + TENANT_ID + "/assign")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPPORT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":\"" + PLAN_ID + "\"}"))
                .andExpect(status().isForbidden());
    }

    // ── 4. PLATFORM_ADMIN can perform all operations ──────────────────────────

    @Test
    @DisplayName("PLATFORM_ADMIN can create plan — returns 201")
    void platformAdmin_canCreatePlan() throws Exception {
        when(subscriptionService.createPlan(any())).thenReturn(planDto());

        mockMvc.perform(post(BASE + "/plans")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Growth\",\"enabledModules\":\"HR\",\"monthlyPriceCents\":7900,\"annualPriceCents\":79000,\"trialDays\":14,\"maxEmployees\":100,\"storageGb\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PLATFORM_ADMIN can assign subscription — returns 201")
    void platformAdmin_canAssignSubscription() throws Exception {
        when(subscriptionService.assignSubscription(eq(TENANT_ID), any())).thenReturn(subDto());

        mockMvc.perform(post(BASE + "/tenant/" + TENANT_ID + "/assign")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":\"" + PLAN_ID + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("PLATFORM_ADMIN can cancel subscription — returns 200")
    void platformAdmin_canCancelSubscription() throws Exception {
        TenantSubscriptionDto cancelled = TenantSubscriptionDto.builder()
                .id(SUB_ID).tenantId(TENANT_ID).tenantName("Acme")
                .plan(planDto())
                .status(TenantSubscription.SubscriptionStatus.CANCELLED)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .startedAt(LocalDateTime.now())
                .currentPeriodStart(LocalDateTime.now())
                .assignedBy("admin").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(subscriptionService.cancel(eq(SUB_ID), any())).thenReturn(cancelled);

        mockMvc.perform(post(BASE + "/" + SUB_ID + "/cancel")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    // ── 5. Invalid transition returns 400 ─────────────────────────────────────

    @Test
    @DisplayName("Invalid subscription transition returns 400")
    void invalidTransition_returns400() throws Exception {
        when(subscriptionService.suspend(eq(SUB_ID), any()))
                .thenThrow(new TenantException("Invalid subscription transition: CANCELLED → SUSPENDED"));

        mockMvc.perform(post(BASE + "/" + SUB_ID + "/suspend")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── 6. Plan not found returns 400 ─────────────────────────────────────────

    @Test
    @DisplayName("Assigning unknown plan returns 400")
    void unknownPlan_returns400() throws Exception {
        when(subscriptionService.assignSubscription(eq(TENANT_ID), any()))
                .thenThrow(new TenantException("Subscription plan not found"));

        mockMvc.perform(post(BASE + "/tenant/" + TENANT_ID + "/assign")
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── 7. PLATFORM_IMPLEMENTATION can read ───────────────────────────────────

    @Test
    @DisplayName("PLATFORM_IMPLEMENTATION can get tenant subscription — returns 200")
    void platformImpl_canGetTenantSubscription() throws Exception {
        when(subscriptionService.getActiveSubscriptionForTenant(TENANT_ID)).thenReturn(subDto());

        mockMvc.perform(get(BASE + "/tenant/" + TENANT_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_IMPLEMENTATION))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }
}
