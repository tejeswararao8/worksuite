package com.platform.hr.platform.dashboard;

import com.platform.tenancy.Tenant;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityService;
import com.platform.tenancy.membership.MembershipStatus;
import com.platform.tenancy.membership.UserTenantMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatformDashboardService {

    private final TenantRepository tenantRepository;
    private final UserTenantMembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public PlatformDashboardDto getSummary() {
        UUID platformTenantId = TenantSecurityService.PLATFORM_TENANT_ID;

        long totalTenants     = tenantRepository.countExcluding(platformTenantId);
        long activeTenants    = tenantRepository.countByStatusExcluding(platformTenantId, Tenant.TenantStatus.ACTIVE);
        long suspendedTenants = tenantRepository.countByStatusExcluding(platformTenantId, Tenant.TenantStatus.SUSPENDED);
        long pendingApprovals = tenantRepository.countByStatusExcluding(platformTenantId, Tenant.TenantStatus.PENDING_REVIEW);

        // Platform employees = ACTIVE memberships in the platform pseudo-tenant.
        long platformEmployees = membershipRepository
                .countByTenantIdAndMembershipStatus(platformTenantId, MembershipStatus.ACTIVE);

        // pendingApprovals: requires PENDING_REVIEW TenantStatus — added in Phase 3C.2.
        // openSupportTickets: requires support ticket module — added in Phase 3C.6.
        // Both are returned as 0 and are clearly labelled as unavailable in the UI.
        return PlatformDashboardDto.builder()
                .totalTenants(totalTenants)
                .activeTenants(activeTenants)
                .pendingApprovals(pendingApprovals)
                .suspendedTenants(suspendedTenants)
                .totalPlatformEmployees(platformEmployees)
                .openSupportTickets(0L)
                .build();
    }
}
