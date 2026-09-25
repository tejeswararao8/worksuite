package com.hrms.employee.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.employee.dto.EmployeeDto;
import com.hrms.employee.entity.Employee.EmploymentStatus;
import com.hrms.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Employee Management", description = "Core employee lifecycle management — create, search, update, and manage employee profiles. " +
        "All operations are scoped to the authenticated user's company (multi-tenant).")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Create employee",
            description = "Creates a new employee profile under the authenticated company. " +
                    "An auto-generated employee code (e.g. EMP00001) is assigned. " +
                    "The employee starts with PROBATION status by default. " +
                    "A user account can be created separately via POST /users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created successfully with auto-generated employee code"),
            @ApiResponse(responseCode = "409", description = "An employee with the same email already exists in this company"),
            @ApiResponse(responseCode = "400", description = "Validation error — required fields missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Employee profile details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "firstName": "John",
                                      "lastName": "Doe",
                                      "email": "john.doe@company.com",
                                      "mobile": "+971501234567",
                                      "gender": "MALE",
                                      "dateOfBirth": "1990-05-15",
                                      "nationality": "Indian",
                                      "maritalStatus": "SINGLE",
                                      "bloodGroup": "O+",
                                      "joiningDate": "2024-01-15",
                                      "departmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                      "designationId": "3fa85f64-5717-4562-b3fc-2c963f66afa7",
                                      "branchId": "3fa85f64-5717-4562-b3fc-2c963f66afa8",
                                      "managerId": "3fa85f64-5717-4562-b3fc-2c963f66afa9",
                                      "workLocation": "Dubai HQ",
                                      "employmentType": "FULL_TIME"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody EmployeeDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created", employeeService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Get employee by ID",
            description = "Returns the full profile of an employee by UUID. " +
                    "Employees can only view their own profile. Managers can view their direct reports. " +
                    "HR and COMPANY_ADMIN can view all employees in the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee profile returned"),
            @ApiResponse(responseCode = "404", description = "Employee not found or belongs to a different company"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> findById(
            @Parameter(description = "Employee UUID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER')")
    @Operation(
            summary = "Search employees",
            description = "Full-text search and filter employees within the company. " +
                    "The `q` parameter searches across first name, last name, email, and employee code. " +
                    "All filters are optional and can be combined. Results are paginated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of matching employees"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeDto.Response>>> search(
            @Parameter(description = "Search keyword — matches name, email, or employee code", example = "john")
            @RequestParam(required = false) String q,
            @Parameter(description = "Filter by department UUID")
            @RequestParam(required = false) UUID departmentId,
            @Parameter(description = "Filter by branch UUID")
            @RequestParam(required = false) UUID branchId,
            @Parameter(description = "Filter by employment status", example = "ACTIVE")
            @RequestParam(required = false) EmploymentStatus status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.search(q, departmentId, branchId, status, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Update employee personal details",
            description = "Updates an employee's personal information such as contact details, address, and personal data. " +
                    "Employment information (department, designation, manager) is managed via Transfer and Promotion APIs. " +
                    "Only provided fields are updated (partial update)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> update(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody EmployeeDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Employee updated", employeeService.update(id, request)));
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(
            summary = "Upload employee profile photo",
            description = "Uploads a profile photo for the employee. " +
                    "Accepted formats: JPEG, PNG, WEBP. Maximum file size: 5MB. " +
                    "The photo is stored in AWS S3 and the URL is saved on the employee profile. " +
                    "Employees can upload their own photo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo uploaded and profile updated"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid file type or file too large"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> uploadPhoto(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "Photo file (JPEG/PNG/WEBP, max 5MB)", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Photo uploaded", employeeService.uploadPhoto(id, file)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Delete employee",
            description = "Soft-deletes an employee by setting is_active = false. " +
                    "The employee record is retained for audit and historical purposes. " +
                    "Associated user account should be deactivated separately. " +
                    "This is typically triggered as part of the Offboarding process."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted", null));
    }
}
