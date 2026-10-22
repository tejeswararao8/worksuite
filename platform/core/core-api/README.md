# core-api

**Defines the contract for every API response and exception in the platform.**

## What It Does

- `ApiResponse<T>` — standard wrapper for every controller response
- `PagedResponse<T>` — standard wrapper for paginated responses
- `PlatformException` — the one exception all modules throw
- `GlobalExceptionHandler` — catches everything, returns consistent error format

## Package

```
com.platform.api
```

## Key Classes

### ApiResponse\<T\>

Every controller in every module returns `ApiResponse<T>`. Never return raw objects.

```java
// Success with data
return ResponseEntity.ok(ApiResponse.success(employeeDto));

// Success with custom message
return ResponseEntity.ok(ApiResponse.success("Employee created", employeeDto));

// Success with traceId
return ResponseEntity.ok(ApiResponse.success("Done", data, traceId));

// Error (used by GlobalExceptionHandler — you don't call this)
ApiResponse.error("Employee not found");
```

Response shape:
```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "traceId": "abc123",
  "timestamp": "2024-01-01T10:00:00"
}
```

### PagedResponse\<T\>

```java
Page<EmployeeDto> page = employeeService.findAll(pageable);
return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
```

Response shape:
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

### PlatformException

The only exception modules should throw. `GlobalExceptionHandler` catches it automatically.

```java
// 404
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

// Custom status
throw new PlatformException("Custom message", HttpStatus.UNPROCESSABLE_ENTITY);
```

### GlobalExceptionHandler

You never write `@ExceptionHandler` in your module. This handles:

| Exception | HTTP Status |
|---|---|
| `PlatformException` | Whatever status was set |
| `MethodArgumentNotValidException` | 400 — with field errors |
| `ConstraintViolationException` | 400 |
| Any other `Exception` | 500 |

## Full Controller Example

```java
@RestController
@RequestMapping("/hr/employees")
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
        return ResponseEntity.ok(ApiResponse.success(employeeService.findById(id)));
        // If not found, service throws PlatformException.notFound("Employee")
        // GlobalExceptionHandler returns 404 automatically
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDto>> create(@Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Employee created", employeeService.create(dto)));
    }
}
```

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
```
