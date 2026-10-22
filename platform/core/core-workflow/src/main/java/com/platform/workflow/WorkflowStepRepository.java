package com.platform.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {
    List<WorkflowStep> findByInstanceIdOrderByStepNumber(UUID instanceId);
    List<WorkflowStep> findByActionAndDueAtBefore(WorkflowStep.StepAction action, LocalDateTime dateTime);
}
