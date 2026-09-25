package com.hrms.company.service;

import com.hrms.audit.entity.AuditLog.AuditAction;
import com.hrms.audit.service.AuditService;
import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.company.dto.CompanyDto;
import com.hrms.company.entity.Company;
import com.hrms.company.entity.Company.CompanyStatus;
import com.hrms.company.mapper.CompanyMapper;
import com.hrms.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final AuditService auditService;

    @Override
    public CompanyDto.Response create(CompanyDto.Request request) {
        if (request.getRegistrationNumber() != null &&
                companyRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Company", "registrationNumber", request.getRegistrationNumber());
        }
        Company company = companyMapper.toEntity(request);
        company.setStatus(CompanyStatus.ACTIVE);
        Company saved = companyRepository.save(company);
        log.info("Company created: {}", saved.getId());
        auditService.log(saved.getId(), "Company", saved.getId().toString(),
                AuditAction.CREATE, null, saved, SecurityUtils.getCurrentUsername(), null);
        return companyMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDto.Response findById(UUID id) {
        return companyRepository.findByIdAndActiveTrue(id)
                .map(companyMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDto.Response> findAll(Pageable pageable) {
        return new PagedResponse<>(companyRepository.findAllByActiveTrue(pageable)
                .map(companyMapper::toResponse));
    }

    @Override
    public CompanyDto.Response update(UUID id, CompanyDto.Request request) {
        Company company = companyRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        CompanyDto.Response oldValue = companyMapper.toResponse(company);
        companyMapper.updateEntity(request, company);
        Company updated = companyRepository.save(company);
        auditService.log(id, "Company", id.toString(),
                AuditAction.UPDATE, oldValue, companyMapper.toResponse(updated),
                SecurityUtils.getCurrentUsername(), null);
        return companyMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Company company = companyRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        company.setActive(false);
        companyRepository.save(company);
        auditService.log(id, "Company", id.toString(),
                AuditAction.DELETE, null, null, SecurityUtils.getCurrentUsername(), null);
        log.info("Company soft-deleted: {}", id);
    }
}
