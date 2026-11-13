package com.platform.hr.platform.tenants;

import com.platform.tenancy.Tenant;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only DTO for tenant data returned to platform admin clients.
 * Never exposes the JPA entity directly.
 */
@Getter
@Builder
public class TenantDto {

    private UUID   id;
    private String name;
    private String slug;
    private String email;
    private String phone;
    private Tenant.TenantStatus status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewNotes;

    public static TenantDto from(Tenant t) {
        return TenantDto.builder()
                .id(t.getId())
                .name(t.getName())
                .slug(t.getSlug())
                .email(t.getEmail())
                .phone(t.getPhone())
                .status(t.getStatus())
                .isActive(t.getIsActive())
                .createdAt(t.getCreatedAt())
                .reviewedBy(t.getReviewedBy())
                .reviewedAt(t.getReviewedAt())
                .reviewNotes(t.getReviewNotes())
                .build();
    }
}
