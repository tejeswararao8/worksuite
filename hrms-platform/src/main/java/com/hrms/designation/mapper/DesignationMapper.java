package com.hrms.designation.mapper;

import com.hrms.designation.dto.DesignationDto;
import com.hrms.designation.entity.Designation;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DesignationMapper {
    Designation toEntity(DesignationDto.Request request);
    DesignationDto.Response toResponse(Designation designation);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(DesignationDto.Request request, @MappingTarget Designation designation);
}
