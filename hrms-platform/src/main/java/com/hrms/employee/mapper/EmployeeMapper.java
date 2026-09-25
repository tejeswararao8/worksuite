package com.hrms.employee.mapper;

import com.hrms.employee.dto.EmployeeDto;
import com.hrms.employee.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {

    Employee toEntity(EmployeeDto.CreateRequest request);

    EmployeeDto.Response toResponse(Employee employee);

    EmployeeDto.Summary toSummary(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(EmployeeDto.UpdateRequest request, @MappingTarget Employee employee);
}
