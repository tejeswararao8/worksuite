package com.hrms.designation.service;

import com.hrms.audit.entity.AuditLog.AuditAction;
import com.hrms.audit.service.AuditService;
import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.designation.dto.DesignationDto;
import com.hrms.designation.entity.Designation;
import com.hrms.designation.mapper.DesignationMapper;
import com.hrms.designation.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DesignationMapper designationMapper;
    private final AuditService auditService;

    @Override
    public DesignationDto.Response create(DesignationDto.Request request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Designation designation = designationMapper.toEntity(request);
        designation.setCompanyId(companyId);
        Designation saved = designationRepository.save(designation);
        auditService.log(companyId, "Designation", saved.getId().toString(),
                AuditAction.CREATE, null, saved, SecurityUtils.getCurrentUsername(), null);
        return designationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationDto.Response findById(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return designationRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .map(designationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DesignationDto.Response> findAll(Pageable pageable) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return new PagedResponse<>(designationRepository.findByCompanyIdAndActiveTrue(companyId, pageable)
                .map(designationMapper::toResponse));
    }

    @Override
    public DesignationDto.Response update(UUID id, DesignationDto.Request request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Designation designation = designationRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", id));
        designationMapper.updateEntity(request, designation);
        return designationMapper.toResponse(designationRepository.save(designation));
    }

    @Override
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Designation designation = designationRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", id));
        designation.setActive(false);
        designationRepository.save(designation);
        auditService.log(companyId, "Designation", id.toString(),
                AuditAction.DELETE, null, null, SecurityUtils.getCurrentUsername(), null);
    }
}
