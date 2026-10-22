# core-audit

**Automatic, immutable audit trail for every write operation across all modules.**

## What It Does

- `@Auditable` annotation — put it on any service method to auto-log it
- `AuditAspect` — AOP intercepts `@Auditable` methods, extracts result, logs asynchronously
- `AuditService` — saves `AuditLog` records, provides query methods
- `AuditLog` entity — stores entity name, entity ID, action, old/new values, performer, IP

## Package

```
com.platform.audit
```

## How to Use in Your Module

### 1. Annotate service methods

```java
@Service
@RequiredArgsConstructor
public class EmployeeService {

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.CREATE)
    public EmployeeDto create(EmployeeDto dto) {
        // ... save employee ...
        return savedDto;
        // AuditAspect automatically logs this after the method returns
    }

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.UPDATE)
    public EmployeeDto update(UUID id, EmployeeDto dto) {
        // ...
    }

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.DELETE)
    public void delete(UUID id) {
        // ...
    }
}
```

That's it. No other code needed. The aspect handles everything.

### 2. What gets logged automatically

| Field | Source |
|---|---|
| `entityName` | From `@Auditable(entity = "Employee")` |
| `entityId` | Extracted from return value via `getId()` reflection |
| `action` | From `@Auditable(action = CREATE)` |
| `newValue` | JSON serialization of the return value |
| `performedBy` | `UserPrincipal.currentUserId()` |
| `ipAddress` | From `X-Forwarded-For` header or `request.getRemoteAddr()` |
| `companyId` | `UserPrincipal.currentCompanyId()` |

### 3. Manual logging (when you need more control)

```java
@Service
@RequiredArgsConstructor
public class SomeService {

    private final AuditService auditService;

    public void doSomething(UUID entityId) {
        // Simple log
        auditService.log("Employee", entityId.toString(), AuditLog.AuditAction.UPDATE);

        // Full log with old/new values
        auditService.log("Employee", entityId.toString(),
            AuditLog.AuditAction.UPDATE,
            oldValueJson, newValueJson, ipAddress);
    }
}
```

### 4. Query audit logs

```java
// All logs for current tenant (paginated)
Page<AuditLog> logs = auditService.findByTenant(pageable);

// Logs for a specific entity type
Page<AuditLog> empLogs = auditService.findByEntity("Employee", pageable);

// Logs for a specific record
Page<AuditLog> recordLogs = auditService.findByEntityId(employeeId.toString(), pageable);
```

## AuditAction Values

```java
AuditLog.AuditAction.CREATE
AuditLog.AuditAction.UPDATE
AuditLog.AuditAction.DELETE
AuditLog.AuditAction.LOGIN
AuditLog.AuditAction.LOGOUT
AuditLog.AuditAction.EXPORT
```

## Important Notes

- Audit logging is **async** (`@Async`) — it never slows down your request
- Audit logs are **immutable** — no update/delete operations on `audit_logs` table
- The aspect only fires if the method **returns successfully** — exceptions are not logged
- `entityId` extraction uses reflection on `getId()` — works for any entity/DTO with a `getId()` method

## Database Table

```sql
CREATE TABLE audit_logs (
    id           CHAR(36) NOT NULL PRIMARY KEY,
    company_id   CHAR(36) NOT NULL,
    entity_name  VARCHAR(100) NOT NULL,
    entity_id    VARCHAR(100) NOT NULL,
    action       VARCHAR(50) NOT NULL,
    old_value    TEXT,
    new_value    TEXT,
    performed_by VARCHAR(255),
    ip_address   VARCHAR(100),
    created_at   DATETIME NOT NULL
);
```

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-aop</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
```
