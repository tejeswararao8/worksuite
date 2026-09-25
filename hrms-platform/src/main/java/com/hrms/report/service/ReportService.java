package com.hrms.report.service;

import com.hrms.common.util.SecurityUtils;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final EmployeeRepository employeeRepository;

    public byte[] generateEmployeeReport(String format) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        List<Employee> employees = employeeRepository.findByCompanyIdAndActiveTrue(companyId);
        return switch (format.toUpperCase()) {
            case "CSV"   -> toEmployeeCsv(employees);
            case "EXCEL" -> toEmployeeExcel(employees);
            default      -> throw new com.hrms.common.exception.BusinessException("INVALID_FORMAT", "Supported formats: CSV, EXCEL");
        };
    }

    private byte[] toEmployeeCsv(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        sb.append("Employee Code,First Name,Last Name,Email,Mobile,Joining Date,Status,Employment Type\n");
        employees.forEach(e -> sb.append(String.join(",",
                nullSafe(e.getEmployeeCode()),
                nullSafe(e.getFirstName()),
                nullSafe(e.getLastName()),
                nullSafe(e.getEmail()),
                nullSafe(e.getMobile()),
                e.getJoiningDate() != null ? e.getJoiningDate().toString() : "",
                e.getEmploymentStatus() != null ? e.getEmploymentStatus().name() : "",
                e.getEmploymentType() != null ? e.getEmploymentType().name() : ""
        )).append("\n"));
        return sb.toString().getBytes();
    }

    private byte[] toEmployeeExcel(List<Employee> employees) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Employees");
            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            String[] headers = {"Employee Code", "First Name", "Last Name", "Email", "Mobile", "Joining Date", "Status", "Type"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Employee e : employees) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nullSafe(e.getEmployeeCode()));
                row.createCell(1).setCellValue(nullSafe(e.getFirstName()));
                row.createCell(2).setCellValue(nullSafe(e.getLastName()));
                row.createCell(3).setCellValue(nullSafe(e.getEmail()));
                row.createCell(4).setCellValue(nullSafe(e.getMobile()));
                row.createCell(5).setCellValue(e.getJoiningDate() != null ? e.getJoiningDate().toString() : "");
                row.createCell(6).setCellValue(e.getEmploymentStatus() != null ? e.getEmploymentStatus().name() : "");
                row.createCell(7).setCellValue(e.getEmploymentType() != null ? e.getEmploymentType().name() : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            log.error("Failed to generate Excel report", ex);
            throw new RuntimeException("Excel generation failed", ex);
        }
    }

    private String nullSafe(String val) {
        return val != null ? val.replace(",", " ") : "";
    }
}
