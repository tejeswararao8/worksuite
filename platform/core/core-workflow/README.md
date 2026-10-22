# core-workflow

**Generic approval workflow engine for any entity in any module.**

## What It Does

- `WorkflowDefinition` — defines the approval process for an entity type (e.g. TRANSFER, PROMOTION)
- `WorkflowInstance` — a running approval process for a specific entity
- `WorkflowStep` — individual approval steps within an instance
- `WorkflowService` — start, approve, reject workflows; auto-escalate overdue steps hourly

## Package

```
com.platform.workflow
```

## How to Use in Your Module

### 1. Start a workflow when an entity needs approval

```java
@Service
@RequiredArgsConstructor
public class TransferService {

    private final WorkflowService workflowService;

    @Auditable(entity = "Transfer", action = AuditLog.AuditAction.CREATE)
    public TransferDto create(TransferDto dto) {
        // ... save transfer with status PENDING ...
        Transfer saved = transferRepository.save(transfer);

        // Start approval workflow
        UUID initiatedBy = UserPrincipal.currentUserId();
        UUID companyId   = UserPrincipal.currentCompanyId();

        workflowService.start("TRANSFER", saved.getId(), initiatedBy, companyId);
        // WorkflowDefinition for "TRANSFER" must exist in workflow_definitions table

        return mapper.toDto(saved);
    }
}
```

### 2. Approve a workflow

```java
@PutMapping("/workflows/{instanceId}/approve")
public ResponseEntity<ApiResponse<Void>> approve(
        @PathVariable UUID instanceId,
        @RequestParam String comments) {

    UUID approverId = UserPrincipal.currentUserId();
    workflowService.approve(instanceId, approverId, comments);
    return ResponseEntity.ok(ApiResponse.success("Approved", null));
}
```

### 3. Reject a workflow

```java
workflowService.reject(instanceId, approverId, "Reason for rejection");
```

### 4. Get workflow status

```java
WorkflowInstance instance = workflowService.getById(instanceId);
WorkflowInstance.WorkflowStatus status = instance.getStatus();
```

## WorkflowStatus Values

```
PENDING      → just created, waiting for first approver
IN_PROGRESS  → at least one step actioned
APPROVED     → all steps approved
REJECTED     → any step rejected
ESCALATED    → step overdue (hourly cron fires)
CANCELLED    → manually cancelled
```

## State Machine

```
[*] → PENDING → IN_PROGRESS → APPROVED → [*]
                            → REJECTED → [*]
                            → ESCALATED → IN_PROGRESS (re-assigned)
```

## Escalation

A scheduled job runs every hour:
```
@Scheduled(cron = "0 0 * * * *")
```
Any `WorkflowStep` with `action = PENDING` and `dueAt < now` gets escalated. The instance status becomes `ESCALATED` and a notification is sent.

## Setting Up a WorkflowDefinition

Insert into `workflow_definitions` table (or via admin API):

```sql
INSERT INTO workflow_definitions (id, company_id, name, entity_type, steps_config, escalation_days, ...)
VALUES (
    uuid(),
    'your-company-id',
    'Transfer Approval',
    'TRANSFER',
    '[{"level":1,"approverRole":"HR_MANAGER"},{"level":2,"approverRole":"DIRECTOR"}]',
    3,   -- escalate after 3 days
    ...
);
```

## Database Tables

```sql
CREATE TABLE workflow_definitions (
    id              CHAR(36) NOT NULL PRIMARY KEY,
    company_id      CHAR(36) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    steps_config    TEXT,          -- JSON array of steps
    escalation_days INT NOT NULL DEFAULT 3
);

CREATE TABLE workflow_instances (
    id           CHAR(36) NOT NULL PRIMARY KEY,
    company_id   CHAR(36) NOT NULL,
    definition_id CHAR(36) NOT NULL,
    entity_id    CHAR(36) NOT NULL,
    entity_type  VARCHAR(100) NOT NULL,
    status       VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    current_step INT NOT NULL DEFAULT 1,
    initiated_by CHAR(36) NOT NULL,
    comments     TEXT
);

CREATE TABLE workflow_steps (
    id          CHAR(36) NOT NULL PRIMARY KEY,
    company_id  CHAR(36) NOT NULL,
    instance_id CHAR(36) NOT NULL,
    step_number INT NOT NULL,
    approver_id CHAR(36),
    action      VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    comments    TEXT,
    due_at      DATETIME,
    acted_at    DATETIME
);
```

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-notification</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
```
