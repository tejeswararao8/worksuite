# core-tenancy

**The foundation of the entire platform. Every other module depends on this.**

## What It Does

- Provides `BaseEntity` — the superclass every entity in every module must extend
- Provides `TenantContext` — ThreadLocal that holds the current tenant (companyId) for the duration of a request
- Provides `TenantInterceptor` — reads tenantId from the JWT-injected request attribute and loads it into TenantContext
- Provides `Tenant` entity — the registry of all companies using the platform
- Provides `TenantService` — create/suspend/lookup tenants

## Package

```
com.platform.tenancy
```

## Key Classes

### BaseEntity

Every entity in every module extends this. You get for free:

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Auto-generated primary key |
| `companyId` | UUID | Tenant isolation — set automatically from TenantContext |
| `createdBy` | String | Set by Spring Data Auditing from SecurityContext |
| `createdAt` | LocalDateTime | Set by Spring Data Auditing |
| `updatedBy` | String | Set by Spring Data Auditing |
| `updatedAt` | LocalDateTime | Set by Spring Data Auditing |
| `version` | Long | Optimistic locking — prevents lost updates |
| `isActive` | Boolean | Soft delete flag — default true |

The `@PrePersist` hook auto-sets `companyId` from `TenantContext` so you never set it manually.

```java
// Your entity — nothing else needed
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {
    private String name;
    // companyId, id, audit fields — all inherited
}
```

### TenantContext

```java
// Set by TenantInterceptor — you never call this directly
TenantContext.setTenantId("uuid-string");

// Read anywhere in your service/repository
String tenantId = TenantContext.getTenantId();
UUID companyId  = UUID.fromString(TenantContext.getTenantId());

// Check if set
boolean hasTenant = TenantContext.hasTenant();
```

### TenantInterceptor

Registered in `WebMvcConfig`. Runs on every request after `JwtAuthFilter` has set the `tenantId` request attribute. You never interact with this directly.

## How Tenant Isolation Works

```
JWT token contains companyId claim
    ↓
JwtAuthFilter extracts it → request.setAttribute("tenantId", companyId)
    ↓
TenantInterceptor reads it → TenantContext.setTenantId(companyId)
    ↓
BaseEntity @PrePersist → entity.companyId = TenantContext.getTenantId()
    ↓
Repository queries → WHERE company_id = TenantContext.getTenantId()
    ↓
TenantInterceptor afterCompletion → TenantContext.clear()
```

## Repository Pattern

Every repository must scope queries to the current tenant:

```java
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    // Always include companyId in every query
    Optional<Employee> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<Employee> findByCompanyId(UUID companyId, Pageable pageable);

    // Never do this — returns ALL tenants' data
    // List<Employee> findAll();  ← WRONG
}
```

## Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## Database Table

```sql
CREATE TABLE tenants (
    id         CHAR(36)     NOT NULL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    status     VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at DATETIME     NOT NULL
);
```
