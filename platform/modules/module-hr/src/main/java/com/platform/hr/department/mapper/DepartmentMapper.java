package com.platform.hr.department.mapper;

import com.platform.hr.department.dto.DepartmentDto;
import com.platform.hr.department.entity.Department;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DepartmentMapper {
    Department toEntity(DepartmentDto.CreateRequest request);
    DepartmentDto.Response toResponse(Department department);
    DepartmentDto.Summary toSummary(Department department);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(DepartmentDto.UpdateRequest request, @MappingTarget Department department);
}
