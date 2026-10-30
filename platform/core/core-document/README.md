# core-document

**MinIO file storage and document expiry tracking with automated reminders.**

## What It Does

- `MinioStorageService` — store, load, get presigned URL, and delete files in MinIO (S3-compatible)
- `Document` entity — tracks file metadata, document type, issue/expiry dates, expiry status
- `DocumentExpiryScheduler` — daily cron job at 08:00 that sends reminders at 90/60/30/7 days before expiry and marks expired documents

## Package

```
com.platform.document
```

## Files

| File | Purpose |
|---|---|
| `Document.java` | JPA entity for document metadata |
| `MinioStorageService.java` | MinIO file operations (store, load, presigned URL, delete) |
| `DocumentExpiryScheduler.java` | Daily cron job for expiry reminders |
| `DocumentRepository.java` | JPA repository with expiry queries |

---

## How to Use — Upload a File

```java
@Service
@RequiredArgsConstructor
public class EmployeeDocumentService {

    private final MinioStorageService storageService;
    private final DocumentRepository documentRepository;

    public Document upload(UUID employeeId, MultipartFile file,
                           Document.DocumentType type, LocalDate expiryDate) throws Exception {

        String tenantId = TenantContext.getTenantId();

        // Store file in MinIO — returns object path
        String filePath = storageService.store(file, tenantId, "EMPLOYEE", employeeId.toString());

        // Save metadata in PostgreSQL
        Document doc = Document.builder()
            .entityId(employeeId)
            .entityType("EMPLOYEE")
            .fileName(file.getOriginalFilename())
            .filePath(filePath)
            .fileSize(file.getSize())
            .contentType(file.getContentType())
            .documentType(type)
            .expiryDate(expiryDate)
            .expiryStatus(Document.ExpiryStatus.VALID)
            .build();
        // companyId set automatically from TenantContext

        return documentRepository.save(doc);
    }
}
```

---

## Download a File

```java
@GetMapping("/{id}/download")
public ResponseEntity<byte[]> download(@PathVariable UUID id) throws Exception {
    Document doc = documentRepository.findById(id)
        .orElseThrow(() -> PlatformException.notFound("Document"));

    byte[] content = storageService.load(doc.getFilePath());

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + doc.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(doc.getContentType()))
        .body(content);
}
```

---

## Presigned URL (temporary download link)

```java
// Returns a URL valid for 1 hour — use for frontend direct download
String url = storageService.getPresignedUrl(doc.getFilePath());
```

---

## Delete a File

```java
storageService.delete(doc.getFilePath());
documentRepository.delete(doc);
```

---

## MinIO File Path Structure

```
{tenantId}/{entityType}/{entityId}/{uuid}_{originalFilename}

Example:
  00000000-0000-0000-0000-000000000001/EMPLOYEE/abc123-uuid/f7e8d9c0_passport.pdf
  00000000-0000-0000-0000-000000000001/EMPLOYEE/abc123-uuid/a1b2c3d4_visa.jpg
```

All files are stored in the bucket configured by `MINIO_BUCKET` (default: `vve-documents`).

---

## Document Expiry Scheduler

Runs daily at 08:00 (`@Scheduled(cron = "0 0 8 * * *")`).

| Days Until Expiry | Action |
|---|---|
| 90 days | Status → EXPIRING_SOON, send notification |
| 60 days | Send reminder notification |
| 30 days | Send reminder notification |
| 7 days | Send urgent reminder notification |
| Past expiry | Status → EXPIRED |

Notifications are sent via `NotificationService` with type `DOCUMENT_EXPIRY`.

---

## ExpiryStatus Values

```java
Document.ExpiryStatus.VALID           // default — not expiring soon
Document.ExpiryStatus.EXPIRING_SOON   // within 90 days of expiry
Document.ExpiryStatus.EXPIRED         // past expiry date
```

---

## DocumentType Values

```java
Document.DocumentType.PASSPORT
Document.DocumentType.VISA
Document.DocumentType.NATIONAL_ID
Document.DocumentType.DRIVING_LICENSE
Document.DocumentType.CERTIFICATE
Document.DocumentType.CONTRACT
Document.DocumentType.OTHER
```

---

## Database Table

```sql
CREATE TABLE documents (
    id                UUID         NOT NULL PRIMARY KEY,
    company_id        UUID         NOT NULL,
    entity_id         UUID         NOT NULL,
    entity_type       VARCHAR(100) NOT NULL,
    file_name         VARCHAR(255) NOT NULL,
    file_path         VARCHAR(500) NOT NULL,
    file_size         BIGINT,
    content_type      VARCHAR(100),
    document_type     VARCHAR(100),
    document_number   VARCHAR(100),
    issue_date        DATE,
    expiry_date       DATE,
    issuing_authority VARCHAR(255),
    expiry_status     VARCHAR(50)  NOT NULL DEFAULT 'VALID',
    created_at        TIMESTAMP    NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 0,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE
);
```

---

## Configuration

```yaml
platform:
  storage:
    minio:
      endpoint:   ${MINIO_ENDPOINT:http://localhost:9000}
      access-key: ${MINIO_ACCESS_KEY:admin}
      secret-key: ${MINIO_SECRET_KEY:admin123456}
      bucket:     ${MINIO_BUCKET:vve-documents}
```

MinIO runs on port 9000 (API) and 9001 (dashboard). Start with:
```bash
docker compose up -d minio
```

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-notification</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
<!-- MinIO Java SDK must be added to pom.xml -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.9</version>
</dependency>
```
