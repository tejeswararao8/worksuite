package com.platform.hr.designation.mapper;

import com.platform.hr.designation.dto.DesignationDto;
import com.platform.hr.designation.entity.Designation;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-11T09:49:04+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class DesignationMapperImpl implements DesignationMapper {

    @Override
    public Designation toEntity(DesignationDto.CreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Designation.DesignationBuilder designation = Designation.builder();

        designation.title( request.getTitle() );
        designation.description( request.getDescription() );
        designation.level( request.getLevel() );
        designation.grade( request.getGrade() );

        return designation.build();
    }

    @Override
    public DesignationDto.Response toResponse(Designation designation) {
        if ( designation == null ) {
            return null;
        }

        DesignationDto.Response response = new DesignationDto.Response();

        response.setId( designation.getId() );
        response.setTitle( designation.getTitle() );
        response.setDescription( designation.getDescription() );
        response.setLevel( designation.getLevel() );
        response.setGrade( designation.getGrade() );
        response.setCreatedAt( designation.getCreatedAt() );

        return response;
    }

    @Override
    public DesignationDto.Summary toSummary(Designation designation) {
        if ( designation == null ) {
            return null;
        }

        DesignationDto.Summary summary = new DesignationDto.Summary();

        summary.setId( designation.getId() );
        summary.setTitle( designation.getTitle() );
        summary.setLevel( designation.getLevel() );
        summary.setGrade( designation.getGrade() );

        return summary;
    }

    @Override
    public void updateEntity(DesignationDto.UpdateRequest request, Designation designation) {
        if ( request == null ) {
            return;
        }

        if ( request.getTitle() != null ) {
            designation.setTitle( request.getTitle() );
        }
        if ( request.getDescription() != null ) {
            designation.setDescription( request.getDescription() );
        }
        if ( request.getLevel() != null ) {
            designation.setLevel( request.getLevel() );
        }
        if ( request.getGrade() != null ) {
            designation.setGrade( request.getGrade() );
        }
    }
}
