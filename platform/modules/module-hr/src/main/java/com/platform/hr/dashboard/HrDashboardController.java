package com.platform.hr.dashboard;

import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.tenancy.TenantContext;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/hr/dashboard")
@RequiredArgsConstructor
public class HrDashboardController {

    private final EmployeeRepository employeeRepository;

    @GetMapping("/stats")
    public DashboardStats getStats() {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return DashboardStats.builder()
            .totalEmployees(employeeRepository.count())
            .activeEmployees(employeeRepository.countByCompanyIdAndEmploymentStatus(companyId, EmploymentStatus.ACTIVE))
            .onProbation(employeeRepository.countByCompanyIdAndEmploymentStatus(companyId, EmploymentStatus.PROBATION))
            .build();
    }

    @Getter
    @Builder
    public static class DashboardStats {
        private long totalEmployees;
        private long activeEmployees;
        private long onProbation;
    }
}
