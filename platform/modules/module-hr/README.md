# module-hr

**The HR business module — and the reference implementation for all future modules.**

## What It Does

Full employee lifecycle management built entirely on top of the 10 core platform modules.

| Sub-module | API Path | Description |
|---|---|---|
| Employee | `/hr/employees` | Core employee profiles |
| Department | `/hr/departments` | Org hierarchy |
| Branch | `/hr/branches` | Office locations |
| Designation | `/hr/designations` | Job titles |
| Team | `/hr/teams` | Sub-groups within departments |
| Probation | `/hr/probations` | Probation period tracking |
| Transfer | `/hr/transfers` | Department/branch/manager moves |
| Promotion | `/hr/promotions` | Designation changes |
| Asset | `/hr/assets` | Company asset assignment |
| Skills | `/hr/skills` | Employee skills + certifications |
| Onboarding | `/hr/onboarding` | New joiner task checklists |
| Offboarding | `/hr/offboarding` | Exit task checklists |
| Dashboard | `/hr/dashboard/stats` | HR KPIs |

## Package

```
com.platform.hr.*
```

## What This Module Does NOT Contain

Everything below is handled by core — zero duplication:

| Concern | Handled By |
|---|---|
| Authentication / JWT | core-security |
| Audit logging | core-audit (`@Auditable`) |
| Request tracing | core-logging (auto) |
| File storage | core-document (`LocalStorageService`) |
| Notifications | core-notification (`NotificationService`) |
| Approval workflows | core-workflow (`WorkflowService`) |
| Report export | core-reporting (`ReportService`) |
| Exception handling | core-api (`GlobalExceptionHandler`) |
| Tenant isolation | core-tenancy (`BaseEntity`, `TenantContext`) |
| Per-tenant config | core-config (`ConfigService`) |

## Entity Pattern

Every HR entity extends `BaseEntity`:

```java
@Entity
@Table(name = "employees",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "company_id"}),
        @UniqueConstraint(columnNames = {"employee_code", "company_id"})
    })
public class Employee extends BaseEntity {
    // Only business fields here
    // id, companyId, audit fields, version, isActive — all inherited
}
```

## Repository Pattern

Every repository scopes to current tenant:

```java
public interface EmployeeRepository extends JpaRepository<Employee, UUID>,
        JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<Employee> findByCompanyId(UUID companyId, Pageable pageable);
    boolean existsByEmailAndCompanyId(String email, UUID companyId);
}
```

## Service Pattern

```java
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.CREATE)
    public EmployeeDto create(EmployeeDto dto) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());

        if (employeeRepository.existsByEmailAndCompanyId(dto.getEmail(), companyId)) {
            throw PlatformException.conflict("Email already exists");
        }

        Employee employee = mapper.toEntity(dto);
        // companyId set by BaseEntity @PrePersist
        return mapper.toDto(employeeRepository.save(employee));
    }

    public EmployeeDto findById(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return employeeRepository.findByIdAndCompanyId(id, companyId)
            .map(mapper::toDto)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
    }
}
```

## Controller Pattern

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
        return ResponseEntity.ok(ApiResponse.success(
            PagedResponse.of(employeeService.findAll(PageRequest.of(page, size)))
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDto>> create(@Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Employee created", employeeService.create(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDto>> update(
            @PathVariable UUID id, @Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Updated", employeeService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }
}
```

## HR Permissions (seeded in V4/V5)

```
EMPLOYEE_READ, EMPLOYEE_WRITE
DEPARTMENT_READ, DEPARTMENT_WRITE
BRANCH_READ, BRANCH_WRITE
DESIGNATION_READ, DESIGNATION_WRITE
TEAM_READ, TEAM_WRITE
PROBATION_READ, PROBATION_WRITE
TRANSFER_READ, TRANSFER_WRITE
PROMOTION_READ, PROMOTION_WRITE
ASSET_READ, ASSET_WRITE
SKILL_READ, SKILL_WRITE
ONBOARDING_READ, ONBOARDING_WRITE
OFFBOARDING_READ, OFFBOARDING_WRITE
HR_REPORT_EXPORT
```

## Database Tables (V5 migration)

All 13 HR tables: `employees`, `departments`, `branches`, `designations`, `teams`, `probations`, `transfers`, `promotions`, `assets`, `employee_skills`, `employee_certifications`, `onboarding_checklists`, `offboarding_checklists`

All have: `id`, `company_id`, `created_by`, `created_at`, `updated_by`, `updated_at`, `version`, `is_active`

## Dependencies

```xml
<!-- All 10 core modules -->
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-api</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-audit</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-logging</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-notification</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-document</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-workflow</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-config</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-reporting</artifactId></dependency>
<dependency><groupId>org.mapstruct</groupId><artifactId>mapstruct</artifactId></dependency>
```

## Using This as a Template for New Modules

1. Copy `module-hr/pom.xml`, change `artifactId` to `module-xyz`
2. Create `com.platform.xyz.*` packages
3. Follow the entity/repository/service/controller patterns above
4. Add Flyway migration for your tables
5. Add your permissions to seed data
6. Write your `module-xyz/README.md`
