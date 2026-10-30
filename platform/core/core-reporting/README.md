# core-reporting

**One-line export to Excel, CSV, or PDF for any data in any module.**

## What It Does

- `ReportService` — single entry point: `export(definition, format)`
- `ReportDefinition` — data container: title, column headers, rows as `List<Map<String, Object>>`
- `ExcelExporter` — Apache POI, produces `.xlsx`
- `CsvExporter` — OpenCSV, produces `.csv`
- `PdfExporter` — Apache PDFBox (Apache 2.0), produces `.pdf` with styled header row and alternating row colors

## Package

```
com.platform.reporting
```

## Files

| File | Purpose |
|---|---|
| `ReportDefinition.java` | Data container: title, columns, rows |
| `ReportService.java` | Entry point — delegates to correct exporter |
| `ExcelExporter.java` | Apache POI `.xlsx` export |
| `CsvExporter.java` | OpenCSV `.csv` export |
| `PdfExporter.java` | Apache PDFBox `.pdf` export |

---

## How to Use — Build and Export

```java
@Service
@RequiredArgsConstructor
public class EmployeeReportService {

    private final EmployeeRepository employeeRepository;
    private final ReportService reportService;

    public byte[] exportEmployees(ReportService.ReportFormat format) throws IOException {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        List<Employee> employees = employeeRepository.findByCompanyId(companyId);

        // Define column headers
        List<String> columns = List.of(
            "Employee Code", "Full Name", "Email",
            "Department", "Designation", "Status", "Joining Date"
        );

        // Map each employee to a row (Map key = column header)
        List<Map<String, Object>> rows = employees.stream()
            .map(e -> Map.of(
                "Employee Code", e.getEmployeeCode(),
                "Full Name",     e.getFirstName() + " " + e.getLastName(),
                "Email",         e.getEmail(),
                "Department",    e.getDepartment() != null ? e.getDepartment().getName() : "",
                "Designation",   e.getDesignation() != null ? e.getDesignation().getTitle() : "",
                "Status",        e.getStatus().name(),
                "Joining Date",  e.getJoiningDate() != null ? e.getJoiningDate().toString() : ""
            ))
            .toList();

        ReportDefinition definition = ReportDefinition.builder()
            .title("Employee Report")
            .columns(columns)
            .rows(rows)
            .build();

        return reportService.export(definition, format);
    }
}
```

---

## Return as File Download from Controller

```java
@GetMapping("/export")
public ResponseEntity<byte[]> export(
        @RequestParam(defaultValue = "EXCEL") ReportService.ReportFormat format)
        throws IOException {

    byte[] data = employeeReportService.exportEmployees(format);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + reportService.fileName("Employee Report", format) + "\"")
        .contentType(MediaType.parseMediaType(reportService.contentType(format)))
        .body(data);
}
```

---

## ReportFormat Values

| Format | Extension | Content-Type |
|---|---|---|
| `EXCEL` | `.xlsx` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |
| `CSV` | `.csv` | `text/csv` |
| `PDF` | `.pdf` | `application/pdf` |

---

## Helper Methods

```java
// Get MIME type for Content-Type header
reportService.contentType(ReportService.ReportFormat.EXCEL);
// → "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

// Get filename with correct extension
reportService.fileName("Employee Report", ReportService.ReportFormat.PDF);
// → "employee_report.pdf"
```

---

## ReportDefinition Fields

| Field | Type | Description |
|---|---|---|
| `title` | `String` | Report title — shown in PDF header, Excel sheet name |
| `columns` | `List<String>` | Column header names — order matters |
| `rows` | `List<Map<String, Object>>` | Data rows — map key must match column header |

---

## PDF Output Style (Apache PDFBox)

- A4 page size
- Blue header row with white bold text
- Alternating light grey rows for readability
- Title displayed above the table
- Long text auto-truncated with `…`
- Auto-adds new page when content overflows

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-notification</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
<dependency><groupId>org.apache.poi</groupId><artifactId>poi-ooxml</artifactId></dependency>
<dependency><groupId>com.opencsv</groupId><artifactId>opencsv</artifactId></dependency>
<dependency><groupId>org.apache.pdfbox</groupId><artifactId>pdfbox</artifactId></dependency>
```
