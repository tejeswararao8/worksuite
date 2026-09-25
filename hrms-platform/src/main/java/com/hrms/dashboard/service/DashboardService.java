package com.hrms.dashboard.service;

import com.hrms.asset.entity.Asset.AssetStatus;
import com.hrms.asset.repository.AssetRepository;
import com.hrms.common.util.SecurityUtils;
import com.hrms.document.repository.DocumentRepository;
import com.hrms.employee.repository.EmployeeRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;
    private final AssetRepository assetRepository;

    public Summary getSummary() {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Summary s = new Summary();
        s.setTotalEmployees(employeeRepository.countByCompanyIdAndActiveTrue(companyId));
        s.setActiveEmployees(employeeRepository.countByCompanyIdAndEmploymentStatusAndActiveTrue(
                companyId, com.hrms.employee.entity.Employee.EmploymentStatus.ACTIVE));
        s.setOnProbation(employeeRepository.countByCompanyIdAndEmploymentStatusAndActiveTrue(
                companyId, com.hrms.employee.entity.Employee.EmploymentStatus.PROBATION));
        s.setDocumentsExpiringIn30Days(documentRepository.countExpiringBefore(
                companyId, LocalDate.now().plusDays(30)));
        return s;
    }

    public List<Map<String, Object>> getHeadcountByDepartment() {
        return employeeRepository.countByDepartmentAndCompanyId(SecurityUtils.getCurrentCompanyId());
    }

    public List<Map<String, Object>> getHeadcountByBranch() {
        return employeeRepository.countByBranchAndCompanyId(SecurityUtils.getCurrentCompanyId());
    }

    public List<Map<String, Object>> getJoiningTrend(int months) {
        LocalDate from = LocalDate.now().minusMonths(months);
        return employeeRepository.joiningTrend(SecurityUtils.getCurrentCompanyId(), from);
    }

    public Map<String, Long> getDocumentExpiryAlerts() {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        LocalDate now = LocalDate.now();
        return Map.of(
                "expiring7Days",  documentRepository.countExpiringBefore(companyId, now.plusDays(7)),
                "expiring30Days", documentRepository.countExpiringBefore(companyId, now.plusDays(30)),
                "expiring60Days", documentRepository.countExpiringBefore(companyId, now.plusDays(60)),
                "expiring90Days", documentRepository.countExpiringBefore(companyId, now.plusDays(90))
        );
    }

    public List<Map<String, Object>> getAssetUtilization() {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return Arrays.stream(AssetStatus.values())
                .map(status -> {
                    long count = assetRepository.findByCompanyIdAndActiveTrue(companyId,
                            org.springframework.data.domain.Pageable.unpaged()).stream()
                            .filter(a -> a.getStatus() == status).count();
                    return Map.<String, Object>of("status", status.name(), "count", count);
                })
                .collect(Collectors.toList());
    }

    @Data
    public static class Summary {
        private long totalEmployees;
        private long activeEmployees;
        private long onProbation;
        private long documentsExpiringIn30Days;
    }
}
