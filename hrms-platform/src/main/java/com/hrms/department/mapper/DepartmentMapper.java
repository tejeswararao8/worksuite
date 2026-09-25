package com.hrms.department.mapper;

import com.hrms.department.dto.DepartmentDto;
import com.hrms.department.entity.Department;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DepartmentMapper {
    Department toEntity(DepartmentDto.Request request);
    DepartmentDto.Response toResponse(Department department);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(DepartmentDto.Request request, @MappingTarget Department department);
}
