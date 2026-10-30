package com.platform.hr.team.mapper;

import com.platform.hr.team.dto.TeamDto;
import com.platform.hr.team.entity.Team;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TeamMapper {
    Team toEntity(TeamDto.CreateRequest request);
    TeamDto.Response toResponse(Team team);
    TeamDto.Summary toSummary(Team team);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TeamDto.UpdateRequest request, @MappingTarget Team team);
}
