package com.hrms.document.dto;

import com.hrms.document.entity.EmployeeDocument.DocumentType;
import com.hrms.document.entity.EmployeeDocument.ExpiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentDto {

    @Data
    public static class Request {
        @NotNull
        private DocumentType documentType;
        @NotNull
        private String documentName;
        private String documentNumber;
        private String issuingCountry;
        private String issuingAuthority;
        private LocalDate issueDate;
        private LocalDate expiryDate;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private DocumentType documentType;
        private String documentName;
        private String documentNumber;
        private String issuingCountry;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String fileName;
        private Integer documentVersion;
        private ExpiryStatus expiryStatus;
        private String downloadUrl;
        private LocalDateTime createdAt;
    }
}
