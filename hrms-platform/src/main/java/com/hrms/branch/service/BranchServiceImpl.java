package com.hrms.branch.service;

import com.hrms.audit.entity.AuditLog.AuditAction;
import com.hrms.audit.service.AuditService;
import com.hrms.branch.dto.BranchDto;
import com.hrms.branch.entity.Branch;
import com.hrms.branch.mapper.BranchMapper;
import com.hrms.branch.repository.BranchRepository;
import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
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
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final AuditService auditService;

    @Override
    public BranchDto.Response create(BranchDto.Request request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        if (branchRepository.existsByCodeAndCompanyId(request.getCode(), companyId)) {
            throw new DuplicateResourceException("Branch", "code", request.getCode());
        }
        Branch branch = branchMapper.toEntity(request);
        branch.setCompanyId(companyId);
        Branch saved = branchRepository.save(branch);
        auditService.log(companyId, "Branch", saved.getId().toString(),
                AuditAction.CREATE, null, saved, SecurityUtils.getCurrentUsername(), null);
        return branchMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchDto.Response findById(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return branchRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .map(branchMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BranchDto.Response> findAll(Pageable pageable) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return new PagedResponse<>(branchRepository.findByCompanyIdAndActiveTrue(companyId, pageable)
                .map(branchMapper::toResponse));
    }

    @Override
    public BranchDto.Response update(UUID id, BranchDto.Request request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Branch branch = branchRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branchMapper.updateEntity(request, branch);
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Branch branch = branchRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setActive(false);
        branchRepository.save(branch);
        auditService.log(companyId, "Branch", id.toString(),
                AuditAction.DELETE, null, null, SecurityUtils.getCurrentUsername(), null);
    }
}
