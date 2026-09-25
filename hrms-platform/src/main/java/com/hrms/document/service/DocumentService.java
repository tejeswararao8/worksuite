package com.hrms.document.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.document.dto.DocumentDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface DocumentService {
    DocumentDto.Response upload(UUID employeeId, DocumentDto.Request request, MultipartFile file);
    DocumentDto.Response findById(UUID id);
    PagedResponse<DocumentDto.Response> findByEmployee(UUID employeeId, Pageable pageable);
    String generateDownloadUrl(UUID id);
    void delete(UUID id);
}
