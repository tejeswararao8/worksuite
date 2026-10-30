package com.platform.hr.branch.mapper;

import com.platform.hr.branch.dto.BranchDto;
import com.platform.hr.branch.entity.Branch;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BranchMapper {
    Branch toEntity(BranchDto.CreateRequest request);
    BranchDto.Response toResponse(Branch branch);
    BranchDto.Summary toSummary(Branch branch);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BranchDto.UpdateRequest request, @MappingTarget Branch branch);
}
