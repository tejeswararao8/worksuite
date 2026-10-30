# core-tenancy

**The foundation of the entire platform. Every other module depends on this.**

## What It Does

- `BaseEntity` — superclass every entity in every module must extend
- `TenantContext` — ThreadLocal that holds the current tenant (companyId) for the duration of a request
- `TenantInterceptor` — reads tenantId from the JWT-injected request attribute and loads it into TenantContext
- `Tenant` — entity that is the registry of all companies using the platform
- `TenantService` — create, suspend, and lookup tenants
- `TenantException` — thrown when tenant operations fail

## Package

```
com.platform.tenancy
```

## Files

| File | Purpose |
|---|---|
| `BaseEntity.java` | Abstract superclass for all JPA entities |
| `Tenant.java` | Tenant (company) entity |
| `TenantContext.java` | ThreadLocal tenant holder |
| `TenantInterceptor.java` | HTTP interceptor that sets TenantContext per request |
| `TenantService.java` | Create, get, suspend tenants |
| `TenantRepository.java` | JPA repository for Tenant |
| `TenantException.java` | Runtime exception for tenant errors |

---

## BaseEntity

Every entity in every module extends this. You get for free:

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Auto-generated primary key (GenerationType.UUID) |
| `companyId` | UUID | Tenant isolation — auto-set from TenantContext on `@PrePersist` |
| `createdBy` | String | Set by Spring Data Auditing from SecurityContext |
| `createdAt` | LocalDateTime | Set by Spring Data Auditing |
| `updatedBy` | String | Set by Spring Data Auditing |
| `updatedAt` | LocalDateTime | Set by Spring Data Auditing |
| `version` | Long | Optimistic locking — prevents lost updates, starts at 0 |
| `isActive` | Boolean | Soft delete flag — default true |

The `@PrePersist` hook auto-sets `companyId` from `TenantContext` so you never set it manually.

```java
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {
    private String name;
    // id, companyId, createdBy, createdAt, updatedBy, updatedAt, version, isActive — all inherited
}
```

---

## TenantContext

```java
// Set by TenantInterceptor — you never call this directly
TenantContext.setTenantId("uuid-string");

// Read anywhere in your service
String tenantId = TenantContext.getTenantId();
UUID companyId  = UUID.fromString(TenantContext.getTenantId());

// Check if set before using
if (TenantContext.hasTenant()) {
    UUID companyId = UUID.fromString(TenantContext.getTenantId());
}

// Cleared automatically by TenantInterceptor.afterCompletion()
```

---

## How Tenant Isolation Works

```
Request arrives with JWT token (contains companyId claim)
    ↓
JwtAuthFilter decodes JWT → request.setAttribute("tenantId", companyId)
    ↓
TenantInterceptor.preHandle() → TenantContext.setTenantId(companyId)
    ↓
BaseEntity @PrePersist → entity.companyId = UUID.fromString(TenantContext.getTenantId())
    ↓
Repository queries → always include companyId in WHERE clause
    ↓
TenantInterceptor.afterCompletion() → TenantContext.clear()
```

---

## Repository Pattern — Always Scope to Tenant

Every repository must include `companyId` in every query:

```java
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    // Correct — scoped to tenant
    Optional<Employee> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<Employee> findByCompanyId(UUID companyId, Pageable pageable);
    Optional<Employee> findByEmailAndCompanyId(String email, UUID companyId);

    // WRONG — returns ALL tenants' data — never do this
    // List<Employee> findAll();
    // Optional<Employee> findByEmail(String email);
}
```

---

## Tenant Entity

```java
Tenant tenant = Tenant.builder()
    .name("Acme Corp")
    .slug("acme-corp")           // URL-safe unique identifier
    .email("admin@acme.com")
    .phone("+971501234567")
    .status(Tenant.TenantStatus.ACTIVE)
    .build();
```

TenantStatus values: `ACTIVE`, `SUSPENDED`, `TRIAL`, `CANCELLED`

---

## TenantService Usage

```java
// Create a new tenant (on company registration)
Tenant tenant = tenantService.createTenant("Acme Corp", "acme-corp", "admin@acme.com", "+971501234567");

// Get by ID
Tenant tenant = tenantService.getById(tenantId);

// Get by slug (for subdomain routing)
Tenant tenant = tenantService.getBySlug("acme-corp");

// Suspend a tenant
tenantService.suspendTenant(tenantId);
```

---

## Database Tables

```sql
CREATE TABLE tenants (
    id         UUID         NOT NULL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    phone      VARCHAR(50),
    logo_path  VARCHAR(500),
    status     VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL
);
```

---

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
