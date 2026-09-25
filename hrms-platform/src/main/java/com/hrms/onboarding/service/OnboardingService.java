package com.hrms.onboarding.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.onboarding.dto.OnboardingDto;
import com.hrms.onboarding.entity.OnboardingChecklist;
import com.hrms.onboarding.repository.OnboardingChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingService {

    private final OnboardingChecklistRepository checklistRepository;

    private static final List<String[]> DEFAULT_TASKS = List.of(
            new String[]{"ID Card Issuance",       "Issue employee ID card",              "true",  "1"},
            new String[]{"System Access Setup",    "Create email and system accounts",    "true",  "2"},
            new String[]{"Policy Acknowledgement", "Sign HR policies and NDA",            "true",  "3"},
            new String[]{"Workstation Setup",      "Assign desk, laptop, peripherals",    "false", "4"},
            new String[]{"Induction Training",     "Complete company induction program",  "true",  "5"},
            new String[]{"Bank Account Details",   "Submit bank account for payroll",     "true",  "6"}
    );

    public List<OnboardingDto.TaskResponse> initiate(OnboardingDto.InitiateRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        if (checklistRepository.existsByEmployeeIdAndCompanyId(req.getEmployeeId(), companyId))
            throw new BusinessException("ONBOARDING_EXISTS", "Onboarding already initiated for this employee");

        List<OnboardingChecklist> tasks = DEFAULT_TASKS.stream().map(t -> {
            OnboardingChecklist task = OnboardingChecklist.builder()
                    .employeeId(req.getEmployeeId())
                    .taskName(t[0])
                    .description(t[1])
                    .mandatory(Boolean.parseBoolean(t[2]))
                    .sequenceOrder(Integer.parseInt(t[3]))
                    .completed(false)
                    .build();
            task.setCompanyId(companyId);
            return task;
        }).toList();

        return checklistRepository.saveAll(tasks).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OnboardingDto.TaskResponse> getChecklist(UUID employeeId) {
        return checklistRepository.findByEmployeeIdAndCompanyIdOrderBySequenceOrder(
                employeeId, SecurityUtils.getCurrentCompanyId())
                .stream().map(this::toResponse).toList();
    }

    public OnboardingDto.TaskResponse completeTask(UUID employeeId, UUID taskId) {
        OnboardingChecklist task = findTask(employeeId, taskId);
        task.setCompleted(true);
        task.setCompletedBy(SecurityUtils.getCurrentUserId());
        task.setCompletedAt(LocalDateTime.now());
        return toResponse(checklistRepository.save(task));
    }

    public OnboardingDto.TaskResponse addCustomTask(UUID employeeId, OnboardingDto.AddTaskRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        OnboardingChecklist task = OnboardingChecklist.builder()
                .employeeId(employeeId)
                .taskName(req.getTaskName())
                .description(req.getDescription())
                .mandatory(req.isMandatory())
                .sequenceOrder(req.getSequenceOrder())
                .completed(false)
                .build();
        task.setCompanyId(companyId);
        return toResponse(checklistRepository.save(task));
    }

    private OnboardingChecklist findTask(UUID employeeId, UUID taskId) {
        return checklistRepository.findById(taskId)
                .filter(t -> t.getEmployeeId().equals(employeeId) && t.getCompanyId().equals(SecurityUtils.getCurrentCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    private OnboardingDto.TaskResponse toResponse(OnboardingChecklist t) {
        OnboardingDto.TaskResponse res = new OnboardingDto.TaskResponse();
        res.setId(t.getId());
        res.setTaskName(t.getTaskName());
        res.setDescription(t.getDescription());
        res.setMandatory(t.isMandatory());
        res.setCompleted(t.isCompleted());
        res.setCompletedBy(t.getCompletedBy());
        res.setCompletedAt(t.getCompletedAt());
        res.setSequenceOrder(t.getSequenceOrder());
        return res;
    }
}
