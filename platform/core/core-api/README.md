# core-api

**Defines the contract for every API response and exception across the entire platform.**

## What It Does

- `ApiResponse<T>` — standard wrapper for every controller response
- `PagedResponse<T>` — standard wrapper for paginated responses
- `PlatformException` — the one exception all modules throw
- `GlobalExceptionHandler` — catches everything, returns consistent error format

## Package

```
com.platform.api
```

## Files

| File | Purpose |
|---|---|
| `ApiResponse.java` | Standard response wrapper for all endpoints |
| `PagedResponse.java` | Pagination wrapper for list endpoints |
| `PlatformException.java` | Base exception for all business errors |
| `GlobalExceptionHandler.java` | Catches all exceptions, returns ApiResponse error |

---

## ApiResponse\<T\>

Every controller in every module returns `ApiResponse<T>`. Never return raw objects.

```java
// Success with data
return ResponseEntity.ok(ApiResponse.success(employeeDto));

// Success with custom message
return ResponseEntity.ok(ApiResponse.success("Employee created", employeeDto));

// Success with traceId
return ResponseEntity.ok(ApiResponse.success("Done", data, traceId));
```

Response JSON shape:
```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "traceId": "a1b2c3d4e5f6g7h8",
  "timestamp": "2024-01-15T10:23:45"
}
```

Fields with `null` value are excluded from JSON (`@JsonInclude(NON_NULL)`).

---

## PagedResponse\<T\>

```java
Page<EmployeeDto> page = employeeService.findAll(pageable);
return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
```

Response JSON shape:
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## PlatformException

The only exception modules should throw. `GlobalExceptionHandler` catches it automatically.

```java
// 404 Not Found
throw PlatformException.notFound("Employee");
// → HTTP 404: {"success":false,"message":"Employee not found"}

// 409 Conflict
throw PlatformException.conflict("Email already exists");
// → HTTP 409

// 403 Forbidden
throw PlatformException.forbidden("Cannot delete active employee");
// → HTTP 403

// 400 Bad Request
throw PlatformException.badRequest("Invalid date range");
// → HTTP 400

// Custom HTTP status
throw new PlatformException("Custom message", HttpStatus.UNPROCESSABLE_ENTITY);
// → HTTP 422
```

---

## GlobalExceptionHandler

You never write `@ExceptionHandler` in your module controllers. This handles everything:

| Exception | HTTP Status | Notes |
|---|---|---|
| `PlatformException` | Whatever status was set | Business errors |
| `MethodArgumentNotValidException` | 400 | `@Valid` bean validation failures |
| `ConstraintViolationException` | 400 | Constraint violations |
| Any other `Exception` | 500 | Logged as ERROR |

---

## Full Controller Example

```java
@RestController
@RequestMapping("/api/v1/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PagedResponse.of(employeeService.findAll(pageable))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDto>> getById(@PathVariable UUID id) {
        // If not found, service throws PlatformException.notFound("Employee")
        // GlobalExceptionHandler returns HTTP 404 automatically
        return ResponseEntity.ok(ApiResponse.success(employeeService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDto>> create(@Valid @RequestBody CreateEmployeeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Employee created", employeeService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeeRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Employee updated", employeeService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted", null));
    }
}
```

---

## Dependencies

```xml
<dependency>
    <groupId>com.platform</groupId>
    <artifactId>core-tenancy</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```
