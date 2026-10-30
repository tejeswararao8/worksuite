# core-cache

**Valkey (Redis-compatible) caching and real-time pub/sub for all modules.**

## What It Does

- `ValkeyConfig` — configures Lettuce connection factory, RedisTemplate, RedisCacheManager with per-module TTLs, and pub/sub listener container
- `CacheService` — general-purpose cache operations: set, get, delete, increment, rate limiting, session management
- `PubSubService` — publish/subscribe for real-time features: chat room broadcasting, per-user notifications

## Tech

Valkey is a 100% free BSD-licensed fork of Redis 7.2, backed by AWS, Google, Oracle, and the Linux Foundation. It uses the exact same API as Redis — all Spring Data Redis code works unchanged.

## Package

```
com.platform.cache.config
com.platform.cache.service
```

## Files

| File | Purpose |
|---|---|
| `config/ValkeyConfig.java` | Lettuce connection, RedisTemplate, CacheManager, pub/sub container |
| `service/CacheService.java` | General cache operations + session + rate limiting |
| `service/PubSubService.java` | Publish/subscribe for real-time features |

---

## CacheService Usage

```java
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final CacheService cacheService;

    public EmployeeDto findById(UUID id) {
        String key = "employee:" + id;

        // Try cache first
        EmployeeDto cached = cacheService.get(key, EmployeeDto.class);
        if (cached != null) return cached;

        // Load from DB
        EmployeeDto dto = mapper.toDto(employeeRepository.findById(id).orElseThrow(...));

        // Cache for 30 minutes
        cacheService.set(key, dto, 30);
        return dto;
    }

    public void update(UUID id, UpdateEmployeeRequest req) {
        // ... update in DB ...

        // Evict cache
        cacheService.delete("employee:" + id);
    }

    public void deleteByDepartment(UUID departmentId) {
        // Delete all employee cache entries for a department
        cacheService.deleteByPattern("employee:dept:" + departmentId + ":*");
    }
}
```

---

## CacheService Methods

```java
// Store with TTL (minutes)
cacheService.set("key", value, 30);
cacheService.set("key", value);          // default 30 minutes

// Retrieve
Object value = cacheService.get("key");
EmployeeDto dto = cacheService.get("key", EmployeeDto.class);

// Delete
cacheService.delete("key");
cacheService.deleteAll(List.of("key1", "key2"));
cacheService.deleteByPattern("employees:*");

// Check existence
boolean exists = cacheService.exists("key");

// Update TTL
cacheService.expire("key", 60);

// Counter (for rate limiting, attempt tracking)
Long count = cacheService.increment("key");
Long count = cacheService.incrementBy("key", 5L);

// Session management
cacheService.saveSession(userId, sessionData);   // 1 hour TTL
Object session = cacheService.getSession(userId);
cacheService.deleteSession(userId);

// Rate limiting (returns true if limit exceeded)
boolean blocked = cacheService.isRateLimited("login:" + email, 5, 15); // 5 attempts per 15 min
cacheService.resetRateLimit("login:" + email);
```

---

## PubSubService Usage

Used by the Chat module and real-time notification features:

```java
@Service
@RequiredArgsConstructor
public class ChatService {

    private final PubSubService pubSubService;

    public void sendMessage(String roomId, ChatMessage message) {
        // Save to MongoDB
        chatMessageRepository.save(message);

        // Broadcast to all subscribers of this room
        pubSubService.publishToRoom(roomId, message);
        // → publishes to channel: "chat:room:{roomId}"
    }
}

// Send real-time notification to a specific user
pubSubService.publishNotification(userId.toString(), notificationDto);
// → publishes to channel: "notification:user:{userId}"

// Custom channel
pubSubService.publish("custom:channel", payload);

// Subscribe to a channel
pubSubService.subscribe("chat:room:" + roomId, listenerAdapter);

// Unsubscribe
pubSubService.unsubscribe("chat:room:" + roomId, listenerAdapter);
```

---

## Pre-configured Cache TTLs (ValkeyConfig)

| Cache Name | TTL | Used By |
|---|---|---|
| `employees` | 30 min | HRMS |
| `departments` | 2 hours | HRMS |
| `designations` | 2 hours | HRMS |
| `branches` | 2 hours | HRMS |
| `teams` | 1 hour | HRMS |
| `payroll` | 1 hour | Payroll |
| `payslips` | 1 hour | Payroll |
| `tickets` | 15 min | Tickets |
| `projects` | 30 min | Tickets |
| `support` | 15 min | Support |
| `finance` | 30 min | Finance |
| `users` | 30 min | Auth |
| `roles` | 2 hours | Auth |
| `permissions` | 2 hours | Auth |

---

## Spring @Cacheable Integration

Modules can also use Spring's `@Cacheable` annotation directly — it uses the same Valkey CacheManager:

```java
@Cacheable(value = "departments", key = "#companyId")
public List<DepartmentDto> findAll(UUID companyId) {
    return departmentRepository.findByCompanyId(companyId)
        .stream().map(mapper::toDto).toList();
}

@CacheEvict(value = "departments", key = "#companyId")
public DepartmentDto create(UUID companyId, CreateDepartmentRequest req) {
    // ...
}
```

---

## Configuration

```yaml
spring:
  redis:
    host: ${VALKEY_HOST:localhost}
    port: ${VALKEY_PORT:6379}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 10
        max-idle: 5
        min-idle: 2
        max-wait: 1000ms

  cache:
    type: redis
    redis:
      time-to-live: 30m
      cache-null-values: false
```

Start Valkey with:
```bash
docker compose up -d valkey
```

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-redis</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-cache</artifactId></dependency>
```
