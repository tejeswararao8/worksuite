package com.hrms.branch.mapper;

import com.hrms.branch.dto.BranchDto;
import com.hrms.branch.entity.Branch;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BranchMapper {

    Branch toEntity(BranchDto.Request request);

    BranchDto.Response toResponse(Branch branch);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BranchDto.Request request, @MappingTarget Branch branch);
}
