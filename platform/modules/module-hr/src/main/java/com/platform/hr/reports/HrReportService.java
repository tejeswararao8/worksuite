package com.platform.hr.reports;

import com.platform.hr.asset.entity.Asset.AssetStatus;
import com.platform.hr.asset.repository.AssetRepository;
import com.platform.hr.branch.repository.BranchRepository;
import com.platform.hr.department.repository.DepartmentRepository;
import com.platform.hr.designation.repository.DesignationRepository;
import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.probation.entity.Probation;
import com.platform.hr.probation.entity.Probation.ProbationStatus;
import com.platform.hr.probation.repository.ProbationRepository;
import com.platform.hr.promotion.entity.Promotion;
import com.platform.hr.promotion.repository.PromotionRepository;
import com.platform.hr.transfer.entity.Transfer;
import com.platform.hr.transfer.repository.TransferRepository;
import com.platform.reporting.ReportDefinition;
import com.platform.reporting.ReportService;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HrReportService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final DesignationRepository designationRepository;
    private final ProbationRepository probationRepository;
    private final TransferRepository transferRepository;
    private final PromotionRepository promotionRepository;
    private final AssetRepository assetRepository;
    private final ReportService reportService;

    // ── Employee Master Report ─────────────────────────────────
    public byte[] employeeMasterReport(ReportService.ReportFormat format) throws IOException {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        List<Employee> employees = employeeRepository
            .findByCompanyIdAndIsActiveTrue(companyId, Pageable.unpaged()).getContent();

        List<Map<String, Object>> rows = employees.stream().map(e -> Map.<String, Object>of(
            "Code",            e.getEmployeeCode(),
            "First Name",      e.getFirstName(),
            "Last Name",       e.getLastName(),
            "Email",           e.getEmail(),
            "Mobile",          e.getMobile() != null ? e.getMobile() : "",
            "Gender",          e.getGender() != null ? e.getGender().name() : "",
            "Nationality",     e.getNationality() != null ? e.getNationality() : "",
            "Joining Date",    e.getJoiningDate().toString(),
            "Status",          e.getEmploymentStatus().name(),
            "Employment Type", e.getEmploymentType() != null ? e.getEmploymentType().name() : ""
        )).toList();

        return reportService.export(ReportDefinition.builder()
            .title("Employee Master Report")
            .columns(List.of("Code", "First Name", "Last Name", "Email", "Mobile",
                "Gender", "Nationality", "Joining Date", "Status", "Employment Type"))
            .rows(rows).build(), format);
    }

    // ── New Joiners Report ─────────────────────────────────────
    public byte[] newJoinersReport(LocalDate from, LocalDate to, ReportService.ReportFormat format) throws IOException {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        List<Employee> employees = employeeRepository
            .findByCompanyIdAndIsActiveTrue(companyId, Pageable.unpaged()).getContent()
            .stream().filter(e -> !e.getJoiningDate().isBefore(from) && !e.getJoiningDate().isAfter(to))
            .toList();

        List<Map<String, Object>> rows = employees.stream().map(e -> Map.<String, Object>of(
            "Code",         e.getEmployeeCode(),
            "Name",         e.getFirstName() + " " + e.getLastName(),
            "Email",        e.getEmail(),
            "Joining Date", e.getJoiningDate().toString(),
            "Status",       e.getEmploymentStatus().name()
        )).toList();

        return reportService.export(ReportDefinition.builder()
            .title("New Joiners Report (" + from + " to " + to + ")")
            .columns(List.of("Code", "Name", "Email", "Joining Date", "Status"))
            .rows(rows).build(), format);
    }

    // ── Probation Status Report ────────────────────────────────
    public byte[] probationReport(ReportService.ReportFormat format) throws IOException {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        List<Probation> probations = probationRepository
            .findByCompanyIdAndStatusAndIsActiveTrue(companyId, ProbationStatus.IN_PROGRESS, Pageable.unpaged())
            .getContent();

        List<Map<String, Object>> rows = probations.stream().map(p -> {
            String empName = employeeRepository.findById(p.getEmployeeId())
                .map(e -> e.getFirstName() + " " + e.getLastName()).orElse("");
            String empCode = employeeRepository.findById(p.getEmployeeId())
                .map(Employee::getEmployeeCode).orElse("");
            LocalDate end = p.getExtendedEndDate() != null ? p.getExtendedEndDate() : p.getEndDate();
            return Map.<String, Object>of(
                "Code",       empCode,
                "Name",       empName,
                "Start Date", p.getStartDate().toString(),
                "End Date",   end.toString(),
                "Status",     p.getStatus().name()
            );
        }).toList();

        return reportService.export(ReportDefinition.builder()
            .title("Probation Status Report")
            .columns(List.of("Code", "Name", "Start Date", "End Date", "Status"))
            .rows(rows).build(), format);
    }

    // ── Transfer History Report ────────────────────────────────
    public byte[] transferReport(LocalDate from, LocalDate to, ReportService.ReportFormat format) throws IOException {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        List<Transfer> transfers = transferRepository
            .findByCompanyIdAndIsActiveTrue(companyId, Pageable.unpaged()).getContent()
            .stream().filter(t -> !t.getEffectiveDate().isBefore(from) && !t.getEffectiveDate().isAfter(to))
            .toList();

        List<Map<String, Object>> rows = transfers.stream().map(t -> {
            String empName = employeeRepository.findById(t.getEmployeeId())
                .map(e -> e.getFirstName() + " " + e.getLastName()).orElse("");
            return Map.<String, Object>of(
                "Employee",       empName,
                "Type",           t.getTransferType().name(),
                "Effective Date", t.getEffectiveDate().toString(),
                "Status",         t.getStatus().name(),
                "Reason",         t.getReason() != null ? t.getReason() : ""
            );
        }).toList();

        return reportService.export(ReportDefinition.builder()
            .title("Transfer Report (" + from + " to " + to + ")")
            .columns(List.of("Employee", "Type", "Effective Date", "Status", "Reason"))
            .rows(rows).build(), format);
    }

    // ── Promotion History Report ───────────────────────────────
    public byte[] promotionReport(LocalDate from, LocalDate to, ReportService.ReportFormat format) throws IOException {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        List<Promotion> promotions = promotionRepository
            .findByCompanyIdAndIsActiveTrue(companyId, Pageable.unpaged()).getContent()
            .stream().filter(p -> !p.getEffectiveDate().isBefore(from) && !p.getEffectiveDate().isAfter(to))
            .toList();

        List<Map<String, Object>> rows = promotions.stream().map(p -> {
            String empName = employeeRepository.findById(p.getEmployeeId())
                .map(e -> e.getFirstName() + " " + e.getLastName()).orElse("");
            String fromDesig = p.getFromDesignationId() != null
                ? designationRepository.findById(p.getFromDesignationId()).map(d -> d.getTitle()).orElse("") : "";
            String toDesig = designationRepository.findById(p.getToDesignationId())
                .map(d -> d.getTitle()).orElse("");
            return Map.<String, Object>of(
                "Employee",       empName,
                "From",           fromDesig,
                "To",             toDesig,
                "Effective Date", p.getEffectiveDate().toString(),
                "Status",         p.getStatus().name()
            );
        }).toList();

        return reportService.export(ReportDefinition.builder()
            .title("Promotion Report (" + from + " to " + to + ")")
            .columns(List.of("Employee", "From", "To", "Effective Date", "Status"))
            .rows(rows).build(), format);
    }

    // ── Asset Assignment Report ────────────────────────────────
    public byte[] assetReport(ReportService.ReportFormat format) throws IOException {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        var assets = assetRepository.search(companyId, null, AssetStatus.ASSIGNED, null, Pageable.unpaged());

        List<Map<String, Object>> rows = assets.getContent().stream().map(a -> {
            String empName = a.getAssignedToEmployeeId() != null
                ? employeeRepository.findById(a.getAssignedToEmployeeId())
                    .map(e -> e.getFirstName() + " " + e.getLastName()).orElse("") : "";
            return Map.<String, Object>of(
                "Asset Code",    a.getAssetCode(),
                "Name",          a.getName(),
                "Type",          a.getAssetType().name(),
                "Serial Number", a.getSerialNumber() != null ? a.getSerialNumber() : "",
                "Assigned To",   empName,
                "Assigned At",   a.getAssignedAt() != null ? a.getAssignedAt().toLocalDate().toString() : ""
            );
        }).toList();

        return reportService.export(ReportDefinition.builder()
            .title("Asset Assignment Report")
            .columns(List.of("Asset Code", "Name", "Type", "Serial Number", "Assigned To", "Assigned At"))
            .rows(rows).build(), format);
    }

    public String contentType(ReportService.ReportFormat format) {
        return reportService.contentType(format);
    }

    public String fileName(String title, ReportService.ReportFormat format) {
        return reportService.fileName(title, format);
    }
}
