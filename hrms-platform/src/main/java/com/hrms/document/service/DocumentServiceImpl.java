package com.hrms.document.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.document.dto.DocumentDto;
import com.hrms.document.entity.EmployeeDocument;
import com.hrms.document.entity.EmployeeDocument.ExpiryStatus;
import com.hrms.document.mapper.DocumentMapper;
import com.hrms.document.repository.DocumentRepository;
import com.hrms.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final StorageService storageService;

    @Override
    public DocumentDto.Response upload(UUID employeeId, DocumentDto.Request request, MultipartFile file) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        EmployeeDocument document = documentMapper.toEntity(request);
        document.setEmployeeId(employeeId);
        document.setCompanyId(companyId);
        document.setFileName(file.getOriginalFilename());
        document.setDocumentVersion(resolveVersion(employeeId, request));
        document.setExpiryStatus(calculateExpiryStatus(request.getExpiryDate()));

        String key = buildStorageKey(companyId, employeeId, document.getDocumentType(), file);
        storageService.upload(file, key);
        document.setFileKey(key);

        EmployeeDocument saved = documentRepository.save(document);
        DocumentDto.Response response = documentMapper.toResponse(saved);
        response.setDownloadUrl(storageService.generatePresignedUrl(key));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto.Response findById(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        EmployeeDocument doc = documentRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        DocumentDto.Response response = documentMapper.toResponse(doc);
        response.setDownloadUrl(storageService.generatePresignedUrl(doc.getFileKey()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DocumentDto.Response> findByEmployee(UUID employeeId, Pageable pageable) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return new PagedResponse<>(
                documentRepository.findByEmployeeIdAndCompanyIdAndActiveTrue(employeeId, companyId, pageable)
                        .map(doc -> {
                            DocumentDto.Response r = documentMapper.toResponse(doc);
                            r.setDownloadUrl(storageService.generatePresignedUrl(doc.getFileKey()));
                            return r;
                        })
        );
    }

    @Override
    @Transactional(readOnly = true)
    public String generateDownloadUrl(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        EmployeeDocument doc = documentRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        return storageService.generatePresignedUrl(doc.getFileKey());
    }

    @Override
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        EmployeeDocument doc = documentRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        doc.setActive(false);
        documentRepository.save(doc);
    }

    private ExpiryStatus calculateExpiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) return ExpiryStatus.VALID;
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) return ExpiryStatus.EXPIRED;
        if (expiryDate.isBefore(today.plusDays(90))) return ExpiryStatus.EXPIRING_SOON;
        return ExpiryStatus.VALID;
    }

    private int resolveVersion(UUID employeeId, DocumentDto.Request request) {
        return documentRepository.findByEmployeeIdAndDocumentTypeAndActiveTrue(employeeId, request.getDocumentType())
                .size() + 1;
    }

    private String buildStorageKey(UUID companyId, UUID employeeId,
                                    EmployeeDocument.DocumentType type, MultipartFile file) {
        return String.format("documents/%s/%s/%s/%s", companyId, employeeId, type.name().toLowerCase(),
                UUID.randomUUID() + "_" + file.getOriginalFilename());
    }
}
