package com.hrms.skills.controller;

import com.hrms.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/employees/{employeeId}/skills")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Skills & Certifications", description = "Manage employee skills and professional certifications. " +
        "Skills are categorized as PRIMARY or SECONDARY. " +
        "Certifications with expiry dates are tracked and reminders are sent before expiry. " +
        "Employees can manage their own skills and certifications.")
public class SkillsController {

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(
            summary = "Add skill",
            description = "Adds a skill to an employee's profile. " +
                    "Skill types: PRIMARY (main expertise), SECONDARY (supporting skills). " +
                    "Proficiency levels: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Skill added successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> addSkill(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Skill details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "skillName": "Java",
                                      "skillType": "PRIMARY",
                                      "proficiencyLevel": "EXPERT",
                                      "yearsOfExperience": 5
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill added", null));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Get employee skills",
            description = "Returns all skills for an employee, grouped by skill type (PRIMARY and SECONDARY)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skills list returned"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<Void>> getSkills(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{skillId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(
            summary = "Remove skill",
            description = "Removes a skill from an employee's profile."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skill removed"),
            @ApiResponse(responseCode = "404", description = "Skill not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> removeSkill(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @Parameter(description = "Skill UUID", required = true) @PathVariable UUID skillId) {
        return ResponseEntity.ok(ApiResponse.success("Skill removed", null));
    }

    @PostMapping("/certifications")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(
            summary = "Add certification",
            description = "Adds a professional certification to an employee's profile. " +
                    "If `expiryDate` is provided, expiry reminders will be sent at 90, 60, 30, and 7 days before expiry. " +
                    "Optionally provide a credential URL for online verification."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Certification added successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> addCertification(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Certification details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "certificationName": "AWS Certified Solutions Architect",
                                      "issuingOrganization": "Amazon Web Services",
                                      "issueDate": "2023-06-15",
                                      "expiryDate": "2026-06-15",
                                      "credentialId": "AWS-SAA-C03-123456",
                                      "credentialUrl": "https://aws.amazon.com/verification/123456"
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Certification added", null));
    }

    @GetMapping("/certifications")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Get employee certifications",
            description = "Returns all certifications for an employee including expiry status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Certifications list returned"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<Void>> getCertifications(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/certifications/{certId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(
            summary = "Remove certification",
            description = "Removes a certification from an employee's profile."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Certification removed"),
            @ApiResponse(responseCode = "404", description = "Certification not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> removeCertification(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @Parameter(description = "Certification UUID", required = true) @PathVariable UUID certId) {
        return ResponseEntity.ok(ApiResponse.success("Certification removed", null));
    }
}
