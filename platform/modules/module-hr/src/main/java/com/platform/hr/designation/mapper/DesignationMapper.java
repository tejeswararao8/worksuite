package com.platform.hr.designation.mapper;

import com.platform.hr.designation.dto.DesignationDto;
import com.platform.hr.designation.entity.Designation;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DesignationMapper {
    Designation toEntity(DesignationDto.CreateRequest request);
    DesignationDto.Response toResponse(Designation designation);
    DesignationDto.Summary toSummary(Designation designation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(DesignationDto.UpdateRequest request, @MappingTarget Designation designation);
}
