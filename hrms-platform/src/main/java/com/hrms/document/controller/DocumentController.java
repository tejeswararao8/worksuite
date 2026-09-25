package com.hrms.document.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.document.dto.DocumentDto;
import com.hrms.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/employees/{employeeId}/documents")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Document Management", description = "Upload and manage employee documents. " +
        "Supports all document types: Resume, Offer Letter, Passport, Visa, Emirates ID, Aadhaar, PAN, Driving License, Certificates, etc. " +
        "Files are stored securely in AWS S3. Download URLs are pre-signed and expire after 60 minutes. " +
        "Document expiry is automatically tracked with reminders at 90, 60, 30, and 7 days before expiry.")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(
            summary = "Upload employee document",
            description = "Uploads a document file for an employee and stores metadata. " +
                    "Supported document types: RESUME, OFFER_LETTER, EXPERIENCE_LETTER, EDUCATION_CERTIFICATE, " +
                    "TRAINING_CERTIFICATE, PASSPORT, VISA, EMIRATES_ID, AADHAAR, PAN, DRIVING_LICENSE, OTHER. " +
                    "For identity documents (Passport, Visa, Emirates ID), provide `expiryDate` to enable expiry tracking. " +
                    "Version is auto-incremented if the same document type is uploaded again. " +
                    "Max file size: 10MB. Accepted formats: PDF, JPEG, PNG, DOCX."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document uploaded successfully — returns metadata and pre-signed download URL"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Validation error — documentType or documentName missing, or file too large"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<DocumentDto.Response>> upload(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Document metadata as form fields",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            examples = @ExampleObject(value = """
                                    documentType: PASSPORT
                                    documentName: John Doe Passport
                                    documentNumber: A1234567
                                    issuingCountry: India
                                    issuingAuthority: Government of India
                                    issueDate: 2020-01-15
                                    expiryDate: 2030-01-14
                                    file: <binary>
                                    """)
                    )
            )
            @Valid @ModelAttribute DocumentDto.Request request,
            @Parameter(description = "Document file (PDF/JPEG/PNG/DOCX, max 10MB)", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded", documentService.upload(employeeId, request, file)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "List employee documents",
            description = "Returns a paginated list of all active documents for the employee, sorted by upload date descending. " +
                    "Each document includes a pre-signed S3 download URL valid for 60 minutes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of documents with download URLs"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<PagedResponse<DocumentDto.Response>>> findAll(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                documentService.findByEmployee(employeeId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Get document by ID",
            description = "Returns metadata and a fresh pre-signed download URL for a specific document. " +
                    "The pre-signed URL is valid for 60 minutes from the time of this request."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document metadata and download URL returned"),
            @ApiResponse(responseCode = "404", description = "Document not found or belongs to a different company"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<DocumentDto.Response>> findById(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @Parameter(description = "Document UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.findById(id)));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Generate document download URL",
            description = "Generates a fresh pre-signed AWS S3 URL to directly download the document file. " +
                    "The URL is valid for 60 minutes. Use this URL in a browser or HTTP client to download the file. " +
                    "This endpoint does not stream the file — it returns the URL only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pre-signed download URL returned"),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<String>> download(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @Parameter(description = "Document UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.generateDownloadUrl(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Delete document",
            description = "Soft-deletes a document record. The file in S3 is retained for compliance purposes. " +
                    "Deleted documents no longer appear in listings or expiry tracking."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @Parameter(description = "Document UUID", required = true) @PathVariable UUID id) {
        documentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}
