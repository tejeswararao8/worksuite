package com.platform.reporting;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Describes what to export — columns, data, title.
 * Any module builds this and passes to ReportService.
 */
@Getter
@Builder
public class ReportDefinition {
    private String title;
    private List<String> columns;
    private List<Map<String, Object>> rows;
}
