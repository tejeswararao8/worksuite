package com.platform.reporting;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Map;

@Component
public class CsvExporter {

    public byte[] export(ReportDefinition report) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {
            List<String> columns = report.getColumns();

            // Header
            writer.writeNext(columns.toArray(new String[0]));

            // Rows
            for (Map<String, Object> row : report.getRows()) {
                String[] line = columns.stream()
                    .map(col -> row.getOrDefault(col, "") != null ? row.get(col).toString() : "")
                    .toArray(String[]::new);
                writer.writeNext(line);
            }
        }
        return out.toByteArray();
    }
}
