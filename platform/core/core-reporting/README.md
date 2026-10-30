# core-reporting

**One-line export to Excel, CSV, or PDF for any data in any module.**

## What It Does

- `ReportService` — single entry point: `export(definition, format)`
- `ReportDefinition` — data container: title, column headers, rows
- `ExcelExporter` — Apache POI, produces `.xlsx`
- `CsvExporter` — OpenCSV, produces `.csv`
- `PdfExporter` — iText7, produces `.pdf`

## Package

```
com.platform.reporting
```

## How to Use in Your Module

### 1. Build a ReportDefinition from your data

```java
@Service
@RequiredArgsConstructor
public class EmployeeReportService {

    private final EmployeeRepository employeeRepository;
    private final ReportService reportService;

    public byte[] exportEmployees(ReportService.ReportFormat format) throws IOException {
        List<Employee> employees = employeeRepository
            .findByCompanyId(UUID.fromString(TenantContext.getTenantId()));

        // Define columns
        List<String> columns = List.of(
            "Employee Code", "Full Name", "Email", "Department",
            "Designation", "Status", "Joining Date"
        );

        // Map rows
        List<List<String>> rows = employees.stream()
            .map(e -> List.of(
                e.getEmployeeCode(),
                e.getFirstName() + " " + e.getLastName(),
                e.getEmail(),
                e.getDepartment() != null ? e.getDepartment().getName() : "",
                e.getDesignation() != null ? e.getDesignation().getTitle() : "",
                e.getStatus().name(),
                e.getJoiningDate() != null ? e.getJoiningDate().toString() : ""
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

### 2. Return from controller as file download

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

## ReportFormat Values

```java
ReportService.ReportFormat.EXCEL   // → .xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
ReportService.ReportFormat.CSV     // → .csv,  text/csv
ReportService.ReportFormat.PDF     // → .pdf,  application/pdf
```

## Helper Methods

```java
// Get MIME type for Content-Type header
reportService.contentType(ReportFormat.EXCEL);
// → "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

// Get filename with extension
reportService.fileName("Employee Report", ReportFormat.EXCEL);
// → "employee_report.xlsx"
```

## ReportDefinition Fields

| Field | Type | Description |
|---|---|---|
| `title` | String | Report title (shown in Excel header, PDF title) |
| `columns` | List\<String\> | Column header names |
| `rows` | List\<List\<String\>\> | Data rows — each inner list is one row |

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>org.apache.poi</groupId><artifactId>poi-ooxml</artifactId></dependency>
<dependency><groupId>com.opencsv</groupId><artifactId>opencsv</artifactId></dependency>
<dependency><groupId>com.itextpdf</groupId><artifactId>itext7-core</artifactId><type>pom</type></dependency>
```
