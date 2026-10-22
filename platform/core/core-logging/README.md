# core-logging

**Structured request tracing and service-layer performance monitoring. Zero config needed.**

## What It Does

- `MdcFilter` — injects `traceId`, `tenantId`, `userId` into MDC for every request so every log line carries them
- `LoggingAspect` — AOP intercepts every `@Service` method, logs execution time, flags slow calls

## Package

```
com.platform.logging
```

## How It Works (Zero Config)

Both components are `@Component` and auto-register. You do nothing. Every module gets this for free.

### MdcFilter

Runs on every HTTP request:

1. Generates a `traceId` (16-char hex)
2. Reads `tenantId` from `TenantContext`
3. Reads `userId` from request attribute (set by `JwtAuthFilter`)
4. Puts all three into SLF4J MDC
5. Sets `X-Trace-Id` response header
6. Logs: `GET /hr/employees 200 45ms`
7. Clears MDC after response

Every log line in your service automatically looks like:
```
2024-01-15 10:23:45 [traceId=a1b2c3d4e5f6g7h8] [tenantId=uuid] [userId=uuid] INFO  EmployeeService - ...
```

### LoggingAspect

Intercepts every method in every `@Service` class:

```
OK   [EmployeeService.create(..)] 23ms      ← normal
SLOW [EmployeeService.findAll(..)] 650ms    ← flagged as WARN
ERROR [EmployeeService.delete(..)] message  ← exception logged as ERROR
```

Slow threshold default: **500ms**. Configure per environment:

```yaml
platform:
  logging:
    slow-threshold-ms: 500
```

## What You Get in Logs

```
# Every request log line (from MdcFilter):
GET /hr/employees 200 45ms

# Every service call (from LoggingAspect):
DEBUG OK [EmployeeService.findAll(..)] 23ms
WARN  SLOW [ReportService.export(..)] 1250ms
ERROR ERROR [EmployeeService.create(..)] Duplicate email

# Your own log statements automatically carry traceId/tenantId/userId:
log.info("Processing employee {}", id);
# → 2024-01-15 10:23:45 [traceId=a1b2c3d4] [tenantId=uuid] INFO EmployeeService - Processing employee uuid
```

## Logback Configuration (recommended for production)

Add to `logback-spring.xml` to output structured JSON:

```xml
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss} [traceId=%X{traceId}] [tenantId=%X{tenantId}] [userId=%X{userId}] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-aop</artifactId></dependency>
```
