package com.platform.hr.probation.service;

import com.platform.api.PagedResponse;
import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.probation.dto.ProbationDto;
import com.platform.hr.probation.entity.Probation;
import com.platform.hr.probation.entity.Probation.ProbationStatus;
import com.platform.hr.probation.repository.ProbationRepository;
import com.platform.notification.Notification;
import com.platform.notification.NotificationRequest;
import com.platform.notification.NotificationService;
import com.platform.security.user.UserPrincipal;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProbationService {

    private final ProbationRepository probationRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    // Called automatically when employee is created
    public ProbationDto.Response startProbation(UUID employeeId, LocalDate joiningDate, UUID companyId, int probationDays) {
        LocalDate endDate = joiningDate.plusDays(probationDays);

        Probation probation = Probation.builder()
            .employeeId(employeeId)
            .startDate(joiningDate)
            .endDate(endDate)
            .status(ProbationStatus.IN_PROGRESS)
            .build();
        probation.setCompanyId(companyId);
        return toResponse(probationRepository.save(probation));
    }

    @Transactional(readOnly = true)
    public ProbationDto.Response findByEmployee(UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return probationRepository
            .findByEmployeeIdAndStatusAndIsActiveTrue(employeeId, ProbationStatus.IN_PROGRESS)
            .map(this::toResponse)
            .orElseThrow(() -> PlatformException.notFound("Active Probation for this employee"));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProbationDto.Response> findAll(ProbationStatus status, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        if (status != null) {
            return PagedResponse.of(
                probationRepository.findByCompanyIdAndStatusAndIsActiveTrue(companyId, status, pageable)
                    .map(this::toResponse));
        }
        return PagedResponse.of(
            probationRepository.findByCompanyIdAndIsActiveTrue(companyId, pageable).map(this::toResponse));
    }

    @Auditable(entity = "Probation", action = AuditLog.AuditAction.UPDATE)
    public ProbationDto.Response confirm(UUID probationId, ProbationDto.ConfirmRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Probation probation = getActive(probationId, companyId);
        validateInProgress(probation);

        probation.setStatus(ProbationStatus.CONFIRMED);
        probation.setRemarks(request.getRemarks());
        probation.setApprovedBy(UserPrincipal.currentUserId());
        probation.setApprovedAt(LocalDateTime.now());
        probationRepository.save(probation);

        // Update employee status to ACTIVE
        Employee employee = getEmployee(probation.getEmployeeId(), companyId);
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employeeRepository.save(employee);

        notificationService.send(NotificationRequest.builder()
            .companyId(companyId)
            .recipientId(probation.getEmployeeId())
            .title("Probation Confirmed")
            .message("Congratulations! Your probation period has been successfully completed.")
            .type(Notification.NotificationType.WORKFLOW_APPROVAL)
            .referenceId(probationId.toString())
            .referenceType("PROBATION")
            .build());

        log.info("Probation confirmed for employee: {}", probation.getEmployeeId());
        return toResponse(probation);
    }

    @Auditable(entity = "Probation", action = AuditLog.AuditAction.UPDATE)
    public ProbationDto.Response extend(UUID probationId, ProbationDto.ExtendRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Probation probation = getActive(probationId, companyId);
        validateInProgress(probation);

        LocalDate currentEnd = probation.getExtendedEndDate() != null
            ? probation.getExtendedEndDate() : probation.getEndDate();
        if (!request.getNewEndDate().isAfter(currentEnd)) {
            throw PlatformException.badRequest("New end date must be after current end date: " + currentEnd);
        }

        probation.setStatus(ProbationStatus.EXTENDED);
        probation.setExtendedEndDate(request.getNewEndDate());
        probation.setRemarks(request.getReason());
        probationRepository.save(probation);

        notificationService.send(NotificationRequest.builder()
            .companyId(companyId)
            .recipientId(probation.getEmployeeId())
            .title("Probation Extended")
            .message("Your probation period has been extended until " + request.getNewEndDate())
            .type(Notification.NotificationType.WORKFLOW_APPROVAL)
            .referenceId(probationId.toString())
            .referenceType("PROBATION")
            .build());

        return toResponse(probation);
    }

    @Auditable(entity = "Probation", action = AuditLog.AuditAction.UPDATE)
    public ProbationDto.Response reject(UUID probationId, ProbationDto.RejectRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Probation probation = getActive(probationId, companyId);
        validateInProgress(probation);

        probation.setStatus(ProbationStatus.REJECTED);
        probation.setRemarks(request.getReason());
        probation.setApprovedBy(UserPrincipal.currentUserId());
        probation.setApprovedAt(LocalDateTime.now());
        probationRepository.save(probation);

        // Terminate employee
        Employee employee = getEmployee(probation.getEmployeeId(), companyId);
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);

        notificationService.send(NotificationRequest.builder()
            .companyId(companyId)
            .recipientId(probation.getEmployeeId())
            .title("Probation Not Confirmed")
            .message("Your probation period review has resulted in termination. Reason: " + request.getReason())
            .type(Notification.NotificationType.WORKFLOW_APPROVAL)
            .referenceId(probationId.toString())
            .referenceType("PROBATION")
            .build());

        log.info("Probation rejected for employee: {}", probation.getEmployeeId());
        return toResponse(probation);
    }

    // ── Scheduler: daily at 7am — send reminders ──────────────
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void sendProbationReminders() {
        // This runs for all tenants — in production use per-tenant scheduling
        LocalDate today = LocalDate.now();
        List<UUID> companyIds = probationRepository.findAll().stream()
            .map(p -> p.getCompanyId()).distinct().toList();

        for (UUID companyId : companyIds) {
            sendRemindersForCompany(companyId, today, 30);
            sendRemindersForCompany(companyId, today, 7);
        }
    }

    private void sendRemindersForCompany(UUID companyId, LocalDate today, int days) {
        LocalDate target = today.plusDays(days);
        List<Probation> ending = probationRepository.findEndingBetween(companyId, target, target);
        for (Probation p : ending) {
            notificationService.send(NotificationRequest.builder()
                .companyId(companyId)
                .recipientId(p.getEmployeeId())
                .title("Probation Ending in " + days + " Days")
                .message("Your probation period ends on " +
                    (p.getExtendedEndDate() != null ? p.getExtendedEndDate() : p.getEndDate()))
                .type(Notification.NotificationType.SYSTEM)
                .referenceId(p.getId().toString())
                .referenceType("PROBATION")
                .build());
        }
        if (!ending.isEmpty()) log.info("Sent {} probation reminders ({} days)", ending.size(), days);
    }

    private Probation getActive(UUID id, UUID companyId) {
        return probationRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Probation"));
    }

    private Employee getEmployee(UUID employeeId, UUID companyId) {
        return employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
    }

    private void validateInProgress(Probation probation) {
        if (probation.getStatus() != ProbationStatus.IN_PROGRESS
                && probation.getStatus() != ProbationStatus.EXTENDED) {
            throw PlatformException.badRequest("Probation is already " + probation.getStatus());
        }
    }

    private ProbationDto.Response toResponse(Probation p) {
        ProbationDto.Response r = new ProbationDto.Response();
        r.setId(p.getId()); r.setEmployeeId(p.getEmployeeId());
        r.setStartDate(p.getStartDate()); r.setEndDate(p.getEndDate());
        r.setExtendedEndDate(p.getExtendedEndDate()); r.setStatus(p.getStatus());
        r.setRemarks(p.getRemarks()); r.setApprovedBy(p.getApprovedBy());
        r.setApprovedAt(p.getApprovedAt());

        LocalDate effectiveEnd = p.getExtendedEndDate() != null ? p.getExtendedEndDate() : p.getEndDate();
        r.setDaysRemaining(Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), effectiveEnd)));

        employeeRepository.findById(p.getEmployeeId()).ifPresent(e -> {
            r.setEmployeeName(e.getFirstName() + " " + e.getLastName());
            r.setEmployeeCode(e.getEmployeeCode());
        });
        return r;
    }
}
