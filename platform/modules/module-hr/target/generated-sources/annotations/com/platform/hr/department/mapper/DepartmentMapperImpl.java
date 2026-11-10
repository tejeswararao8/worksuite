package com.platform.hr.department.mapper;

import com.platform.hr.department.dto.DepartmentDto;
import com.platform.hr.department.entity.Department;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-09T16:52:01+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class DepartmentMapperImpl implements DepartmentMapper {

    @Override
    public Department toEntity(DepartmentDto.CreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Department.DepartmentBuilder department = Department.builder();

        department.name( request.getName() );
        department.code( request.getCode() );
        department.description( request.getDescription() );
        department.parentDepartmentId( request.getParentDepartmentId() );
        department.headEmployeeId( request.getHeadEmployeeId() );

        return department.build();
    }

    @Override
    public DepartmentDto.Response toResponse(Department department) {
        if ( department == null ) {
            return null;
        }

        DepartmentDto.Response response = new DepartmentDto.Response();

        response.setId( department.getId() );
        response.setName( department.getName() );
        response.setCode( department.getCode() );
        response.setDescription( department.getDescription() );
        response.setParentDepartmentId( department.getParentDepartmentId() );
        response.setHeadEmployeeId( department.getHeadEmployeeId() );
        response.setCreatedAt( department.getCreatedAt() );

        return response;
    }

    @Override
    public DepartmentDto.Summary toSummary(Department department) {
        if ( department == null ) {
            return null;
        }

        DepartmentDto.Summary summary = new DepartmentDto.Summary();

        summary.setId( department.getId() );
        summary.setName( department.getName() );
        summary.setCode( department.getCode() );
        summary.setParentDepartmentId( department.getParentDepartmentId() );

        return summary;
    }

    @Override
    public void updateEntity(DepartmentDto.UpdateRequest request, Department department) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            department.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            department.setDescription( request.getDescription() );
        }
        if ( request.getParentDepartmentId() != null ) {
            department.setParentDepartmentId( request.getParentDepartmentId() );
        }
        if ( request.getHeadEmployeeId() != null ) {
            department.setHeadEmployeeId( request.getHeadEmployeeId() );
        }
    }
}
