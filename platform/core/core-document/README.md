# core-document

**Local filesystem file storage and document expiry tracking with automated reminders.**

## What It Does

- `LocalStorageService` — store, load, delete files on local filesystem (no AWS)
- `Document` entity — tracks file metadata, document type, issue/expiry dates, expiry status
- `DocumentExpiryScheduler` — daily cron job that sends reminders at 90/60/30/7 days before expiry

## Package

```
com.platform.document
```

## How to Use in Your Module

### Upload a File

```java
@Service
@RequiredArgsConstructor
public class EmployeeDocumentService {

    private final LocalStorageService storageService;
    private final DocumentRepository documentRepository;

    public Document uploadDocument(UUID employeeId, MultipartFile file,
                                   Document.DocumentType type, LocalDate expiryDate) throws IOException {

        String tenantId = TenantContext.getTenantId();

        // Store file — returns relative path
        String filePath = storageService.store(file, tenantId, "EMPLOYEE", employeeId.toString());

        // Save metadata
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

### Download a File

```java
@GetMapping("/{id}/download")
public ResponseEntity<byte[]> download(@PathVariable UUID id) throws IOException {
    Document doc = documentRepository.findById(id)
        .orElseThrow(() -> PlatformException.notFound("Document"));

    byte[] content = storageService.load(doc.getFilePath());

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(doc.getContentType()))
        .body(content);
}
```

### Delete a File

```java
storageService.delete(doc.getFilePath());
documentRepository.delete(doc);
```

## File Storage Path Structure

```
{base-path}/{tenantId}/{entityType}/{entityId}/{uuid}_{originalFilename}

Example:
./uploads/
  00000000-0000-0000-0000-000000000001/   ← tenantId
    EMPLOYEE/
      abc123-uuid/                         ← employeeId
        f7e8d9c0_passport.pdf
        a1b2c3d4_visa.jpg
```

## Document Expiry Scheduler

Runs daily at 08:00. For each document with an expiry date:

| Days Until Expiry | Action |
|---|---|
| 90 days | Status → EXPIRING_SOON, send notification |
| 60 days | Send reminder notification |
| 30 days | Send reminder notification |
| 7 days | Send urgent reminder notification |
| Past expiry | Status → EXPIRED |

Notifications are sent via `NotificationService` to the document owner.

## ExpiryStatus Values

```java
Document.ExpiryStatus.VALID          // default
Document.ExpiryStatus.EXPIRING_SOON  // within 90 days
Document.ExpiryStatus.EXPIRED        // past expiry date
```

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

## Database Table

```sql
CREATE TABLE documents (
    id                CHAR(36) NOT NULL PRIMARY KEY,
    company_id        CHAR(36) NOT NULL,
    entity_id         CHAR(36) NOT NULL,
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
    expiry_status     VARCHAR(50) DEFAULT 'VALID',
    created_at        DATETIME NOT NULL
);
```

## Configuration

```yaml
platform:
  storage:
    local:
      base-path: ./uploads   # relative to working directory
```

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-notification</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
```
