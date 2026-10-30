package com.platform.hr.emergency.controller;

import com.platform.api.ApiResponse;
import com.platform.hr.emergency.dto.EmergencyContactDto;
import com.platform.hr.emergency.service.EmergencyContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/employees/{employeeId}/emergency-contacts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Emergency Contacts")
public class EmergencyContactController {

    private final EmergencyContactService contactService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Add emergency contact")
    public ResponseEntity<ApiResponse<EmergencyContactDto.Response>> create(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmergencyContactDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Emergency contact added", contactService.create(employeeId, request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get emergency contacts for employee")
    public ResponseEntity<ApiResponse<List<EmergencyContactDto.Response>>> findByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(contactService.findByEmployee(employeeId)));
    }

    @PutMapping("/{contactId}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Update emergency contact")
    public ResponseEntity<ApiResponse<EmergencyContactDto.Response>> update(
            @PathVariable UUID employeeId,
            @PathVariable UUID contactId,
            @Valid @RequestBody EmergencyContactDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Contact updated",
            contactService.update(employeeId, contactId, request)));
    }

    @DeleteMapping("/{contactId}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Delete emergency contact")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID employeeId,
            @PathVariable UUID contactId) {
        contactService.delete(employeeId, contactId);
        return ResponseEntity.ok(ApiResponse.success("Contact deleted", null));
    }
}
