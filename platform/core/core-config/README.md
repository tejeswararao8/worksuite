# core-config

**Per-tenant runtime configuration with Caffeine caching.**

## What It Does

- Stores key/value config per tenant in the database
- Caches values in Caffeine (10 min TTL) — no DB hit on every read
- Cache auto-evicts on write
- Modules use this instead of hardcoding tenant-specific settings

## Package

```
com.platform.config
```

## How to Use in Your Module

### Inject ConfigService

```java
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final ConfigService configService;

    public void processOnboarding(UUID employeeId) {
        // Read config — returns cached value or DB fallback
        int maxTasks = configService.getInt("onboarding.max_tasks", 20);
        boolean sendEmail = configService.getBoolean("onboarding.send_welcome_email", true);
        String welcomeMsg = configService.get("onboarding.welcome_message", "Welcome aboard!");

        // Use the values
    }
}
```

### Read Methods

```java
// String value
String value = configService.get("key");                    // null if not set
String value = configService.get("key", "defaultValue");    // default if not set

// Integer
int pageSize = configService.getInt("pagination.size", 20);

// Boolean
boolean enabled = configService.getBoolean("feature.x.enabled", false);
```

### Write (admin operations)

```java
// Set a config value for the current tenant
configService.set("onboarding.max_tasks", "30", "ONBOARDING");

// Cache is automatically evicted on write
```

### Cache Key

Cache key = `configKey + ':' + tenantId` — so each tenant has its own cached value for the same key.

## Recommended Config Key Naming

```
{module}.{feature}.{setting}

Examples:
  hr.probation.default_days        → "90"
  hr.document.expiry_reminder_days → "30,7"
  onboarding.send_welcome_email    → "true"
  pagination.default_size          → "20"
```

## Database Table

```sql
CREATE TABLE platform_configs (
    id           CHAR(36) NOT NULL PRIMARY KEY,
    company_id   CHAR(36) NOT NULL,
    config_key   VARCHAR(255) NOT NULL,
    config_value TEXT,
    category     VARCHAR(100),
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uq_config_key_company (config_key, company_id)
);
```

## Configuration

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=10m
```

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-cache</artifactId></dependency>
<dependency><groupId>com.github.ben-manes.caffeine</groupId><artifactId>caffeine</artifactId></dependency>
```
