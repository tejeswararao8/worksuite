package com.hrms.company.mapper;

import com.hrms.company.dto.CompanyDto;
import com.hrms.company.entity.Company;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {

    Company toEntity(CompanyDto.Request request);

    CompanyDto.Response toResponse(Company company);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CompanyDto.Request request, @MappingTarget Company company);
}
