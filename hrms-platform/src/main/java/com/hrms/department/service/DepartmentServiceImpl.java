package com.hrms.department.service;

import com.hrms.audit.entity.AuditLog.AuditAction;
import com.hrms.audit.service.AuditService;
import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.department.dto.DepartmentDto;
import com.hrms.department.entity.Department;
import com.hrms.department.mapper.DepartmentMapper;
import com.hrms.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final AuditService auditService;

    @Override
    public DepartmentDto.Response create(DepartmentDto.Request request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        if (departmentRepository.existsByCodeAndCompanyId(request.getCode(), companyId)) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }
        Department dept = departmentMapper.toEntity(request);
        dept.setCompanyId(companyId);
        Department saved = departmentRepository.save(dept);
        auditService.log(companyId, "Department", saved.getId().toString(),
                AuditAction.CREATE, null, saved, SecurityUtils.getCurrentUsername(), null);
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto.Response findById(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return departmentRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .map(departmentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DepartmentDto.Response> findAll(Pageable pageable) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return new PagedResponse<>(departmentRepository.findByCompanyIdAndActiveTrue(companyId, pageable)
                .map(departmentMapper::toResponse));
    }

    @Override
    public DepartmentDto.Response update(UUID id, DepartmentDto.Request request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Department dept = departmentRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        departmentMapper.updateEntity(request, dept);
        return departmentMapper.toResponse(departmentRepository.save(dept));
    }

    @Override
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Department dept = departmentRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        dept.setActive(false);
        departmentRepository.save(dept);
        auditService.log(companyId, "Department", id.toString(),
                AuditAction.DELETE, null, null, SecurityUtils.getCurrentUsername(), null);
    }
}
