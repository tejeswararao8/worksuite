package com.platform.hr.invitation.bulk;

import com.platform.api.ApiResponse;
import com.platform.hr.subscription.EnforceModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/hr/employees/bulk")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Bulk Employee Onboarding")
public class BulkOnboardingController {

    private final BulkOnboardingService bulkOnboardingService;

    @GetMapping("/template")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Download CSV import template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] csv = bulkOnboardingService.generateCsvTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employee_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @EnforceModule("HR")
    @Operation(summary = "Bulk import employees from CSV or XLSX")
    public ResponseEntity<ApiResponse<BulkOnboardingDto.ImportResponse>> importFile(
            @RequestParam("file") MultipartFile file) throws IOException {
        BulkOnboardingDto.ImportResponse response = bulkOnboardingService.importFile(file);
        return ResponseEntity.ok(ApiResponse.success("Bulk import completed", response));
    }
}
