package com.hrms.employee.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.employee.dto.EmployeeDto;
import com.hrms.employee.entity.Employee.EmploymentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface EmployeeService {

    EmployeeDto.Response create(EmployeeDto.CreateRequest request);

    EmployeeDto.Response findById(UUID id);

    PagedResponse<EmployeeDto.Response> search(String query, UUID departmentId,
                                                UUID branchId, EmploymentStatus status, Pageable pageable);

    EmployeeDto.Response update(UUID id, EmployeeDto.UpdateRequest request);

    EmployeeDto.Response uploadPhoto(UUID id, MultipartFile file);

    void delete(UUID id);
}
