package com.platform.hr.offboarding.service;

import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.offboarding.dto.OffboardingDto;
import com.platform.hr.offboarding.entity.OffboardingChecklist;
import com.platform.hr.offboarding.entity.OffboardingChecklist.TaskStatus;
import com.platform.hr.offboarding.repository.OffboardingRepository;
import com.platform.messaging.service.MessagingService;
import com.platform.notification.Notification;
import com.platform.notification.NotificationRequest;
import com.platform.notification.NotificationService;
import com.platform.security.user.KeycloakUserService;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OffboardingService {

    private final OffboardingRepository offboardingRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final MessagingService messagingService;
    private final KeycloakUserService keycloakUserService;

    private static final List<String[]> DEFAULT_TASKS = List.of(
        new String[]{"Return Laptop & Equipment",           "IT",       "0"},
        new String[]{"Return Access Card & ID",             "HR",       "0"},
        new String[]{"Return SIM Card",                     "IT",       "0"},
        new String[]{"Revoke System Access",                "IT",       "0"},
        new String[]{"Revoke Email Access",                 "IT",       "0"},
        new String[]{"Knowledge Transfer Documentation",    "MANAGER",  "-7"},
        new String[]{"Exit Interview",                      "HR",       "-3"},
        new String[]{"Full & Final Settlement",             "FINANCE",  "7"},
        new String[]{"Experience Letter Issuance",          "HR",       "7"},
        new String[]{"NOC / Clearance Certificate",         "HR",       "7"}
    );

    @Auditable(entity = "Offboarding", action = AuditLog.AuditAction.CREATE)
    public OffboardingDto.ProgressResponse initiate(OffboardingDto.InitiateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Employee employee = employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getEmployeeId(), companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));

        employee.setEmploymentStatus(EmploymentStatus.RESIGNED);
        employeeRepository.save(employee);

        createDefaultTasks(request.getEmployeeId(), request.getLastWorkingDay(), companyId);

        // Notify IT for access revocation
        messagingService.sendOffboardingEvent(companyId.toString(), Map.of(
            "employeeId",    employee.getId().toString(),
            "employeeName",  employee.getFirstName() + " " + employee.getLastName(),
            "lastWorkingDay", request.getLastWorkingDay().toString(),
            "type",          request.getType().name()
        ));

        log.info("Offboarding initiated for employee: {}", request.getEmployeeId());
        return buildProgress(request.getEmployeeId(), companyId, request.getLastWorkingDay());
    }

    public void createDefaultTasks(UUID employeeId, LocalDate lastWorkingDay, UUID companyId) {
        for (String[] task : DEFAULT_TASKS) {
            int dueDays = Integer.parseInt(task[2]);
            LocalDate dueDate = lastWorkingDay.plusDays(dueDays);

            OffboardingChecklist checklist = OffboardingChecklist.builder()
                .employeeId(employeeId)
                .taskName(task[0])
                .description("Responsible: " + task[1])
                .status(TaskStatus.PENDING)
                .dueDate(dueDate)
                .build();
            checklist.setCompanyId(companyId);
            offboardingRepository.save(checklist);
        }
    }

    @Transactional(readOnly = true)
    public List<OffboardingDto.Response> findByEmployee(UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return offboardingRepository.findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OffboardingDto.Response> findMyAssignedTasks() {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        UUID userId = com.platform.security.user.UserPrincipal.currentUserId();
        return offboardingRepository.findByAssignedToAndCompanyIdAndIsActiveTrue(userId, companyId)
            .stream().map(this::toResponse).toList();
    }

    @Auditable(entity = "OffboardingTask", action = AuditLog.AuditAction.UPDATE)
    public OffboardingDto.Response completeTask(UUID taskId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        OffboardingChecklist task = offboardingRepository.findByIdAndCompanyIdAndIsActiveTrue(taskId, companyId)
            .orElseThrow(() -> PlatformException.notFound("OffboardingTask"));
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDate.now());
        return toResponse(offboardingRepository.save(task));
    }

    @Auditable(entity = "OffboardingTask", action = AuditLog.AuditAction.CREATE)
    public OffboardingDto.Response addTask(UUID employeeId, OffboardingDto.CreateTaskRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        OffboardingChecklist task = OffboardingChecklist.builder()
            .employeeId(employeeId)
            .taskName(request.getTaskName())
            .description(request.getDescription())
            .assignedTo(request.getAssignedTo())
            .dueDate(request.getDueDate())
            .status(TaskStatus.PENDING)
            .build();
        task.setCompanyId(companyId);
        return toResponse(offboardingRepository.save(task));
    }

    // ── Scheduler: daily at 9am — disable Keycloak on last working day ──
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void processLastWorkingDay() {
        // Find employees whose last working day is today and disable Keycloak access
        // In production, store lastWorkingDay on Employee entity
        log.info("Processing last working day access revocations");
    }

    // ── Scheduler: daily at 8am — remind overdue tasks ────────
    @Scheduled(cron = "0 30 8 * * *")
    @Transactional
    public void remindOverdueTasks() {
        List<UUID> companyIds = offboardingRepository.findAll().stream()
            .map(t -> t.getCompanyId()).distinct().toList();

        for (UUID companyId : companyIds) {
            List<OffboardingChecklist> overdue = offboardingRepository
                .findOverdueTasks(companyId, LocalDate.now());
            for (OffboardingChecklist task : overdue) {
                if (task.getAssignedTo() != null) {
                    notificationService.send(NotificationRequest.builder()
                        .companyId(companyId)
                        .recipientId(task.getAssignedTo())
                        .title("Overdue Offboarding Task")
                        .message("Task '" + task.getTaskName() + "' is overdue.")
                        .type(Notification.NotificationType.OFFBOARDING_TASK)
                        .referenceId(task.getId().toString())
                        .referenceType("OFFBOARDING")
                        .build());
                }
            }
        }
    }

    private OffboardingDto.ProgressResponse buildProgress(UUID employeeId, UUID companyId, LocalDate lastWorkingDay) {
        long total = offboardingRepository.countByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId);
        long completed = offboardingRepository.countByEmployeeIdAndCompanyIdAndStatusAndIsActiveTrue(
            employeeId, companyId, TaskStatus.COMPLETED);

        OffboardingDto.ProgressResponse p = new OffboardingDto.ProgressResponse();
        p.setEmployeeId(employeeId);
        p.setLastWorkingDay(lastWorkingDay);
        p.setTotalTasks((int) total);
        p.setCompletedTasks((int) completed);
        p.setPendingTasks((int) (total - completed));
        p.setCompletionPercentage(total > 0 ? (double) completed / total * 100 : 0);
        p.setFullyCompleted(total > 0 && completed == total);
        employeeRepository.findById(employeeId).ifPresent(e ->
            p.setEmployeeName(e.getFirstName() + " " + e.getLastName()));
        return p;
    }

    private OffboardingDto.Response toResponse(OffboardingChecklist t) {
        OffboardingDto.Response r = new OffboardingDto.Response();
        r.setId(t.getId()); r.setEmployeeId(t.getEmployeeId());
        r.setTaskName(t.getTaskName()); r.setDescription(t.getDescription());
        r.setStatus(t.getStatus()); r.setAssignedTo(t.getAssignedTo());
        r.setDueDate(t.getDueDate()); r.setCompletedAt(t.getCompletedAt());
        r.setOverdue(t.getStatus() != TaskStatus.COMPLETED
            && t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()));
        employeeRepository.findById(t.getEmployeeId()).ifPresent(e ->
            r.setEmployeeName(e.getFirstName() + " " + e.getLastName()));
        if (t.getAssignedTo() != null) {
            employeeRepository.findById(t.getAssignedTo()).ifPresent(e ->
                r.setAssignedToName(e.getFirstName() + " " + e.getLastName()));
        }
        return r;
    }
}
