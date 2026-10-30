package com.platform.hr.branch.service;

import com.platform.api.PagedResponse;
import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.cache.service.CacheService;
import com.platform.hr.branch.dto.BranchDto;
import com.platform.hr.branch.entity.Branch;
import com.platform.hr.branch.mapper.BranchMapper;
import com.platform.hr.branch.repository.BranchRepository;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final CacheService cacheService;

    private static final String CACHE_PREFIX = "branches:";

    @Auditable(entity = "Branch", action = AuditLog.AuditAction.CREATE)
    public BranchDto.Response create(BranchDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());

        if (branchRepository.existsByCodeAndCompanyId(request.getCode(), companyId)) {
            throw PlatformException.conflict("Branch code already exists: " + request.getCode());
        }
        if (branchRepository.existsByNameAndCompanyId(request.getName(), companyId)) {
            throw PlatformException.conflict("Branch name already exists: " + request.getName());
        }

        Branch branch = branchMapper.toEntity(request);
        branch.setCompanyId(companyId);
        Branch saved = branchRepository.save(branch);

        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
        log.info("Branch created: {} for company: {}", saved.getCode(), companyId);
        return toResponseWithCount(saved);
    }

    @Transactional(readOnly = true)
    public BranchDto.Response findById(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Branch branch = getActive(id, companyId);
        return toResponseWithCount(branch);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BranchDto.Response> search(String query, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return PagedResponse.of(
            branchRepository.search(companyId, query, pageable)
                .map(this::toResponseWithCount)
        );
    }

    @Transactional(readOnly = true)
    public List<BranchDto.Summary> findAll() {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        String cacheKey = CACHE_PREFIX + companyId + ":all";

        @SuppressWarnings("unchecked")
        List<BranchDto.Summary> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) return cached;

        List<BranchDto.Summary> result = branchRepository
            .findByCompanyIdAndIsActiveTrue(companyId)
            .stream().map(branchMapper::toSummary).toList();

        cacheService.set(cacheKey, result, 120);
        return result;
    }

    @Auditable(entity = "Branch", action = AuditLog.AuditAction.UPDATE)
    public BranchDto.Response update(UUID id, BranchDto.UpdateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Branch branch = getActive(id, companyId);

        branchMapper.updateEntity(request, branch);
        Branch saved = branchRepository.save(branch);

        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
        return toResponseWithCount(saved);
    }

    @Auditable(entity = "Branch", action = AuditLog.AuditAction.DELETE)
    public void deactivate(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Branch branch = getActive(id, companyId);

        long employeeCount = branchRepository.countActiveEmployeesByBranchId(id);
        if (employeeCount > 0) {
            throw PlatformException.badRequest(
                "Cannot deactivate branch with " + employeeCount + " active employees. Reassign them first.");
        }

        branch.setIsActive(false);
        branchRepository.save(branch);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
        log.info("Branch deactivated: {}", id);
    }

    private Branch getActive(UUID id, UUID companyId) {
        return branchRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Branch"));
    }

    private BranchDto.Response toResponseWithCount(Branch branch) {
        BranchDto.Response response = branchMapper.toResponse(branch);
        response.setEmployeeCount(branchRepository.countActiveEmployeesByBranchId(branch.getId()));
        return response;
    }
}
