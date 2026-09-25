package com.hrms.company.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.company.dto.CompanyDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyService {

    CompanyDto.Response create(CompanyDto.Request request);

    CompanyDto.Response findById(UUID id);

    PagedResponse<CompanyDto.Response> findAll(Pageable pageable);

    CompanyDto.Response update(UUID id, CompanyDto.Request request);

    void delete(UUID id);
}
