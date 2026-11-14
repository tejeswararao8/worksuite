package com.platform.hr.invitation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.TenantSecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TenantInvitationController.class)
@ContextConfiguration(classes = com.platform.hr.TestApplication.class)
@DisplayName("TenantInvitationController — Security IT")
class TenantInvitationSecurityIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TenantInvitationService invitationService;

    // ── 1. Unauthenticated request rejected ───────────────────────────────────

    @Test
    @DisplayName("POST /hr/invitations — unauthenticated returns 401")
    void invite_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/hr/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":\"" + UUID.randomUUID() + "\",\"roleName\":\"EMPLOYEE\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── 2. Non-admin tenant user cannot invite employees ──────────────────────

    @Test
    @DisplayName("POST /hr/invitations — EMPLOYEE role returns 403")
    @WithMockUser(authorities = {"EMPLOYEE_READ"})
    void invite_employeeRole_returns403() throws Exception {
        mockMvc.perform(post("/hr/invitations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":\"" + UUID.randomUUID() + "\",\"roleName\":\"EMPLOYEE\"}"))
                .andExpect(status().isForbidden());
    }

    // ── 3. Authorized user can invite ─────────────────────────────────────────

    @Test
    @DisplayName("POST /hr/invitations — EMPLOYEE_WRITE returns 201")
    @WithMockUser(authorities = {"EMPLOYEE_WRITE"})
    void invite_authorizedUser_returns201() throws Exception {
        TenantInvitationDto.Response resp = TenantInvitationDto.Response.builder()
                .id(UUID.randomUUID())
                .status(TenantEmployeeInvitation.InvitationStatus.PENDING)
                .emailDelivered(true)
                .build();
        when(invitationService.invite(any())).thenReturn(resp);

        mockMvc.perform(post("/hr/invitations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":\"" + UUID.randomUUID() + "\",\"roleName\":\"EMPLOYEE\"}"))
                .andExpect(status().isCreated());
    }

    // ── 4. Accept endpoint requires authentication ────────────────────────────

    @Test
    @DisplayName("POST /hr/invitations/accept — unauthenticated returns 401")
    void accept_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/hr/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"sometoken\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── 5. Accept endpoint accessible to any authenticated user ──────────────

    @Test
    @DisplayName("POST /hr/invitations/accept — authenticated user can call accept")
    @WithMockUser
    void accept_authenticated_callsService() throws Exception {
        TenantInvitationDto.Response resp = TenantInvitationDto.Response.builder()
                .id(UUID.randomUUID())
                .status(TenantEmployeeInvitation.InvitationStatus.ACCEPTED)
                .emailDelivered(true)
                .build();
        when(invitationService.acceptInvitation(any())).thenReturn(resp);

        mockMvc.perform(post("/hr/invitations/accept")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"sometoken\"}"))
                .andExpect(status().isOk());
    }

    // ── 6. Resend requires EMPLOYEE_WRITE ─────────────────────────────────────

    @Test
    @DisplayName("POST /hr/invitations/{id}/resend — EMPLOYEE_READ returns 403")
    @WithMockUser(authorities = {"EMPLOYEE_READ"})
    void resend_readOnly_returns403() throws Exception {
        mockMvc.perform(post("/hr/invitations/" + UUID.randomUUID() + "/resend")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── 7. Revoke requires EMPLOYEE_WRITE ─────────────────────────────────────

    @Test
    @DisplayName("POST /hr/invitations/{id}/revoke — EMPLOYEE_READ returns 403")
    @WithMockUser(authorities = {"EMPLOYEE_READ"})
    void revoke_readOnly_returns403() throws Exception {
        mockMvc.perform(post("/hr/invitations/" + UUID.randomUUID() + "/revoke")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
