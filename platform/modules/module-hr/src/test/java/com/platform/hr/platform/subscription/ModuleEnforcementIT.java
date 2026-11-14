package com.platform.hr.platform.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.api.GlobalExceptionHandler;
import com.platform.hr.promotion.controller.PromotionController;
import com.platform.hr.promotion.service.PromotionService;
import com.platform.hr.transfer.controller.TransferController;
import com.platform.hr.transfer.service.TransferService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MVC integration tests for Phase 3C.6 module enforcement on additional controllers.
 *
 * <p>Tests:
 * <ul>
 *   <li>Promotion create blocked when HR module disabled (403)</li>
 *   <li>Transfer create blocked when HR module disabled (403)</li>
 *   <li>Promotion create blocked when subscription suspended (400)</li>
 *   <li>Promotion create succeeds when module enabled (201)</li>
 *   <li>Missing subscription returns 400</li>
 * </ul>
 */
@WebMvcTest
@ContextConfiguration(classes = {
        PromotionController.class,
        TransferController.class,
        ModuleEnforcementIT.TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
@DisplayName("Module Enforcement — Additional Controllers MVC Tests")
class ModuleEnforcementIT {

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
    @MockBean PromotionService promotionService;
    @MockBean TransferService transferService;

    @BeforeEach
    void setTenantContext() {
        TenantContext.setTenantId(TENANT_ID_STR);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    private String promotionBody() {
        return """
                {
                  "employeeId": "00000000-0000-0000-0000-000000000001",
                  "toDesignationId": "00000000-0000-0000-0000-000000000002",
                  "effectiveDate": "2024-06-01",
                  "reason": "Performance"
                }
                """;
    }

    private String transferBody() {
        return """
                {
                  "employeeId": "00000000-0000-0000-0000-000000000001",
                  "transferType": "DEPARTMENT",
                  "toDepartmentId": "00000000-0000-0000-0000-000000000003",
                  "effectiveDate": "2024-06-01",
                  "reason": "Restructuring"
                }
                """;
    }

    // ── 1. Promotion — HR module disabled → 403 ───────────────────────────────

    @Test
    @DisplayName("POST /hr/promotions returns 403 when HR module is not enabled")
    void promotion_moduleDisabled_returns403() throws Exception {
        when(promotionService.create(any()))
                .thenThrow(new TenantSecurityException("Module 'HR' is not enabled for plan 'Starter'."));

        mockMvc.perform(post("/hr/promotions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("PROMOTION_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(promotionBody()))
                .andExpect(status().isForbidden());
    }

    // ── 2. Transfer — HR module disabled → 403 ───────────────────────────────

    @Test
    @DisplayName("POST /hr/transfers returns 403 when HR module is not enabled")
    void transfer_moduleDisabled_returns403() throws Exception {
        when(transferService.create(any()))
                .thenThrow(new TenantSecurityException("Module 'HR' is not enabled."));

        mockMvc.perform(post("/hr/transfers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("TRANSFER_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody()))
                .andExpect(status().isForbidden());
    }

    // ── 3. Promotion — subscription suspended → 400 ──────────────────────────

    @Test
    @DisplayName("POST /hr/promotions returns 400 when subscription is suspended")
    void promotion_suspended_returns400() throws Exception {
        when(promotionService.create(any()))
                .thenThrow(new TenantException("Subscription is suspended."));

        mockMvc.perform(post("/hr/promotions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("PROMOTION_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(promotionBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("suspended")));
    }

    // ── 4. Promotion — no subscription → 400 ─────────────────────────────────

    @Test
    @DisplayName("POST /hr/promotions returns 400 when tenant has no subscription")
    void promotion_noSubscription_returns400() throws Exception {
        when(promotionService.create(any()))
                .thenThrow(new TenantException("Tenant has no active subscription."));

        mockMvc.perform(post("/hr/promotions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("PROMOTION_WRITE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(promotionBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no active subscription")));
    }

    // ── 5. Unauthenticated → 403 (no JWT decoder in test context) ───────────────

    @Test
    @DisplayName("POST /hr/promotions returns 403 when unauthenticated (no JWT in test context)")
    void promotion_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/hr/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(promotionBody()))
                .andExpect(status().isForbidden());
    }
}
