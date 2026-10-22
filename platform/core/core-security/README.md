# core-security

**JWT authentication, RBAC, User/Role/Permission entities, and token blacklist.**

## What It Does

- Issues and validates JWT access tokens (24h) and refresh tokens (7d)
- Blacklists tokens on logout using Caffeine cache (no Redis)
- Provides `User`, `Role`, `Permission` entities with many-to-many RBAC
- `JwtAuthFilter` — validates every request, sets SecurityContext + tenantId
- `UserPrincipal` — static helpers to get current userId/companyId/permissions anywhere

## Package

```
com.platform.security.jwt
com.platform.security.rbac
com.platform.security.user
```

## Key Classes

### JwtTokenProvider

Used by your auth service to issue tokens on login.

```java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;

    public LoginResponse login(String email, String password) {
        // ... validate user ...
        String roles = user.getRoles().stream()
            .map(Role::getName).collect(Collectors.joining(","));

        String accessToken  = jwtTokenProvider.generateAccessToken(
            user.getId(), user.getCompanyId(), user.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(
            user.getId(), user.getCompanyId());

        return new LoginResponse(accessToken, refreshToken);
    }

    public void logout(String token) {
        tokenBlacklist.blacklist(token);
    }
}
```

### TokenBlacklist

Caffeine-based in-memory blacklist. Tokens auto-expire after refresh token TTL (7d). No Redis needed.

```java
// Blacklist on logout
tokenBlacklist.blacklist(token);

// Check on every request (done by JwtAuthFilter — you don't call this)
tokenBlacklist.isBlacklisted(token);
```

### UserPrincipal — Static Helpers

Get the current authenticated user from anywhere in your service layer:

```java
// Get current user's ID
UUID userId = UserPrincipal.currentUserId();

// Get current tenant (companyId)
UUID companyId = UserPrincipal.currentCompanyId();

// Get current user's permissions
Set<String> perms = UserPrincipal.currentPermissions();

// Check a specific permission
boolean canWrite = UserPrincipal.currentPermissions().contains("EMPLOYEE_WRITE");
```

### User Entity

```java
// User extends BaseEntity — gets companyId, audit fields, etc.
User user = User.builder()
    .email("john@company.com")
    .passwordHash(passwordEncoder.encode("password"))
    .firstName("John")
    .lastName("Doe")
    .status(User.UserStatus.ACTIVE)
    .mustChangePassword(true)
    .build();
// companyId set automatically from TenantContext
```

Fields: `email`, `passwordHash`, `firstName`, `lastName`, `status` (ACTIVE/INACTIVE/SUSPENDED), `failedLoginAttempts`, `lockedUntil`, `mustChangePassword`, `lastLoginAt`, `roles`

### Role & Permission

```java
// Roles are per-tenant (company_id scoped)
// Permissions are platform-wide (no company_id)
// Many-to-many via role_permissions table

// Check in controller with Spring Security
@PreAuthorize("hasAuthority('EMPLOYEE_WRITE')")
@PostMapping
public ResponseEntity<?> create(...) { ... }

// Or check programmatically
if (!UserPrincipal.currentPermissions().contains("EMPLOYEE_WRITE")) {
    throw PlatformException.forbidden("Insufficient permissions");
}
```

## JWT Token Structure

```
Header: { alg: HS256 }
Payload: {
  sub: "userId-uuid",
  companyId: "companyId-uuid",
  email: "user@company.com",
  roles: "HR_MANAGER,EMPLOYEE_READ",
  type: "access",
  iat: ...,
  exp: ...
}
```

## JwtAuthFilter Flow

```
Every request
    ↓
Extract "Authorization: Bearer <token>" header
    ↓
validateToken() — checks signature + expiry
    ↓
isBlacklisted() — checks Caffeine cache
    ↓
parseToken() — extract userId, companyId, roles
    ↓
Set SecurityContext (UsernamePasswordAuthenticationToken)
    ↓
request.setAttribute("tenantId", companyId)  ← picked up by TenantInterceptor
request.setAttribute("userId", userId)        ← picked up by MdcFilter
```

## Database Tables

```sql
-- permissions: platform-wide, no company_id
CREATE TABLE permissions (
    id     CHAR(36) NOT NULL PRIMARY KEY,
    name   VARCHAR(100) NOT NULL UNIQUE,  -- e.g. EMPLOYEE_READ
    module VARCHAR(100) NOT NULL          -- e.g. HR
);

-- roles: per-tenant
CREATE TABLE roles (
    id             CHAR(36) NOT NULL PRIMARY KEY,
    company_id     CHAR(36) NOT NULL,
    name           VARCHAR(100) NOT NULL,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE
);

-- users: per-tenant
CREATE TABLE users (
    id                   CHAR(36) NOT NULL PRIMARY KEY,
    company_id           CHAR(36) NOT NULL,
    email                VARCHAR(255) NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    status               VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until         DATETIME,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE
);
```

## Configuration

```yaml
platform:
  jwt:
    secret: your-256-bit-secret-key-here
    access-token-ms: 86400000    # 24 hours
    refresh-token-ms: 604800000  # 7 days
```

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-api</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId></dependency>
<dependency><groupId>com.github.ben-manes.caffeine</groupId><artifactId>caffeine</artifactId></dependency>
```
