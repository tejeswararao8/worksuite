package com.platform.hr.platform.tenants;

import com.platform.tenancy.Tenant;
import com.platform.tenancy.TenantLifecycleEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TenantLifecycleEventDto {

    private UUID id;
    private UUID tenantId;
    private String actorId;
    private String actorRole;
    private Tenant.TenantStatus fromStatus;
    private Tenant.TenantStatus toStatus;
    private String reason;
    private LocalDateTime occurredAt;

    public static TenantLifecycleEventDto from(TenantLifecycleEvent e) {
        return TenantLifecycleEventDto.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .actorId(e.getActorId())
                .actorRole(e.getActorRole())
                .fromStatus(e.getFromStatus())
                .toStatus(e.getToStatus())
                .reason(e.getReason())
                .occurredAt(e.getOccurredAt())
                .build();
    }
}
