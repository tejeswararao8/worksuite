package com.platform.hr.employee.mapper;

import com.platform.hr.employee.dto.EmployeeDto;
import com.platform.hr.employee.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {
    Employee toEntity(EmployeeDto.CreateRequest request);
    EmployeeDto.Response toResponse(Employee employee);
    EmployeeDto.Summary toSummary(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(EmployeeDto.UpdateRequest request, @MappingTarget Employee employee);
}
