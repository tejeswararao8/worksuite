# core-workflow

**Generic multi-step approval workflow engine for any entity in any module.**

## What It Does

- `WorkflowDefinition` — defines the approval process for an entity type (e.g. TRANSFER, PROMOTION, LEAVE)
- `WorkflowInstance` — a running approval process for a specific entity record
- `WorkflowStep` — individual approval steps within an instance
- `WorkflowService` — start, approve, reject workflows; auto-escalate overdue steps every hour

## Package

```
com.platform.workflow
```

## Files

| File | Purpose |
|---|---|
| `WorkflowDefinition.java` | Entity defining approval steps for an entity type |
| `WorkflowInstance.java` | Entity representing a running approval process |
| `WorkflowStep.java` | Entity representing one step in an approval process |
| `WorkflowService.java` | Start, approve, reject, escalate workflows |
| `WorkflowDefinitionRepository.java` | JPA repository for definitions |
| `WorkflowInstanceRepository.java` | JPA repository for instances |
| `WorkflowStepRepository.java` | JPA repository for steps |

---

## How to Use — Start a Workflow

```java
@Service
@RequiredArgsConstructor
public class TransferService {

    private final WorkflowService workflowService;

    @Auditable(entity = "Transfer", action = AuditLog.AuditAction.CREATE)
    public TransferDto create(CreateTransferRequest req) {
        // Save transfer with PENDING status
        Transfer saved = transferRepository.save(transfer);

        // Start approval workflow
        // WorkflowDefinition for "TRANSFER" must exist in workflow_definitions table
        workflowService.start(
            "TRANSFER",
            saved.getId(),
            UserPrincipal.currentUserId(),
            UserPrincipal.currentCompanyId()
        );

        return mapper.toDto(saved);
    }
}
```

---

## Approve a Workflow Step

```java
@PutMapping("/workflows/{instanceId}/approve")
public ResponseEntity<ApiResponse<Void>> approve(
        @PathVariable UUID instanceId,
        @RequestParam String comments) {

    workflowService.approve(instanceId, UserPrincipal.currentUserId(), comments);
    return ResponseEntity.ok(ApiResponse.success("Approved", null));
}
```

---

## Reject a Workflow

```java
workflowService.reject(instanceId, UserPrincipal.currentUserId(), "Reason for rejection");
```

---

## Get Workflow Status

```java
WorkflowInstance instance = workflowService.getById(instanceId);
WorkflowInstance.WorkflowStatus status = instance.getStatus();
int currentStep = instance.getCurrentStep();
```

---

## WorkflowStatus Values

| Status | Meaning |
|---|---|
| `PENDING` | Just created, waiting for first approver |
| `IN_PROGRESS` | At least one step actioned, more steps remain |
| `APPROVED` | All steps approved — final approval |
| `REJECTED` | Any step rejected |
| `ESCALATED` | A step was overdue — hourly cron escalated it |
| `CANCELLED` | Manually cancelled |

---

## Approval Flow (Multi-Step)

```
workflowService.start("TRANSFER", entityId, initiatedBy, companyId)
    ↓
WorkflowInstance created (status=PENDING, currentStep=1)
WorkflowStep created (stepNumber=1, action=PENDING, dueAt=now+escalationDays)
    ↓
Approver calls workflowService.approve(instanceId, approverId, comments)
    ↓
If more steps exist:
    currentStep advances → status=IN_PROGRESS
    Next approver notified via NotificationService
    ↓
If last step approved:
    status=APPROVED
    Initiator notified
    ↓
Any step rejected:
    status=REJECTED
```

---

## Auto-Escalation

A scheduled job runs every hour (`@Scheduled(cron = "0 0 * * * *")`):
- Finds all `WorkflowStep` records with `action=PENDING` and `dueAt < now`
- Sets step `action=ESCALATED`
- Sets instance `status=ESCALATED`
- Sends escalation notification via `NotificationService`

---

## Setting Up a WorkflowDefinition

Insert into `workflow_definitions` table (or via admin API):

```sql
INSERT INTO workflow_definitions
    (id, company_id, name, entity_type, steps_config, escalation_days, created_at, version, is_active)
VALUES (
    gen_random_uuid(),
    'your-company-uuid',
    'Transfer Approval',
    'TRANSFER',
    '[{"level":1,"approverRole":"HR_MANAGER"},{"level":2,"approverRole":"DIRECTOR"}]',
    3,
    NOW(),
    0,
    TRUE
);
```

`steps_config` is a JSON array. Each object defines one approval level. The `WorkflowService` reads `escalationDays` to set `dueAt` on each step.

---

## Database Tables

```sql
CREATE TABLE workflow_definitions (
    id              UUID         NOT NULL PRIMARY KEY,
    company_id      UUID         NOT NULL,
    name            VARCHAR(255) NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    steps_config    TEXT,
    escalation_days INT          NOT NULL DEFAULT 3,
    created_at      TIMESTAMP    NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE workflow_instances (
    id            UUID        NOT NULL PRIMARY KEY,
    company_id    UUID        NOT NULL,
    definition_id UUID        NOT NULL REFERENCES workflow_definitions(id),
    entity_id     UUID        NOT NULL,
    entity_type   VARCHAR(100) NOT NULL,
    status        VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    current_step  INT         NOT NULL DEFAULT 1,
    initiated_by  UUID        NOT NULL,
    comments      TEXT,
    created_at    TIMESTAMP   NOT NULL,
    version       BIGINT      NOT NULL DEFAULT 0,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE workflow_steps (
    id           UUID        NOT NULL PRIMARY KEY,
    company_id   UUID        NOT NULL,
    instance_id  UUID        NOT NULL REFERENCES workflow_instances(id),
    step_number  INT         NOT NULL,
    approver_id  UUID,
    approver_role VARCHAR(100),
    action       VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    comments     TEXT,
    acted_at     TIMESTAMP,
    due_at       TIMESTAMP,
    created_at   TIMESTAMP   NOT NULL,
    version      BIGINT      NOT NULL DEFAULT 0,
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE
);
```

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-notification</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
```
