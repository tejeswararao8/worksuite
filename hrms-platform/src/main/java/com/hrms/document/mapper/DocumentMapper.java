package com.hrms.document.mapper;

import com.hrms.document.dto.DocumentDto;
import com.hrms.document.entity.EmployeeDocument;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DocumentMapper {
    EmployeeDocument toEntity(DocumentDto.Request request);
    DocumentDto.Response toResponse(EmployeeDocument document);
}
