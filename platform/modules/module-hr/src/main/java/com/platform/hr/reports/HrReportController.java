package com.platform.hr.reports;

import com.platform.reporting.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/hr/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Reports")
public class HrReportController {

    private final HrReportService hrReportService;

    @GetMapping("/employees")
    @PreAuthorize("hasAnyAuthority('HR_REPORT_EXPORT')")
    @Operation(summary = "Export employee master report")
    public ResponseEntity<byte[]> employeeMaster(
            @RequestParam(defaultValue = "EXCEL") ReportService.ReportFormat format) throws IOException {
        byte[] data = hrReportService.employeeMasterReport(format);
        return download(data, "employee_master", format);
    }

    @GetMapping("/new-joiners")
    @PreAuthorize("hasAnyAuthority('HR_REPORT_EXPORT')")
    @Operation(summary = "Export new joiners report")
    public ResponseEntity<byte[]> newJoiners(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "EXCEL") ReportService.ReportFormat format) throws IOException {
        byte[] data = hrReportService.newJoinersReport(from, to, format);
        return download(data, "new_joiners", format);
    }

    @GetMapping("/probation")
    @PreAuthorize("hasAnyAuthority('HR_REPORT_EXPORT')")
    @Operation(summary = "Export probation status report")
    public ResponseEntity<byte[]> probation(
            @RequestParam(defaultValue = "EXCEL") ReportService.ReportFormat format) throws IOException {
        byte[] data = hrReportService.probationReport(format);
        return download(data, "probation_status", format);
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAnyAuthority('HR_REPORT_EXPORT')")
    @Operation(summary = "Export transfer history report")
    public ResponseEntity<byte[]> transfers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "EXCEL") ReportService.ReportFormat format) throws IOException {
        byte[] data = hrReportService.transferReport(from, to, format);
        return download(data, "transfer_history", format);
    }

    @GetMapping("/promotions")
    @PreAuthorize("hasAnyAuthority('HR_REPORT_EXPORT')")
    @Operation(summary = "Export promotion history report")
    public ResponseEntity<byte[]> promotions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "EXCEL") ReportService.ReportFormat format) throws IOException {
        byte[] data = hrReportService.promotionReport(from, to, format);
        return download(data, "promotion_history", format);
    }

    @GetMapping("/assets")
    @PreAuthorize("hasAnyAuthority('HR_REPORT_EXPORT')")
    @Operation(summary = "Export asset assignment report")
    public ResponseEntity<byte[]> assets(
            @RequestParam(defaultValue = "EXCEL") ReportService.ReportFormat format) throws IOException {
        byte[] data = hrReportService.assetReport(format);
        return download(data, "asset_assignment", format);
    }

    private ResponseEntity<byte[]> download(byte[] data, String title, ReportService.ReportFormat format) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + hrReportService.fileName(title, format) + "\"")
            .contentType(MediaType.parseMediaType(hrReportService.contentType(format)))
            .body(data);
    }
}
