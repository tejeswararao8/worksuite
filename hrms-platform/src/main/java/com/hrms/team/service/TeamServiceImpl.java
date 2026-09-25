package com.hrms.team.service;

import com.hrms.audit.entity.AuditLog.AuditAction;
import com.hrms.audit.service.AuditService;
import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.team.dto.TeamDto;
import com.hrms.team.entity.Team;
import com.hrms.team.mapper.TeamMapper;
import com.hrms.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final AuditService auditService;

    @Override
    public TeamDto.Response create(TeamDto.Request request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Team team = teamMapper.toEntity(request);
        team.setCompanyId(companyId);
        Team saved = teamRepository.save(team);
        auditService.log(companyId, "Team", saved.getId().toString(),
                AuditAction.CREATE, null, saved, SecurityUtils.getCurrentUsername(), null);
        return teamMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamDto.Response findById(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return teamRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .map(teamMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TeamDto.Response> findAll(Pageable pageable) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return new PagedResponse<>(teamRepository.findByCompanyIdAndActiveTrue(companyId, pageable)
                .map(teamMapper::toResponse));
    }

    @Override
    public TeamDto.Response update(UUID id, TeamDto.Request request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Team team = teamRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
        teamMapper.updateEntity(request, team);
        return teamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Team team = teamRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
        team.setActive(false);
        teamRepository.save(team);
        auditService.log(companyId, "Team", id.toString(),
                AuditAction.DELETE, null, null, SecurityUtils.getCurrentUsername(), null);
    }
}
