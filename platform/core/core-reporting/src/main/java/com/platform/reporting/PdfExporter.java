package com.platform.reporting;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Component
public class PdfExporter {

    public byte[] export(ReportDefinition report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);

        try (Document document = new Document(pdf)) {
            document.add(new Paragraph(report.getTitle())
                .setBold().setFontSize(16).setMarginBottom(10));

            List<String> columns = report.getColumns();
            Table table = new Table(UnitValue.createPercentArray(columns.size())).useAllAvailableWidth();

            // Header
            columns.forEach(col -> table.addHeaderCell(
                new Cell().add(new Paragraph(col).setBold())));

            // Rows
            for (Map<String, Object> row : report.getRows()) {
                columns.forEach(col -> {
                    Object val = row.getOrDefault(col, "");
                    table.addCell(new Cell().add(new Paragraph(val != null ? val.toString() : "")));
                });
            }

            document.add(table);
        }

        return out.toByteArray();
    }
}
