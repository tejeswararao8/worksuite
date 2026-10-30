# core-security

**Keycloak OAuth2 JWT authentication, RBAC entities, and user management via Keycloak Admin API.**

## What It Does

- `SecurityConfig` — configures Spring Security as an OAuth2 resource server, validates Keycloak-issued JWTs
- `JwtAuthFilter` — validates every request, extracts userId/companyId/email from Keycloak JWT, sets SecurityContext
- `UserPrincipal` — static helpers to get current userId/companyId/email/roles from anywhere in the app
- `Role` + `Permission` — RBAC entities for per-tenant role management
- `KeycloakUserService` — manages users in Keycloak via Admin REST API (create, update, reset password, disable, assign roles)

## Package

```
com.platform.security
com.platform.security.jwt
com.platform.security.rbac
com.platform.security.user
```

## Files

| File | Purpose |
|---|---|
| `SecurityConfig.java` | Spring Security + Keycloak OAuth2 resource server config |
| `jwt/JwtAuthFilter.java` | Validates Keycloak JWT on every request |
| `rbac/Role.java` | Role entity (per-tenant) |
| `rbac/Permission.java` | Permission entity (platform-wide) |
| `user/UserPrincipal.java` | Static helpers to access current user from SecurityContext |
| `user/KeycloakUserService.java` | Keycloak Admin REST API integration |

---

## Authentication Flow

```
User logs in via Keycloak (http://localhost:8080)
    ↓
Keycloak issues JWT access token
    ↓
Frontend sends: Authorization: Bearer <token>
    ↓
JwtAuthFilter intercepts every request
    ↓
JwtDecoder validates token against Keycloak JWKS endpoint
    ↓
JwtAuthenticationConverter extracts roles from realm_access.roles claim
    ↓
SecurityContext populated with JwtAuthenticationToken
    ↓
request.setAttribute("tenantId", companyId)  ← picked up by TenantInterceptor
request.setAttribute("userId", userId)        ← picked up by MdcFilter
    ↓
Controller/Service executes
```

---

## SecurityConfig — Public vs Protected URLs

```java
// These URLs are open — no token needed
"/api/v1/auth/**"
"/api/v1/tenants/register"
"/actuator/health"
"/swagger-ui/**"
"/v3/api-docs/**"

// Everything else requires a valid Keycloak JWT
```

---

## UserPrincipal — Static Helpers

Use anywhere in your service layer — no injection needed:

```java
// Get current user's Keycloak UUID
UUID userId = UserPrincipal.currentUserId();

// Get current tenant (companyId from JWT claim)
UUID companyId = UserPrincipal.currentCompanyId();

// Get current user's email
String email = UserPrincipal.currentEmail();

// Get all roles as Set<String>
Set<String> roles = UserPrincipal.currentRoles();
// → {"ROLE_HR_MANAGER", "ROLE_EMPLOYEE"}

// Check a specific role
boolean isHrManager = UserPrincipal.hasRole("HR_MANAGER");
```

---

## Role-Based Access Control

### Method-level security (recommended)

```java
@PreAuthorize("hasRole('HR_MANAGER')")
@PostMapping
public ResponseEntity<?> create(...) { ... }

@PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<?> delete(...) { ... }
```

### Programmatic check

```java
if (!UserPrincipal.hasRole("HR_MANAGER")) {
    throw PlatformException.forbidden("HR Manager role required");
}
```

### Keycloak Roles defined for the platform

| Role | Access |
|---|---|
| `SUPER_ADMIN` | Full access to everything |
| `HR_ADMIN` | HRMS + Payroll full access |
| `HR_MANAGER` | HRMS read/write |
| `EMPLOYEE` | Own profile, leave, tickets |
| `FINANCE_ADMIN` | Finance + Payroll |
| `PROJECT_MANAGER` | Projects + Tickets |
| `SUPPORT_AGENT` | Support tickets only |
| `IT_ADMIN` | IT support tickets |
| `MANAGER` | Team management |

---

## Role & Permission Entities

```java
// Role — per-tenant (extends BaseEntity, has companyId)
// Permission — platform-wide (no companyId)
// Many-to-many via role_permissions table

// Roles are synced from Keycloak realm roles
// Permissions define fine-grained access within the platform
```

---

## KeycloakUserService — User Management

Called when creating/updating employees in the platform. Keycloak is the single source of truth for credentials.

```java
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final KeycloakUserService keycloakUserService;

    public EmployeeDto create(CreateEmployeeRequest req) {
        // Create user in Keycloak first
        String keycloakUserId = keycloakUserService.createUser(
            req.getEmail(),
            req.getFirstName(),
            req.getLastName(),
            req.getTemporaryPassword(),
            companyId.toString(),
            List.of("EMPLOYEE")   // assign roles
        );

        // Save employee in DB with keycloakUserId reference
        Employee employee = Employee.builder()
            .keycloakUserId(keycloakUserId)
            .email(req.getEmail())
            // ...
            .build();
        return mapper.toDto(employeeRepository.save(employee));
    }

    public void resetPassword(UUID employeeId, String newPassword) {
        Employee employee = getById(employeeId);
        keycloakUserService.resetPassword(employee.getKeycloakUserId(), newPassword);
    }

    public void offboard(UUID employeeId) {
        Employee employee = getById(employeeId);
        keycloakUserService.disableUser(employee.getKeycloakUserId());
        // ... update employee status ...
    }
}
```

---

## Database Tables

```sql
-- permissions: platform-wide, no company_id
CREATE TABLE permissions (
    id          UUID         NOT NULL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,   -- e.g. EMPLOYEE_READ
    description VARCHAR(255),
    module      VARCHAR(100) NOT NULL            -- e.g. HR, FINANCE
);

-- roles: per-tenant
CREATE TABLE roles (
    id             UUID         NOT NULL PRIMARY KEY,
    company_id     UUID         NOT NULL,
    name           VARCHAR(100) NOT NULL,
    description    VARCHAR(255),
    is_system_role BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL,
    UNIQUE (name, company_id)
);

-- role_permissions: many-to-many join
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);
```

---

## Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8080/realms/vve-platform}

keycloak:
  auth-server-url: ${KEYCLOAK_URL:http://localhost:8080}
  realm: ${KEYCLOAK_REALM:vve-platform}
  admin:
    client-id: admin-cli
    username: ${KEYCLOAK_ADMIN_USER:admin}
    password: ${KEYCLOAK_ADMIN_PASS:admin123}
```

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-api</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-oauth2-resource-server</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId></dependency>
```
