package com.platform.hr.designation.service;

import com.platform.api.PagedResponse;
import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.cache.service.CacheService;
import com.platform.hr.designation.dto.DesignationDto;
import com.platform.hr.designation.entity.Designation;
import com.platform.hr.designation.mapper.DesignationMapper;
import com.platform.hr.designation.repository.DesignationRepository;
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
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final DesignationMapper designationMapper;
    private final CacheService cacheService;

    private static final String CACHE_PREFIX = "designations:";

    @Auditable(entity = "Designation", action = AuditLog.AuditAction.CREATE)
    public DesignationDto.Response create(DesignationDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());

        if (designationRepository.existsByTitleAndCompanyId(request.getTitle(), companyId)) {
            throw PlatformException.conflict("Designation already exists: " + request.getTitle());
        }

        Designation designation = designationMapper.toEntity(request);
        designation.setCompanyId(companyId);
        Designation saved = designationRepository.save(designation);

        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
        return enrichResponse(saved);
    }

    @Transactional(readOnly = true)
    public DesignationDto.Response findById(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return enrichResponse(getActive(id, companyId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DesignationDto.Response> search(String query, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return PagedResponse.of(
            designationRepository.search(companyId, query, pageable).map(this::enrichResponse)
        );
    }

    @Transactional(readOnly = true)
    public List<DesignationDto.Summary> findAll() {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        String cacheKey = CACHE_PREFIX + companyId + ":all";

        @SuppressWarnings("unchecked")
        List<DesignationDto.Summary> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) return cached;

        List<DesignationDto.Summary> result = designationRepository
            .findByCompanyIdAndIsActiveTrueOrderByLevelAsc(companyId)
            .stream().map(designationMapper::toSummary).toList();

        cacheService.set(cacheKey, result, 120);
        return result;
    }

    @Auditable(entity = "Designation", action = AuditLog.AuditAction.UPDATE)
    public DesignationDto.Response update(UUID id, DesignationDto.UpdateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Designation designation = getActive(id, companyId);
        designationMapper.updateEntity(request, designation);
        Designation saved = designationRepository.save(designation);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
        return enrichResponse(saved);
    }

    @Auditable(entity = "Designation", action = AuditLog.AuditAction.DELETE)
    public void deactivate(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Designation designation = getActive(id, companyId);

        long count = designationRepository.countActiveEmployeesByDesignationId(id);
        if (count > 0) {
            throw PlatformException.badRequest(
                "Cannot deactivate designation with " + count + " active employees");
        }

        designation.setIsActive(false);
        designationRepository.save(designation);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
    }

    private Designation getActive(UUID id, UUID companyId) {
        return designationRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Designation"));
    }

    private DesignationDto.Response enrichResponse(Designation d) {
        DesignationDto.Response response = designationMapper.toResponse(d);
        response.setEmployeeCount(designationRepository.countActiveEmployeesByDesignationId(d.getId()));
        return response;
    }
}
