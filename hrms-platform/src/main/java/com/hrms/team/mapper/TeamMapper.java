package com.hrms.team.mapper;

import com.hrms.team.dto.TeamDto;
import com.hrms.team.entity.Team;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TeamMapper {
    Team toEntity(TeamDto.Request request);
    TeamDto.Response toResponse(Team team);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TeamDto.Request request, @MappingTarget Team team);
}
