package com.platform.hr.dashboard;

import com.platform.api.ApiResponse;
import com.platform.hr.asset.entity.Asset.AssetStatus;
import com.platform.hr.asset.repository.AssetRepository;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.onboarding.entity.OnboardingChecklist.TaskStatus;
import com.platform.hr.onboarding.repository.OnboardingRepository;
import com.platform.hr.probation.entity.Probation.ProbationStatus;
import com.platform.hr.probation.repository.ProbationRepository;
import com.platform.hr.promotion.entity.Promotion.PromotionStatus;
import com.platform.hr.promotion.repository.PromotionRepository;
import com.platform.hr.transfer.entity.Transfer.TransferStatus;
import com.platform.hr.transfer.repository.TransferRepository;
import com.platform.tenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Dashboard")
public class HrDashboardController {

    private final EmployeeRepository employeeRepository;
    private final ProbationRepository probationRepository;
    private final TransferRepository transferRepository;
    private final PromotionRepository promotionRepository;
    private final OnboardingRepository onboardingRepository;
    private final AssetRepository assetRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get HR dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);

        DashboardStats stats = DashboardStats.builder()
            // Employee counts
            .totalEmployees(employeeRepository.countByCompanyIdAndIsActiveTrue(companyId))
            .activeEmployees(employeeRepository.countByCompanyIdAndEmploymentStatus(companyId, EmploymentStatus.ACTIVE))
            .onProbation(employeeRepository.countByCompanyIdAndEmploymentStatus(companyId, EmploymentStatus.PROBATION))
            .onLeave(employeeRepository.countByCompanyIdAndEmploymentStatus(companyId, EmploymentStatus.ON_LEAVE))
            .resigned(employeeRepository.countByCompanyIdAndEmploymentStatus(companyId, EmploymentStatus.RESIGNED))
            // Pending approvals
            .pendingTransfers(transferRepository
                .findByCompanyIdAndStatusAndIsActiveTrue(companyId, TransferStatus.PENDING, PageRequest.of(0, 1))
                .getTotalElements())
            .pendingPromotions(promotionRepository
                .findByCompanyIdAndStatusAndIsActiveTrue(companyId, PromotionStatus.PENDING, PageRequest.of(0, 1))
                .getTotalElements())
            // Probation ending soon
            .probationEndingSoon(probationRepository
                .findEndingBetween(companyId, today, thirtyDaysLater).size())
            // Onboarding
            .employeesInOnboarding(onboardingRepository.findEmployeesWithPendingTasks(companyId).size())
            // Assets
            .availableAssets(assetRepository.countByCompanyIdAndStatusAndIsActiveTrue(companyId, AssetStatus.AVAILABLE))
            .assignedAssets(assetRepository.countByCompanyIdAndStatusAndIsActiveTrue(companyId, AssetStatus.ASSIGNED))
            .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Getter
    @Builder
    public static class DashboardStats {
        // Headcount
        private long totalEmployees;
        private long activeEmployees;
        private long onProbation;
        private long onLeave;
        private long resigned;
        // Pending approvals
        private long pendingTransfers;
        private long pendingPromotions;
        // Alerts
        private long probationEndingSoon;
        private int employeesInOnboarding;
        // Assets
        private long availableAssets;
        private long assignedAssets;
    }
}
