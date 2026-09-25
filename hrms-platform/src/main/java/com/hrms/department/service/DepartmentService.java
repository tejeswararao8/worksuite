package com.hrms.department.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.department.dto.DepartmentDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DepartmentService {
    DepartmentDto.Response create(DepartmentDto.Request request);
    DepartmentDto.Response findById(UUID id);
    PagedResponse<DepartmentDto.Response> findAll(Pageable pageable);
    DepartmentDto.Response update(UUID id, DepartmentDto.Request request);
    void delete(UUID id);
}
