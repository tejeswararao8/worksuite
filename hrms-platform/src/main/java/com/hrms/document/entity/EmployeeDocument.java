package com.hrms.document.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDocument extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "issuing_country")
    private String issuingCountry;

    @Column(name = "issuing_authority")
    private String issuingAuthority;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "file_key")
    private String fileKey;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "version")
    private Integer documentVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "expiry_status")
    private ExpiryStatus expiryStatus;

    public enum DocumentType {
        RESUME, OFFER_LETTER, EXPERIENCE_LETTER, EDUCATION_CERTIFICATE,
        TRAINING_CERTIFICATE, PASSPORT, VISA, EMIRATES_ID, AADHAAR,
        PAN, DRIVING_LICENSE, OTHER
    }

    public enum ExpiryStatus {
        VALID, EXPIRING_SOON, EXPIRED
    }
}
