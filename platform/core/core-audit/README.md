# core-audit

**Automatic, immutable audit trail for every write operation across all modules.**

## What It Does

- `@Auditable` annotation — put it on any service method to auto-log it
- `AuditAspect` — AOP intercepts `@Auditable` methods, captures result, logs asynchronously
- `AuditService` — saves `AuditLog` records, provides tenant-scoped query methods
- `AuditLog` entity — stores entity name, entity ID, action, old/new values, performer, IP address

## Package

```
com.platform.audit
```

## Files

| File | Purpose |
|---|---|
| `Auditable.java` | Annotation to mark methods for auto-audit |
| `AuditAspect.java` | AOP around advice that intercepts `@Auditable` methods |
| `AuditService.java` | Async save + query methods |
| `AuditLog.java` | JPA entity for audit records |
| `AuditLogRepository.java` | JPA repository with tenant-scoped queries |

---

## How to Use — Annotate Service Methods

```java
@Service
@RequiredArgsConstructor
public class EmployeeService {

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.CREATE)
    public EmployeeDto create(CreateEmployeeRequest req) {
        // ... save employee ...
        return savedDto;
        // AuditAspect automatically logs after this method returns successfully
    }

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.UPDATE)
    public EmployeeDto update(UUID id, UpdateEmployeeRequest req) {
        // ...
    }

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.DELETE)
    public void delete(UUID id) {
        // ...
    }
}
```

That's it. No other code needed.

---

## What Gets Logged Automatically

| Field | Source |
|---|---|
| `entityName` | From `@Auditable(entity = "Employee")` |
| `entityId` | Extracted from return value via `getId()` reflection |
| `action` | From `@Auditable(action = CREATE)` |
| `newValue` | JSON serialization of the return value |
| `performedBy` | `UserPrincipal.currentUserId()` |
| `ipAddress` | From `X-Forwarded-For` header or `request.getRemoteAddr()` |
| `companyId` | `UserPrincipal.currentCompanyId()` |

---

## Manual Logging (when you need more control)

```java
@Service
@RequiredArgsConstructor
public class SomeService {

    private final AuditService auditService;

    public void doSomething(UUID entityId) {
        // Simple log — no old/new values
        auditService.log("Employee", entityId.toString(), AuditLog.AuditAction.UPDATE);

        // Full log with old/new values and IP
        auditService.log("Employee", entityId.toString(),
            AuditLog.AuditAction.UPDATE,
            oldValueJson, newValueJson, ipAddress);
    }
}
```

---

## Query Audit Logs

```java
// All logs for current tenant (paginated, newest first)
Page<AuditLog> logs = auditService.findByTenant(pageable);

// Logs for a specific entity type
Page<AuditLog> empLogs = auditService.findByEntity("Employee", pageable);

// Logs for a specific record ID
Page<AuditLog> recordLogs = auditService.findByEntityId(employeeId.toString(), pageable);
```

---

## AuditAction Values

```java
AuditLog.AuditAction.CREATE
AuditLog.AuditAction.UPDATE
AuditLog.AuditAction.DELETE
AuditLog.AuditAction.LOGIN
AuditLog.AuditAction.LOGOUT
AuditLog.AuditAction.EXPORT
```

---

## Important Behaviour

- Audit logging is `@Async` — never slows down your request
- Only fires if the method **returns successfully** — exceptions are not logged
- `entityId` extraction uses reflection on `getId()` — works for any entity or DTO with a `getId()` method
- Audit logs are **never updated or deleted** — append-only table

---

## Database Table

```sql
CREATE TABLE audit_logs (
    id           UUID         NOT NULL PRIMARY KEY,
    company_id   UUID         NOT NULL,
    entity_name  VARCHAR(100) NOT NULL,
    entity_id    VARCHAR(100) NOT NULL,
    action       VARCHAR(50)  NOT NULL,
    old_value    TEXT,
    new_value    TEXT,
    performed_by VARCHAR(255),
    ip_address   VARCHAR(100),
    description  VARCHAR(500),
    created_at   TIMESTAMP    NOT NULL,
    version      BIGINT       NOT NULL DEFAULT 0,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE
);
```

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-aop</artifactId></dependency>
```
