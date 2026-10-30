# core-logging

**Structured request tracing and service-layer performance monitoring. Zero configuration needed.**

## What It Does

- `MdcFilter` — injects `traceId`, `tenantId`, `userId` into SLF4J MDC for every request so every log line carries them automatically
- `LoggingAspect` — AOP intercepts every `@Service` method, logs execution time, flags slow calls as WARN

## Package

```
com.platform.logging
```

## Files

| File | Purpose |
|---|---|
| `MdcFilter.java` | Servlet filter — injects trace context into MDC per request |
| `LoggingAspect.java` | AOP aspect — logs all service method calls with timing |

---

## Zero Configuration

Both components are `@Component` and auto-register. You do nothing. Every module gets structured logging for free just by having `core-logging` on the classpath.

---

## MdcFilter

Runs on every HTTP request (registered with `@Order(1)` — runs first):

1. Generates a `traceId` (16-char hex string)
2. Reads `tenantId` from `TenantContext`
3. Reads `userId` from request attribute (set by `JwtAuthFilter`)
4. Puts all three into SLF4J MDC
5. Sets `X-Trace-Id` response header (so frontend can correlate)
6. After response: logs `METHOD /path STATUS DURATIONms`
7. Clears MDC after response

Every log line in your service automatically carries these fields:
```
2024-01-15 10:23:45 [traceId=a1b2c3d4e5f6g7h8] [tenantId=uuid] [userId=uuid] INFO EmployeeService - ...
```

---

## LoggingAspect

Intercepts every method in every `@Service` class automatically:

```
DEBUG OK    [EmployeeService.findAll(..)] 23ms      ← normal call
WARN  SLOW  [EmployeeService.export(..)] 650ms      ← exceeds threshold
ERROR ERROR [EmployeeService.create(..)] Duplicate  ← exception
```

Slow threshold default: **500ms**. Configure per environment:

```yaml
platform:
  logging:
    slow-threshold-ms: 500   # dev
    # slow-threshold-ms: 1000  # prod (more lenient)
```

---

## What You See in Logs

```
# Request log (from MdcFilter — one per request):
INFO  MdcFilter - GET /api/v1/hr/employees 200 45ms

# Service call logs (from LoggingAspect — one per service method):
DEBUG LoggingAspect - OK [EmployeeService.findAll(..)] 23ms
WARN  LoggingAspect - SLOW [ReportService.export(..)] 1250ms
ERROR LoggingAspect - ERROR [EmployeeService.create(..)] Duplicate email

# Your own log.info() calls automatically carry traceId/tenantId/userId:
log.info("Processing employee {}", id);
# → 2024-01-15 10:23:45 [traceId=a1b2c3d4] [tenantId=uuid-1] [userId=uuid-2] INFO EmployeeService - Processing employee uuid-3
```

---

## Logback Pattern (add to logback-spring.xml)

```xml
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss} [traceId=%X{traceId}] [tenantId=%X{tenantId}] [userId=%X{userId}] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-aop</artifactId></dependency>
```
