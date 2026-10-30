package com.platform.hr.department.service;

import com.platform.api.PagedResponse;
import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.cache.service.CacheService;
import com.platform.hr.department.dto.DepartmentDto;
import com.platform.hr.department.entity.Department;
import com.platform.hr.department.mapper.DepartmentMapper;
import com.platform.hr.department.repository.DepartmentRepository;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final EmployeeRepository employeeRepository;
    private final CacheService cacheService;

    private static final String CACHE_PREFIX = "departments:";

    @Auditable(entity = "Department", action = AuditLog.AuditAction.CREATE)
    public DepartmentDto.Response create(DepartmentDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());

        if (departmentRepository.existsByCodeAndCompanyId(request.getCode(), companyId)) {
            throw PlatformException.conflict("Department code already exists: " + request.getCode());
        }
        if (departmentRepository.existsByNameAndCompanyId(request.getName(), companyId)) {
            throw PlatformException.conflict("Department name already exists: " + request.getName());
        }
        if (request.getParentDepartmentId() != null) {
            validateParentExists(request.getParentDepartmentId(), companyId);
        }
        if (request.getHeadEmployeeId() != null) {
            validateEmployeeExists(request.getHeadEmployeeId(), companyId);
        }

        Department department = departmentMapper.toEntity(request);
        department.setCompanyId(companyId);
        Department saved = departmentRepository.save(department);

        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
        return enrichResponse(saved);
    }

    @Transactional(readOnly = true)
    public DepartmentDto.Response findById(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return enrichResponse(getActive(id, companyId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DepartmentDto.Response> search(String query, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return PagedResponse.of(
            departmentRepository.search(companyId, query, pageable).map(this::enrichResponse)
        );
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto.Summary> findAll() {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        String cacheKey = CACHE_PREFIX + companyId + ":all";

        @SuppressWarnings("unchecked")
        List<DepartmentDto.Summary> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) return cached;

        List<DepartmentDto.Summary> result = departmentRepository
            .findByCompanyIdAndIsActiveTrue(companyId)
            .stream().map(departmentMapper::toSummary).toList();

        cacheService.set(cacheKey, result, 120);
        return result;
    }

    // ── Org chart — builds full tree ──────────────────────────
    @Transactional(readOnly = true)
    public List<DepartmentDto.OrgNode> getOrgChart() {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        List<Department> roots = departmentRepository
            .findByParentDepartmentIdIsNullAndCompanyIdAndIsActiveTrue(companyId);
        return roots.stream().map(d -> buildOrgNode(d, companyId)).toList();
    }

    @Auditable(entity = "Department", action = AuditLog.AuditAction.UPDATE)
    public DepartmentDto.Response update(UUID id, DepartmentDto.UpdateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Department department = getActive(id, companyId);

        // Circular reference check
        if (request.getParentDepartmentId() != null) {
            if (request.getParentDepartmentId().equals(id)) {
                throw PlatformException.badRequest("Department cannot be its own parent");
            }
            validateParentExists(request.getParentDepartmentId(), companyId);
            if (isDescendant(request.getParentDepartmentId(), id, companyId)) {
                throw PlatformException.badRequest("Circular department hierarchy not allowed");
            }
        }
        if (request.getHeadEmployeeId() != null) {
            validateEmployeeExists(request.getHeadEmployeeId(), companyId);
        }

        departmentMapper.updateEntity(request, department);
        Department saved = departmentRepository.save(department);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
        return enrichResponse(saved);
    }

    @Auditable(entity = "Department", action = AuditLog.AuditAction.DELETE)
    public void deactivate(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Department department = getActive(id, companyId);

        long empCount = departmentRepository.countActiveEmployeesByDepartmentId(id);
        if (empCount > 0) {
            throw PlatformException.badRequest(
                "Cannot deactivate department with " + empCount + " active employees");
        }
        long childCount = departmentRepository
            .findByParentDepartmentIdAndCompanyIdAndIsActiveTrue(id, companyId).size();
        if (childCount > 0) {
            throw PlatformException.badRequest(
                "Cannot deactivate department with " + childCount + " sub-departments");
        }

        department.setIsActive(false);
        departmentRepository.save(department);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
    }

    // ── Private helpers ────────────────────────────────────────
    private Department getActive(UUID id, UUID companyId) {
        return departmentRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Department"));
    }

    private DepartmentDto.Response enrichResponse(Department dept) {
        DepartmentDto.Response response = departmentMapper.toResponse(dept);
        response.setEmployeeCount(departmentRepository.countActiveEmployeesByDepartmentId(dept.getId()));
        response.setTeamCount(departmentRepository.countActiveTeamsByDepartmentId(dept.getId()));

        if (dept.getParentDepartmentId() != null) {
            departmentRepository.findById(dept.getParentDepartmentId())
                .ifPresent(p -> response.setParentDepartmentName(p.getName()));
        }
        if (dept.getHeadEmployeeId() != null) {
            employeeRepository.findById(dept.getHeadEmployeeId())
                .ifPresent(e -> response.setHeadEmployeeName(e.getFirstName() + " " + e.getLastName()));
        }
        return response;
    }

    private DepartmentDto.OrgNode buildOrgNode(Department dept, UUID companyId) {
        DepartmentDto.OrgNode node = new DepartmentDto.OrgNode();
        node.setId(dept.getId());
        node.setName(dept.getName());
        node.setCode(dept.getCode());
        node.setEmployeeCount(departmentRepository.countActiveEmployeesByDepartmentId(dept.getId()));

        if (dept.getHeadEmployeeId() != null) {
            employeeRepository.findById(dept.getHeadEmployeeId())
                .ifPresent(e -> node.setHeadEmployeeName(e.getFirstName() + " " + e.getLastName()));
        }

        List<Department> children = departmentRepository
            .findByParentDepartmentIdAndCompanyIdAndIsActiveTrue(dept.getId(), companyId);
        node.setChildren(children.stream().map(c -> buildOrgNode(c, companyId)).toList());
        return node;
    }

    private boolean isDescendant(UUID potentialDescendant, UUID ancestorId, UUID companyId) {
        List<Department> children = departmentRepository
            .findByParentDepartmentIdAndCompanyIdAndIsActiveTrue(ancestorId, companyId);
        for (Department child : children) {
            if (child.getId().equals(potentialDescendant)) return true;
            if (isDescendant(potentialDescendant, child.getId(), companyId)) return true;
        }
        return false;
    }

    private void validateParentExists(UUID parentId, UUID companyId) {
        departmentRepository.findByIdAndCompanyIdAndIsActiveTrue(parentId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Parent Department"));
    }

    private void validateEmployeeExists(UUID employeeId, UUID companyId) {
        employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
    }
}
