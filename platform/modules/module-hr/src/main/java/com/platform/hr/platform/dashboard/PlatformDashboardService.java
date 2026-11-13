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

        long totalTenants = tenantRepository.count() - 1; // exclude platform pseudo-tenant
        long activeTenants = tenantRepository.findAll().stream()
                .filter(t -> !t.getId().equals(platformTenantId))
                .filter(t -> t.getStatus() == Tenant.TenantStatus.ACTIVE && Boolean.TRUE.equals(t.getIsActive()))
                .count();
        long suspendedTenants = tenantRepository.findAll().stream()
                .filter(t -> !t.getId().equals(platformTenantId))
                .filter(t -> t.getStatus() == Tenant.TenantStatus.SUSPENDED)
                .count();

        // Platform employees = ACTIVE memberships in the platform pseudo-tenant
        long platformEmployees = membershipRepository
                .findByTenantIdAndMembershipStatus(platformTenantId, MembershipStatus.ACTIVE)
                .size();

        return PlatformDashboardDto.builder()
                .totalTenants(totalTenants)
                .activeTenants(activeTenants)
                .pendingApprovals(0L)   // PENDING_REVIEW status added in 3C.2
                .suspendedTenants(suspendedTenants)
                .totalPlatformEmployees(platformEmployees)
                .openSupportTickets(0L) // support tickets added in 3C.6
                .build();
    }
}
