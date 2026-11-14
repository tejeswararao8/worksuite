package com.platform.hr.invitation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class TenantInvitationDto {

    @Getter
    @Builder
    public static class Response {
        private UUID id;
        private UUID tenantId;
        private UUID employeeId;
        private String email;
        private String roleName;
        private TenantEmployeeInvitation.InvitationStatus status;
        private String invitedBy;
        private LocalDateTime expiresAt;
        private LocalDateTime acceptedAt;
        private LocalDateTime createdAt;

        /**
         * true when email was successfully delivered.
         * false when invitation was saved but email delivery failed.
         */
        @Builder.Default
        private boolean emailDelivered = true;

        public static Response from(TenantEmployeeInvitation inv) {
            return Response.builder()
                    .id(inv.getId())
                    .tenantId(inv.getTenantId())
                    .employeeId(inv.getEmployeeId())
                    .email(inv.getEmail())
                    .roleName(inv.getRoleName())
                    .status(inv.getStatus())
                    .invitedBy(inv.getInvitedBy())
                    .expiresAt(inv.getExpiresAt())
                    .acceptedAt(inv.getAcceptedAt())
                    .createdAt(inv.getCreatedAt())
                    .emailDelivered(true)
                    .build();
        }

        public static Response fromWithDeliveryFailure(TenantEmployeeInvitation inv) {
            return Response.builder()
                    .id(inv.getId())
                    .tenantId(inv.getTenantId())
                    .employeeId(inv.getEmployeeId())
                    .email(inv.getEmail())
                    .roleName(inv.getRoleName())
                    .status(inv.getStatus())
                    .invitedBy(inv.getInvitedBy())
                    .expiresAt(inv.getExpiresAt())
                    .acceptedAt(inv.getAcceptedAt())
                    .createdAt(inv.getCreatedAt())
                    .emailDelivered(false)
                    .build();
        }
    }

    @Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    public static class InviteRequest {
        @NotNull
        private UUID employeeId;

        @NotBlank
        private String roleName;
    }

    @Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    public static class AcceptRequest {
        /**
         * Raw invitation token from the email.
         * Transmitted in POST body only — never in a query string.
         */
        @NotBlank
        @Size(max = 128)
        private String token;
    }
}
