package com.platform.hr.platform.employees;

import com.platform.tenancy.membership.MembershipStatus;
import com.platform.tenancy.membership.UserTenantMembership;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read DTO for a platform employee.
 * A platform employee is a UserTenantMembership in the platform pseudo-tenant.
 */
@Getter
@Builder
public class PlatformEmployeeDto {

    private UUID membershipId;
    private String keycloakUserId;
    private String roleName;
    private MembershipStatus membershipStatus;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;

    public static PlatformEmployeeDto from(UserTenantMembership m) {
        return PlatformEmployeeDto.builder()
                .membershipId(m.getId())
                .keycloakUserId(m.getKeycloakUserId())
                .roleName(m.getRoleName())
                .membershipStatus(m.getMembershipStatus())
                .joinedAt(m.getJoinedAt())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
