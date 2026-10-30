package com.platform.workflow;

import com.platform.api.PlatformException;
import com.platform.notification.Notification;
import com.platform.notification.NotificationRequest;
import com.platform.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowStepRepository stepRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final NotificationService notificationService;

    @Transactional
    public WorkflowInstance start(String entityType, UUID entityId, UUID initiatedBy, UUID companyId) {
        WorkflowDefinition definition = definitionRepository
            .findByEntityTypeAndCompanyId(entityType, companyId)
            .orElseThrow(() -> PlatformException.notFound("WorkflowDefinition for " + entityType));

        WorkflowInstance instance = WorkflowInstance.builder()
            .definition(definition)
            .entityId(entityId)
            .entityType(entityType)
            .status(WorkflowInstance.WorkflowStatus.PENDING)
            .currentStep(1)
            .initiatedBy(initiatedBy)
            .build();
        instance.setCompanyId(companyId);
        instanceRepository.save(instance);

        // Create first step
        WorkflowStep step = WorkflowStep.builder()
            .instance(instance)
            .stepNumber(1)
            .action(WorkflowStep.StepAction.PENDING)
            .dueAt(LocalDateTime.now().plusDays(definition.getEscalationDays()))
            .build();
        step.setCompanyId(companyId);
        stepRepository.save(step);

        log.info("Workflow started for {} {}", entityType, entityId);
        return instance;
    }

    @Transactional
    public WorkflowInstance approve(UUID instanceId, UUID approverId, String comments) {
        WorkflowInstance instance = getById(instanceId);
        List<WorkflowStep> steps = stepRepository.findByInstanceIdOrderByStepNumber(instanceId);

        // Find and complete current step
        WorkflowStep currentStep = steps.stream()
            .filter(s -> s.getStepNumber() == instance.getCurrentStep())
            .findFirst()
            .orElseThrow(() -> PlatformException.notFound("WorkflowStep"));

        currentStep.setAction(WorkflowStep.StepAction.APPROVED);
        currentStep.setApproverId(approverId);
        currentStep.setComments(comments);
        currentStep.setActedAt(LocalDateTime.now());
        stepRepository.save(currentStep);

        // Check if more steps exist
        int nextStepNumber = instance.getCurrentStep() + 1;
        boolean hasNextStep = steps.stream()
            .anyMatch(s -> s.getStepNumber() == nextStepNumber);

        if (hasNextStep) {
            // Move to next step
            instance.setCurrentStep(nextStepNumber);
            instance.setStatus(WorkflowInstance.WorkflowStatus.IN_PROGRESS);
            instanceRepository.save(instance);

            // Notify next approver
            WorkflowStep nextStep = steps.stream()
                .filter(s -> s.getStepNumber() == nextStepNumber)
                .findFirst().get();

            notificationService.send(NotificationRequest.builder()
                .companyId(instance.getCompanyId())
                .title("Approval Required")
                .message("Your approval is required for " + instance.getEntityType())
                .type(Notification.NotificationType.WORKFLOW_APPROVAL)
                .referenceId(instance.getId().toString())
                .referenceType("WORKFLOW")
                .build());

            log.info("Workflow {} moved to step {}", instanceId, nextStepNumber);
        } else {
            // All steps approved — final approval
            instance.setStatus(WorkflowInstance.WorkflowStatus.APPROVED);
            instanceRepository.save(instance);

            notificationService.send(NotificationRequest.builder()
                .companyId(instance.getCompanyId())
                .title("Request Approved")
                .message(instance.getEntityType() + " request has been fully approved.")
                .type(Notification.NotificationType.WORKFLOW_APPROVAL)
                .referenceId(instance.getId().toString())
                .referenceType("WORKFLOW")
                .build());

            log.info("Workflow {} fully approved", instanceId);
        }

        return instance;
    }

    @Transactional
    public WorkflowInstance reject(UUID instanceId, UUID approverId, String comments) {
        WorkflowInstance instance = getById(instanceId);
        instance.setStatus(WorkflowInstance.WorkflowStatus.REJECTED);
        instance.setComments(comments);
        instanceRepository.save(instance);
        log.info("Workflow {} rejected by {}", instanceId, approverId);
        return instance;
    }

    public WorkflowInstance getById(UUID id) {
        return instanceRepository.findById(id)
            .orElseThrow(() -> PlatformException.notFound("WorkflowInstance"));
    }

    // Auto-escalate overdue steps every hour
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void escalateOverdue() {
        List<WorkflowStep> overdue = stepRepository.findByActionAndDueAtBefore(
            WorkflowStep.StepAction.PENDING, LocalDateTime.now());

        for (WorkflowStep step : overdue) {
            step.setAction(WorkflowStep.StepAction.ESCALATED);
            stepRepository.save(step);

            WorkflowInstance instance = step.getInstance();
            instance.setStatus(WorkflowInstance.WorkflowStatus.ESCALATED);
            instanceRepository.save(instance);

            notificationService.send(NotificationRequest.builder()
                .companyId(instance.getCompanyId())
                .title("Workflow Escalated")
                .message("Approval for " + instance.getEntityType() + " has been escalated due to no action.")
                .type(Notification.NotificationType.WORKFLOW_APPROVAL)
                .referenceId(instance.getId().toString())
                .referenceType("WORKFLOW")
                .build());
        }

        if (!overdue.isEmpty()) log.info("Escalated {} overdue workflow steps", overdue.size());
    }
}
