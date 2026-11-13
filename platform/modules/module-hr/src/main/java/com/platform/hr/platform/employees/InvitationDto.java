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
                .build();
    }
}
