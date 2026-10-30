# core-config

**Per-tenant runtime configuration stored in PostgreSQL, cached in Valkey.**

## What It Does

- Stores key/value configuration per tenant in the `platform_configs` table
- Caches values in Valkey (Redis-compatible) via Spring Cache — no DB hit on repeated reads
- Cache auto-evicts on write (`@CacheEvict`)
- Modules use this instead of hardcoding tenant-specific settings

## Package

```
com.platform.config
```

## Files

| File | Purpose |
|---|---|
| `PlatformConfig.java` | JPA entity for per-tenant config key/value pairs |
| `ConfigService.java` | Read/write config with Valkey caching |
| `PlatformConfigRepository.java` | JPA repository scoped by key + companyId |

---

## How to Use in Your Module

```java
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final ConfigService configService;

    public void processOnboarding(UUID employeeId) {
        // String value with default
        String welcomeMsg = configService.get("onboarding.welcome_message", "Welcome aboard!");

        // Integer value with default
        int maxTasks = configService.getInt("onboarding.max_tasks", 20);

        // Boolean value with default
        boolean sendEmail = configService.getBoolean("onboarding.send_welcome_email", true);
    }
}
```

---

## Read Methods

```java
// String — returns null if not set
String value = configService.get("key");

// String — returns default if not set
String value = configService.get("key", "defaultValue");

// Integer
int pageSize = configService.getInt("pagination.default_size", 20);

// Boolean
boolean enabled = configService.getBoolean("feature.chat.enabled", false);
```

---

## Write Method (admin operations)

```java
// Set a config value for the current tenant
// Cache is automatically evicted on write
configService.set("onboarding.max_tasks", "30", "ONBOARDING");
configService.set("hr.probation.default_days", "90", "HR");
```

---

## Cache Behaviour

- Cache name: `configs`
- Cache key: `configKey + ':' + tenantId` — each tenant has its own cached value
- Cache provider: Valkey (via `spring-boot-starter-data-redis`)
- TTL: controlled by `core-cache` ValkeyConfig (default 30 minutes)
- On write: `@CacheEvict` removes the specific key for the current tenant

---

## Recommended Config Key Naming

```
{module}.{feature}.{setting}

Examples:
  hr.probation.default_days          → "90"
  hr.document.expiry_reminder_days   → "30,7"
  onboarding.send_welcome_email      → "true"
  onboarding.max_tasks               → "20"
  pagination.default_size            → "20"
  payroll.processing_day             → "25"
  leave.annual_quota_days            → "21"
```

---

## Database Table

```sql
CREATE TABLE platform_configs (
    id           UUID         NOT NULL PRIMARY KEY,
    company_id   UUID         NOT NULL,
    config_key   VARCHAR(255) NOT NULL,
    config_value TEXT,
    category     VARCHAR(100),
    description  VARCHAR(500),
    is_encrypted BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL,
    version      BIGINT       NOT NULL DEFAULT 0,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE (config_key, company_id)
);
```

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-cache</artifactId></dependency>
<dependency><groupId>com.github.ben-manes.caffeine</groupId><artifactId>caffeine</artifactId></dependency>
```
