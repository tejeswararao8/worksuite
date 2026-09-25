package com.hrms.employee.service;

import com.hrms.audit.entity.AuditLog.AuditAction;
import com.hrms.audit.service.AuditService;
import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.employee.dto.EmployeeDto;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.entity.Employee.EmploymentStatus;
import com.hrms.employee.mapper.EmployeeMapper;
import com.hrms.employee.repository.EmployeeRepository;
import com.hrms.employee.specification.EmployeeSpecification;
import com.hrms.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AuditService auditService;
    private final StorageService storageService;

    @Override
    public EmployeeDto.Response create(EmployeeDto.CreateRequest request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        if (employeeRepository.existsByEmailAndCompanyId(request.getEmail(), companyId)) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }
        Employee employee = employeeMapper.toEntity(request);
        employee.setCompanyId(companyId);
        employee.setEmployeeCode(generateEmployeeCode(companyId));
        employee.setEmploymentStatus(EmploymentStatus.PROBATION);
        Employee saved = employeeRepository.save(employee);
        log.info("Employee created: {} for company: {}", saved.getId(), companyId);
        auditService.log(companyId, "Employee", saved.getId().toString(),
                AuditAction.CREATE, null, saved, SecurityUtils.getCurrentUsername(), null);
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto.Response findById(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return employeeRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .map(employeeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeDto.Response> search(String query, UUID departmentId,
                                                       UUID branchId, EmploymentStatus status, Pageable pageable) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return new PagedResponse<>(
                employeeRepository.findAll(
                        EmployeeSpecification.filter(companyId, query, departmentId, branchId, status),
                        pageable
                ).map(employeeMapper::toResponse)
        );
    }

    @Override
    public EmployeeDto.Response update(UUID id, EmployeeDto.UpdateRequest request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Employee employee = employeeRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        EmployeeDto.Response oldValue = employeeMapper.toResponse(employee);
        employeeMapper.updateEntity(request, employee);
        Employee updated = employeeRepository.save(employee);
        auditService.log(companyId, "Employee", id.toString(),
                AuditAction.UPDATE, oldValue, employeeMapper.toResponse(updated),
                SecurityUtils.getCurrentUsername(), null);
        return employeeMapper.toResponse(updated);
    }

    @Override
    public EmployeeDto.Response uploadPhoto(UUID id, MultipartFile file) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Employee employee = employeeRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        String key = "employees/" + companyId + "/" + id + "/photo";
        String url = storageService.upload(file, key);
        employee.setPhotoUrl(url);
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Employee employee = employeeRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        employee.setActive(false);
        employeeRepository.save(employee);
        auditService.log(companyId, "Employee", id.toString(),
                AuditAction.DELETE, null, null, SecurityUtils.getCurrentUsername(), null);
        log.info("Employee soft-deleted: {}", id);
    }

    private String generateEmployeeCode(UUID companyId) {
        int seq = employeeRepository.findMaxEmployeeCodeSequence(companyId) + 1;
        return String.format("EMP%05d", seq);
    }
}
