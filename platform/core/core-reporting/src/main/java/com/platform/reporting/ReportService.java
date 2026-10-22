package com.platform.reporting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Entry point for all report exports.
 * Any module calls ReportService.export(definition, format) — done.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExcelExporter excelExporter;
    private final CsvExporter csvExporter;
    private final PdfExporter pdfExporter;

    public byte[] export(ReportDefinition definition, ReportFormat format) throws IOException {
        return switch (format) {
            case EXCEL -> excelExporter.export(definition);
            case CSV   -> csvExporter.export(definition);
            case PDF   -> pdfExporter.export(definition);
        };
    }

    public String contentType(ReportFormat format) {
        return switch (format) {
            case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case CSV   -> "text/csv";
            case PDF   -> "application/pdf";
        };
    }

    public String fileName(String title, ReportFormat format) {
        String ext = switch (format) {
            case EXCEL -> ".xlsx";
            case CSV   -> ".csv";
            case PDF   -> ".pdf";
        };
        return title.toLowerCase().replace(" ", "_") + ext;
    }

    public enum ReportFormat {
        EXCEL, CSV, PDF
    }
}
