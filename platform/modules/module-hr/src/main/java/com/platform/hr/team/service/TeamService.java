package com.platform.hr.team.service;

import com.platform.api.PagedResponse;
import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.cache.service.CacheService;
import com.platform.hr.department.repository.DepartmentRepository;
import com.platform.hr.designation.repository.DesignationRepository;
import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.team.dto.TeamDto;
import com.platform.hr.team.entity.Team;
import com.platform.hr.team.mapper.TeamMapper;
import com.platform.hr.team.repository.TeamRepository;
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
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final CacheService cacheService;

    private static final String CACHE_PREFIX = "teams:";

    @Auditable(entity = "Team", action = AuditLog.AuditAction.CREATE)
    public TeamDto.Response create(TeamDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());

        departmentRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getDepartmentId(), companyId)
            .orElseThrow(() -> PlatformException.notFound("Department"));

        if (teamRepository.existsByNameAndDepartmentIdAndCompanyId(
                request.getName(), request.getDepartmentId(), companyId)) {
            throw PlatformException.conflict("Team name already exists in this department");
        }
        if (request.getLeadEmployeeId() != null) {
            employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getLeadEmployeeId(), companyId)
                .orElseThrow(() -> PlatformException.notFound("Lead Employee"));
        }

        Team team = teamMapper.toEntity(request);
        team.setCompanyId(companyId);
        Team saved = teamRepository.save(team);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
        return enrichResponse(saved);
    }

    @Transactional(readOnly = true)
    public TeamDto.Response findById(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return enrichResponse(getActive(id, companyId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TeamDto.Response> search(String query, UUID departmentId, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return PagedResponse.of(
            teamRepository.search(companyId, query, departmentId, pageable).map(this::enrichResponse)
        );
    }

    @Transactional(readOnly = true)
    public List<TeamDto.Summary> findAll() {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return teamRepository.findByCompanyIdAndIsActiveTrue(companyId)
            .stream().map(teamMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<TeamDto.MemberResponse> getMembers(UUID teamId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        getActive(teamId, companyId);
        return employeeRepository.findByTeamIdAndCompanyIdAndIsActiveTrue(teamId, companyId)
            .stream().map(this::toMemberResponse).toList();
    }

    public void addMember(UUID teamId, UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        getActive(teamId, companyId);
        Employee employee = employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
        employee.setTeamId(teamId);
        employeeRepository.save(employee);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
    }

    public void removeMember(UUID teamId, UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        getActive(teamId, companyId);
        Employee employee = employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
        if (!teamId.equals(employee.getTeamId())) {
            throw PlatformException.badRequest("Employee is not a member of this team");
        }
        employee.setTeamId(null);
        employeeRepository.save(employee);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
    }

    @Auditable(entity = "Team", action = AuditLog.AuditAction.UPDATE)
    public TeamDto.Response update(UUID id, TeamDto.UpdateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Team team = getActive(id, companyId);
        if (request.getLeadEmployeeId() != null) {
            employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getLeadEmployeeId(), companyId)
                .orElseThrow(() -> PlatformException.notFound("Lead Employee"));
        }
        teamMapper.updateEntity(request, team);
        Team saved = teamRepository.save(team);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
        return enrichResponse(saved);
    }

    @Auditable(entity = "Team", action = AuditLog.AuditAction.DELETE)
    public void deactivate(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Team team = getActive(id, companyId);
        long count = teamRepository.countActiveEmployeesByTeamId(id);
        if (count > 0) {
            throw PlatformException.badRequest("Cannot deactivate team with " + count + " active members");
        }
        team.setIsActive(false);
        teamRepository.save(team);
        cacheService.deleteByPattern(CACHE_PREFIX + companyId + ":*");
    }

    private Team getActive(UUID id, UUID companyId) {
        return teamRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Team"));
    }

    private TeamDto.Response enrichResponse(Team team) {
        TeamDto.Response response = teamMapper.toResponse(team);
        response.setMemberCount(teamRepository.countActiveEmployeesByTeamId(team.getId()));
        departmentRepository.findById(team.getDepartmentId())
            .ifPresent(d -> response.setDepartmentName(d.getName()));
        if (team.getLeadEmployeeId() != null) {
            employeeRepository.findById(team.getLeadEmployeeId())
                .ifPresent(e -> response.setLeadEmployeeName(e.getFirstName() + " " + e.getLastName()));
        }
        return response;
    }

    private TeamDto.MemberResponse toMemberResponse(Employee e) {
        TeamDto.MemberResponse m = new TeamDto.MemberResponse();
        m.setEmployeeId(e.getId());
        m.setEmployeeCode(e.getEmployeeCode());
        m.setFirstName(e.getFirstName());
        m.setLastName(e.getLastName());
        if (e.getDesignationId() != null) {
            designationRepository.findById(e.getDesignationId())
                .ifPresent(d -> m.setDesignation(d.getTitle()));
        }
        return m;
    }
}
