package com.platform.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {
    Optional<WorkflowInstance> findByEntityIdAndEntityTypeAndStatus(UUID entityId, String entityType, WorkflowInstance.WorkflowStatus status);
    List<WorkflowInstance> findByStatusIn(List<WorkflowInstance.WorkflowStatus> statuses);
}
