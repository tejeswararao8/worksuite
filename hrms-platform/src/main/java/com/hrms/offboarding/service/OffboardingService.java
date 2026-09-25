package com.hrms.offboarding.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.repository.EmployeeRepository;
import com.hrms.offboarding.dto.OffboardingDto;
import com.hrms.offboarding.entity.OffboardingChecklist;
import com.hrms.offboarding.entity.OffboardingChecklist.OffboardingStatus;
import com.hrms.offboarding.repository.OffboardingChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OffboardingService {

    private final OffboardingChecklistRepository checklistRepository;
    private final EmployeeRepository employeeRepository;

    private static final List<String[]> DEFAULT_TASKS = List.of(
            new String[]{"Asset Return",           "Return all company assets",           "true"},
            new String[]{"Access Revocation",      "Revoke all system and building access","true"},
            new String[]{"Knowledge Transfer",     "Complete knowledge transfer document", "true"},
            new String[]{"Exit Interview",         "Complete exit interview with HR",      "false"},
            new String[]{"Final Settlement",       "Process final salary settlement",      "true"},
            new String[]{"Experience Letter",      "Issue experience letter",              "false"}
    );

    public List<OffboardingDto.TaskResponse> initiate(OffboardingDto.InitiateRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        if (checklistRepository.existsByEmployeeIdAndCompanyId(req.getEmployeeId(), companyId))
            throw new BusinessException("OFFBOARDING_EXISTS", "Offboarding already initiated for this employee");

        List<OffboardingChecklist> tasks = DEFAULT_TASKS.stream().map(t -> {
            OffboardingChecklist task = OffboardingChecklist.builder()
                    .employeeId(req.getEmployeeId())
                    .taskName(t[0])
                    .description(t[1])
                    .mandatory(Boolean.parseBoolean(t[2]))
                    .lastWorkingDate(req.getLastWorkingDate())
                    .exitReason(req.getExitReason())
                    .offboardingStatus(OffboardingStatus.IN_PROGRESS)
                    .completed(false)
                    .build();
            task.setCompanyId(companyId);
            return task;
        }).toList();

        return checklistRepository.saveAll(tasks).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OffboardingDto.TaskResponse> getChecklist(UUID employeeId) {
        return checklistRepository.findByEmployeeIdAndCompanyId(employeeId, SecurityUtils.getCurrentCompanyId())
                .stream().map(this::toResponse).toList();
    }

    public OffboardingDto.TaskResponse completeTask(UUID employeeId, UUID taskId) {
        OffboardingChecklist task = findTask(employeeId, taskId);
        task.setCompleted(true);
        task.setCompletedBy(SecurityUtils.getCurrentUserId());
        task.setCompletedAt(LocalDateTime.now());
        return toResponse(checklistRepository.save(task));
    }

    public void completeOffboarding(UUID employeeId) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        List<OffboardingChecklist> tasks = checklistRepository.findByEmployeeIdAndCompanyId(employeeId, companyId);
        boolean mandatoryPending = tasks.stream().anyMatch(t -> t.isMandatory() && !t.isCompleted());
        if (mandatoryPending)
            throw new BusinessException("PENDING_TASKS", "All mandatory offboarding tasks must be completed first");

        tasks.forEach(t -> t.setOffboardingStatus(OffboardingStatus.COMPLETED));
        checklistRepository.saveAll(tasks);

        employeeRepository.findByIdAndCompanyIdAndActiveTrue(employeeId, companyId).ifPresent(emp -> {
            emp.setEmploymentStatus(Employee.EmploymentStatus.RESIGNED);
            emp.setActive(false);
            employeeRepository.save(emp);
        });
    }

    private OffboardingChecklist findTask(UUID employeeId, UUID taskId) {
        return checklistRepository.findById(taskId)
                .filter(t -> t.getEmployeeId().equals(employeeId) && t.getCompanyId().equals(SecurityUtils.getCurrentCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    private OffboardingDto.TaskResponse toResponse(OffboardingChecklist t) {
        OffboardingDto.TaskResponse res = new OffboardingDto.TaskResponse();
        res.setId(t.getId());
        res.setTaskName(t.getTaskName());
        res.setDescription(t.getDescription());
        res.setMandatory(t.isMandatory());
        res.setCompleted(t.isCompleted());
        res.setCompletedBy(t.getCompletedBy());
        res.setCompletedAt(t.getCompletedAt());
        res.setLastWorkingDate(t.getLastWorkingDate());
        res.setExitReason(t.getExitReason());
        res.setOffboardingStatus(t.getOffboardingStatus());
        return res;
    }
}
