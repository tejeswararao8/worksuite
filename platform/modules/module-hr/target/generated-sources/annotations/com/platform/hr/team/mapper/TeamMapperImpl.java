package com.platform.hr.team.mapper;

import com.platform.hr.team.dto.TeamDto;
import com.platform.hr.team.entity.Team;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-10T17:13:18+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class TeamMapperImpl implements TeamMapper {

    @Override
    public Team toEntity(TeamDto.CreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Team.TeamBuilder team = Team.builder();

        team.name( request.getName() );
        team.description( request.getDescription() );
        team.departmentId( request.getDepartmentId() );
        team.leadEmployeeId( request.getLeadEmployeeId() );

        return team.build();
    }

    @Override
    public TeamDto.Response toResponse(Team team) {
        if ( team == null ) {
            return null;
        }

        TeamDto.Response response = new TeamDto.Response();

        response.setId( team.getId() );
        response.setName( team.getName() );
        response.setDescription( team.getDescription() );
        response.setDepartmentId( team.getDepartmentId() );
        response.setLeadEmployeeId( team.getLeadEmployeeId() );
        response.setCreatedAt( team.getCreatedAt() );

        return response;
    }

    @Override
    public TeamDto.Summary toSummary(Team team) {
        if ( team == null ) {
            return null;
        }

        TeamDto.Summary summary = new TeamDto.Summary();

        summary.setId( team.getId() );
        summary.setName( team.getName() );
        summary.setDepartmentId( team.getDepartmentId() );

        return summary;
    }

    @Override
    public void updateEntity(TeamDto.UpdateRequest request, Team team) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            team.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            team.setDescription( request.getDescription() );
        }
        if ( request.getDepartmentId() != null ) {
            team.setDepartmentId( request.getDepartmentId() );
        }
        if ( request.getLeadEmployeeId() != null ) {
            team.setLeadEmployeeId( request.getLeadEmployeeId() );
        }
    }
}
