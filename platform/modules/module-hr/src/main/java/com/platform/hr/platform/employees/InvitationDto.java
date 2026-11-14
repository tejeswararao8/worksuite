package com.platform.hr.platform.employees;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InvitationDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String roleName;
    private PlatformInvitation.InvitationStatus status;
    private String invitedBy;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;

    /**
     * {@code true} when the invitation email was successfully delivered.
     * {@code false} when the invitation was saved but email delivery failed.
     * Callers should warn the admin to resend when this is false.
     */
    @Builder.Default
    private boolean emailDelivered = true;

    public static InvitationDto from(PlatformInvitation inv) {
        return InvitationDto.builder()
                .id(inv.getId())
                .email(inv.getEmail())
                .firstName(inv.getFirstName())
                .lastName(inv.getLastName())
                .roleName(inv.getRoleName())
                .status(inv.getStatus())
                .invitedBy(inv.getInvitedBy())
                .expiresAt(inv.getExpiresAt())
                .acceptedAt(inv.getAcceptedAt())
                .createdAt(inv.getCreatedAt())
                .emailDelivered(true)
                .build();
    }

    /**
     * Factory for when the invitation was saved but email delivery failed.
     * The invitation is preserved and can be resent by an admin.
     */
    public static InvitationDto fromWithDeliveryFailure(PlatformInvitation inv) {
        return InvitationDto.builder()
                .id(inv.getId())
                .email(inv.getEmail())
                .firstName(inv.getFirstName())
                .lastName(inv.getLastName())
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
