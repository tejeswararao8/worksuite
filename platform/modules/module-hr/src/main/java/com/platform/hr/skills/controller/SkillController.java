package com.platform.hr.skills.controller;

import com.platform.api.ApiResponse;
import com.platform.hr.skills.dto.SkillDto;
import com.platform.hr.skills.service.SkillService;
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
@RequestMapping("/api/v1/hr/employees/{employeeId}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Skills & Certifications")
public class SkillController {

    private final SkillService skillService;

    // ── SKILLS ────────────────────────────────────────────────

    @PostMapping("/skills")
    @PreAuthorize("hasAnyAuthority('SKILL_WRITE')")
    @Operation(summary = "Add skill to employee")
    public ResponseEntity<ApiResponse<SkillDto.SkillResponse>> addSkill(
            @PathVariable UUID employeeId,
            @Valid @RequestBody SkillDto.CreateSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Skill added", skillService.addSkill(employeeId, request)));
    }

    @GetMapping("/skills")
    @PreAuthorize("hasAnyAuthority('SKILL_READ')")
    @Operation(summary = "Get skills for employee")
    public ResponseEntity<ApiResponse<List<SkillDto.SkillResponse>>> findSkills(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(skillService.findSkillsByEmployee(employeeId)));
    }

    @PutMapping("/skills/{skillId}")
    @PreAuthorize("hasAnyAuthority('SKILL_WRITE')")
    @Operation(summary = "Update skill")
    public ResponseEntity<ApiResponse<SkillDto.SkillResponse>> updateSkill(
            @PathVariable UUID employeeId,
            @PathVariable UUID skillId,
            @Valid @RequestBody SkillDto.UpdateSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Skill updated",
            skillService.updateSkill(employeeId, skillId, request)));
    }

    @DeleteMapping("/skills/{skillId}")
    @PreAuthorize("hasAnyAuthority('SKILL_WRITE')")
    @Operation(summary = "Delete skill")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(
            @PathVariable UUID employeeId, @PathVariable UUID skillId) {
        skillService.deleteSkill(employeeId, skillId);
        return ResponseEntity.ok(ApiResponse.success("Skill deleted", null));
    }

    // ── CERTIFICATIONS ────────────────────────────────────────

    @PostMapping("/certifications")
    @PreAuthorize("hasAnyAuthority('SKILL_WRITE')")
    @Operation(summary = "Add certification to employee")
    public ResponseEntity<ApiResponse<SkillDto.CertificationResponse>> addCertification(
            @PathVariable UUID employeeId,
            @Valid @RequestBody SkillDto.CreateCertificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Certification added",
                skillService.addCertification(employeeId, request)));
    }

    @GetMapping("/certifications")
    @PreAuthorize("hasAnyAuthority('SKILL_READ')")
    @Operation(summary = "Get certifications for employee")
    public ResponseEntity<ApiResponse<List<SkillDto.CertificationResponse>>> findCertifications(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(skillService.findCertificationsByEmployee(employeeId)));
    }

    @PutMapping("/certifications/{certId}")
    @PreAuthorize("hasAnyAuthority('SKILL_WRITE')")
    @Operation(summary = "Update certification")
    public ResponseEntity<ApiResponse<SkillDto.CertificationResponse>> updateCertification(
            @PathVariable UUID employeeId,
            @PathVariable UUID certId,
            @Valid @RequestBody SkillDto.UpdateCertificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Certification updated",
            skillService.updateCertification(employeeId, certId, request)));
    }

    @DeleteMapping("/certifications/{certId}")
    @PreAuthorize("hasAnyAuthority('SKILL_WRITE')")
    @Operation(summary = "Delete certification")
    public ResponseEntity<ApiResponse<Void>> deleteCertification(
            @PathVariable UUID employeeId, @PathVariable UUID certId) {
        skillService.deleteCertification(employeeId, certId);
        return ResponseEntity.ok(ApiResponse.success("Certification deleted", null));
    }
}

// ── Separate endpoint for skill search (not under employee) ──────────────────
