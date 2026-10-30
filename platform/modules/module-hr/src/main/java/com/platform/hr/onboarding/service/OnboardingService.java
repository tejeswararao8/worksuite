package com.platform.hr.onboarding.service;

import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.onboarding.dto.OnboardingDto;
import com.platform.hr.onboarding.entity.OnboardingChecklist;
import com.platform.hr.onboarding.entity.OnboardingChecklist.TaskStatus;
import com.platform.hr.onboarding.repository.OnboardingRepository;
import com.platform.notification.Notification;
import com.platform.notification.NotificationRequest;
import com.platform.notification.NotificationService;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    // Default onboarding tasks — auto-created when employee joins
    private static final List<String[]> DEFAULT_TASKS = List.of(
        new String[]{"IT Setup - Laptop & Equipment",       "IT",       "3"},
        new String[]{"Email & System Access Setup",         "IT",       "1"},
        new String[]{"ID Card & Access Card Issuance",      "HR",       "3"},
        new String[]{"HR Documentation & Contract Signing", "HR",       "1"},
        new String[]{"Payroll & Bank Details Submission",   "FINANCE",  "5"},
        new String[]{"Office Tour & Introduction",          "HR",       "1"},
        new String[]{"Department Induction",                "MANAGER",  "7"},
        new String[]{"Health Insurance Enrollment",         "HR",       "14"},
        new String[]{"Company Policy Acknowledgement",      "HR",       "3"},
        new String[]{"Workstation Setup",                   "IT",       "2"}
    );

    // Called automatically when employee is created
    public void createDefaultTasks(UUID employeeId, LocalDate joiningDate, UUID companyId) {
        for (String[] task : DEFAULT_TASKS) {
            int dueDays = Integer.parseInt(task[2]);
            OnboardingChecklist checklist = OnboardingChecklist.builder()
                .employeeId(employeeId)
                .taskName(task[0])
                .description("Assigned to: " + task[1])
                .status(TaskStatus.PENDING)
                .dueDate(joiningDate.plusDays(dueDays))
                .build();
            checklist.setCompanyId(companyId);
            onboardingRepository.save(checklist);
        }
        log.info("Created {} onboarding tasks for employee: {}", DEFAULT_TASKS.size(), employeeId);
    }

    @Auditable(entity = "OnboardingTask", action = AuditLog.AuditAction.CREATE)
    public OnboardingDto.Response addTask(UUID employeeId, OnboardingDto.CreateTaskRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        validateEmployee(employeeId, companyId);

        OnboardingChecklist task = OnboardingChecklist.builder()
            .employeeId(employeeId)
            .taskName(request.getTaskName())
            .description(request.getDescription())
            .assignedTo(request.getAssignedTo())
            .dueDate(request.getDueDate())
            .status(TaskStatus.PENDING)
            .build();
        task.setCompanyId(companyId);
        OnboardingChecklist saved = onboardingRepository.save(task);

        if (request.getAssignedTo() != null) {
            notifyAssignee(request.getAssignedTo(), employeeId, request.getTaskName(), companyId);
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OnboardingDto.Response> findByEmployee(UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        validateEmployee(employeeId, companyId);
        return onboardingRepository.findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OnboardingDto.Response> findMyAssignedTasks() {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        UUID userId = com.platform.security.user.UserPrincipal.currentUserId();
        return onboardingRepository.findByAssignedToAndCompanyIdAndIsActiveTrue(userId, companyId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OnboardingDto.ProgressResponse getProgress(UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        validateEmployee(employeeId, companyId);

        long total = onboardingRepository.countByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId);
        long completed = onboardingRepository.countByEmployeeIdAndCompanyIdAndStatusAndIsActiveTrue(
            employeeId, companyId, TaskStatus.COMPLETED);
        long overdue = onboardingRepository.findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED
                && t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now())).count();

        OnboardingDto.ProgressResponse progress = new OnboardingDto.ProgressResponse();
        progress.setEmployeeId(employeeId);
        progress.setTotalTasks((int) total);
        progress.setCompletedTasks((int) completed);
        progress.setPendingTasks((int) (total - completed));
        progress.setOverdueTasks((int) overdue);
        progress.setCompletionPercentage(total > 0 ? (double) completed / total * 100 : 0);

        employeeRepository.findById(employeeId).ifPresent(e ->
            progress.setEmployeeName(e.getFirstName() + " " + e.getLastName()));
        return progress;
    }

    @Auditable(entity = "OnboardingTask", action = AuditLog.AuditAction.UPDATE)
    public OnboardingDto.Response completeTask(UUID taskId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        OnboardingChecklist task = onboardingRepository.findByIdAndCompanyIdAndIsActiveTrue(taskId, companyId)
            .orElseThrow(() -> PlatformException.notFound("OnboardingTask"));
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDate.now());
        return toResponse(onboardingRepository.save(task));
    }

    @Auditable(entity = "OnboardingTask", action = AuditLog.AuditAction.UPDATE)
    public OnboardingDto.Response updateTask(UUID taskId, OnboardingDto.UpdateTaskRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        OnboardingChecklist task = onboardingRepository.findByIdAndCompanyIdAndIsActiveTrue(taskId, companyId)
            .orElseThrow(() -> PlatformException.notFound("OnboardingTask"));
        if (request.getTaskName() != null) task.setTaskName(request.getTaskName());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getAssignedTo() != null) task.setAssignedTo(request.getAssignedTo());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.COMPLETED) task.setCompletedAt(LocalDate.now());
        }
        return toResponse(onboardingRepository.save(task));
    }

    // ── Scheduler: daily at 8am — remind overdue tasks ────────
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void remindOverdueTasks() {
        List<UUID> companyIds = onboardingRepository.findAll().stream()
            .map(t -> t.getCompanyId()).distinct().toList();

        for (UUID companyId : companyIds) {
            List<OnboardingChecklist> overdue = onboardingRepository
                .findOverdueTasks(companyId, LocalDate.now());
            for (OnboardingChecklist task : overdue) {
                if (task.getAssignedTo() != null) {
                    notificationService.send(NotificationRequest.builder()
                        .companyId(companyId)
                        .recipientId(task.getAssignedTo())
                        .title("Overdue Onboarding Task")
                        .message("Task '" + task.getTaskName() + "' is overdue. Please complete it.")
                        .type(Notification.NotificationType.ONBOARDING_TASK)
                        .referenceId(task.getId().toString())
                        .referenceType("ONBOARDING")
                        .build());
                }
            }
            if (!overdue.isEmpty()) log.info("Sent {} overdue onboarding reminders", overdue.size());
        }
    }

    private void notifyAssignee(UUID assigneeId, UUID employeeId, String taskName, UUID companyId) {
        notificationService.send(NotificationRequest.builder()
            .companyId(companyId)
            .recipientId(assigneeId)
            .title("New Onboarding Task Assigned")
            .message("You have been assigned onboarding task: " + taskName)
            .type(Notification.NotificationType.ONBOARDING_TASK)
            .referenceId(employeeId.toString())
            .referenceType("ONBOARDING")
            .build());
    }

    private void validateEmployee(UUID employeeId, UUID companyId) {
        employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
    }

    private OnboardingDto.Response toResponse(OnboardingChecklist t) {
        OnboardingDto.Response r = new OnboardingDto.Response();
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
