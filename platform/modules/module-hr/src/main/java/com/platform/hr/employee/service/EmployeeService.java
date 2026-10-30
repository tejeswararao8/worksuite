package com.platform.hr.employee.service;

import com.platform.api.PagedResponse;
import com.platform.audit.AuditLog;
import com.platform.audit.AuditService;
import com.platform.audit.Auditable;
import com.platform.api.PlatformException;
import com.platform.config.ConfigService;
import com.platform.document.MinioStorageService;
import com.platform.hr.employee.dto.EmployeeDto;
import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import com.platform.hr.employee.mapper.EmployeeMapper;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.employee.specification.EmployeeSpecification;
import com.platform.hr.onboarding.service.OnboardingService;
import com.platform.hr.probation.service.ProbationService;
import com.platform.security.user.UserPrincipal;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AuditService auditService;
    private final MinioStorageService storageService;
    private final OnboardingService onboardingService;
    private final ProbationService probationService;
    private final ConfigService configService;

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.CREATE)
    public EmployeeDto.Response create(EmployeeDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        if (employeeRepository.existsByEmailAndCompanyId(request.getEmail(), companyId)) {
            throw PlatformException.conflict("Employee with email already exists: " + request.getEmail());
        }
        Employee employee = employeeMapper.toEntity(request);
        employee.setCompanyId(companyId);
        employee.setEmployeeCode(generateCode(companyId));
        employee.setEmploymentStatus(EmploymentStatus.PROBATION);
        Employee saved = employeeRepository.save(employee);

        // Auto-start probation
        int probationDays = configService.getInt("hr.probation.default_days", 90);
        probationService.startProbation(saved.getId(), saved.getJoiningDate(), companyId, probationDays);

        // Auto-create onboarding tasks
        onboardingService.createDefaultTasks(saved.getId(), saved.getJoiningDate(), companyId);

        return employeeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeDto.Response findById(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .map(employeeMapper::toResponse)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
    }

    @Transactional(readOnly = true)
    public PagedResponse<EmployeeDto.Response> search(String query, UUID departmentId,
                                                       UUID branchId, EmploymentStatus status,
                                                       Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return PagedResponse.of(
            employeeRepository.findAll(
                EmployeeSpecification.filter(companyId, query, departmentId, branchId, status), pageable
            ).map(employeeMapper::toResponse)
        );
    }

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.UPDATE)
    public EmployeeDto.Response update(UUID id, EmployeeDto.UpdateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Employee employee = employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
        employeeMapper.updateEntity(request, employee);
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    public EmployeeDto.Response uploadPhoto(UUID id, MultipartFile file) throws IOException {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Employee employee = employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
        String path;
        try {
            path = storageService.store(file, companyId.toString(), "employees", id.toString());
        } catch (Exception e) {
            throw new RuntimeException("Photo upload failed: " + e.getMessage(), e);
        }
        employee.setPhotoUrl(path);
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.DELETE)
    public void delete(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Employee employee = employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }

    private String generateCode(UUID companyId) {
        int seq = employeeRepository.findMaxEmployeeCodeSequence(companyId) + 1;
        return String.format("EMP%05d", seq);
    }
}
