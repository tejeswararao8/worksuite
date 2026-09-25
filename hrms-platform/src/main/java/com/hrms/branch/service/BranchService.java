package com.hrms.branch.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.branch.dto.BranchDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BranchService {
    BranchDto.Response create(BranchDto.Request request);
    BranchDto.Response findById(UUID id);
    PagedResponse<BranchDto.Response> findAll(Pageable pageable);
    BranchDto.Response update(UUID id, BranchDto.Request request);
    void delete(UUID id);
}
