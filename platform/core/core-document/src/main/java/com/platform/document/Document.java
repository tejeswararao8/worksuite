package com.platform.document;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends BaseEntity {

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "entity_type", nullable = false)
    private String entityType; // e.g. EMPLOYEE, ASSET

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issuing_authority")
    private String issuingAuthority;

    @Enumerated(EnumType.STRING)
    @Column(name = "expiry_status")
    private ExpiryStatus expiryStatus = ExpiryStatus.VALID;

    public enum DocumentType {
        PASSPORT, VISA, NATIONAL_ID, DRIVING_LICENSE,
        CERTIFICATE, CONTRACT, OTHER
    }

    public enum ExpiryStatus {
        VALID, EXPIRING_SOON, EXPIRED
    }
}
