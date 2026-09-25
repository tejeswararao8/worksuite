package com.hrms.asset.mapper;

import com.hrms.asset.dto.AssetDto;
import com.hrms.asset.entity.Asset;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssetMapper {
    Asset toEntity(AssetDto.Request request);
    AssetDto.Response toResponse(Asset asset);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AssetDto.Request request, @MappingTarget Asset asset);
}
