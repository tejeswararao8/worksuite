package com.hrms.designation.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.designation.dto.DesignationDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DesignationService {
    DesignationDto.Response create(DesignationDto.Request request);
    DesignationDto.Response findById(UUID id);
    PagedResponse<DesignationDto.Response> findAll(Pageable pageable);
    DesignationDto.Response update(UUID id, DesignationDto.Request request);
    void delete(UUID id);
}
