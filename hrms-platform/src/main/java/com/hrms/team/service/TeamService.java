package com.hrms.team.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.team.dto.TeamDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TeamService {
    TeamDto.Response create(TeamDto.Request request);
    TeamDto.Response findById(UUID id);
    PagedResponse<TeamDto.Response> findAll(Pageable pageable);
    TeamDto.Response update(UUID id, TeamDto.Request request);
    void delete(UUID id);
}
