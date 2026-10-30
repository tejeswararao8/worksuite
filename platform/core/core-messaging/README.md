# core-messaging

**RabbitMQ message broker for async background task processing across all modules.**

## What It Does

- `RabbitMQConfig` — configures DirectExchange, 8 durable queues, bindings, and Jackson JSON message converter
- `MessagingService` — typed send methods for every queue: email, payroll, notification, ticket, support, audit, report, onboarding
- `MessagePayload` — serializable DTO that wraps all messages with metadata (messageId, type, tenantId, priority, timestamp)

## Package

```
com.platform.messaging.config
com.platform.messaging.model
com.platform.messaging.service
```

## Files

| File | Purpose |
|---|---|
| `config/RabbitMQConfig.java` | Exchange, queues, bindings, message converter |
| `service/MessagingService.java` | Typed send methods for all queues |
| `model/MessagePayload.java` | Serializable message wrapper DTO |

---

## How to Use — Send a Message

```java
@Service
@RequiredArgsConstructor
public class PayrollService {

    private final MessagingService messagingService;

    public void triggerMonthlyPayroll(UUID companyId, int month, int year) {
        String tenantId = companyId.toString();

        messagingService.processPayroll(tenantId, Map.of(
            "companyId", companyId.toString(),
            "month",     month,
            "year",      year,
            "triggeredBy", UserPrincipal.currentUserId().toString()
        ));
        // Returns immediately — payroll runs in background
    }
}
```

---

## All Send Methods

```java
// Send email in background (welcome email, payslip email, etc.)
messagingService.sendEmail(tenantId, Map.of(
    "to",      "user@company.com",
    "subject", "Your Payslip",
    "body",    "Please find your payslip attached."
));

// Trigger payroll calculation
messagingService.processPayroll(tenantId, Map.of(
    "companyId", companyId,
    "month", 1, "year", 2025
));

// Send push/in-app notification
messagingService.sendNotification(tenantId, Map.of(
    "recipientId", userId,
    "title",       "Leave Approved",
    "message",     "Your leave has been approved."
));

// Ticket event (created, assigned, updated)
messagingService.sendTicketEvent(tenantId, Map.of(
    "ticketId",  ticketId,
    "event",     "ASSIGNED",
    "assigneeId", assigneeId
));

// Support ticket event
messagingService.sendSupportEvent(tenantId, Map.of(
    "ticketId", ticketId,
    "event",    "ESCALATED"
));

// Audit log (async, non-blocking)
messagingService.sendAuditLog(tenantId, Map.of(
    "entityName", "Employee",
    "entityId",   employeeId,
    "action",     "UPDATE"
));

// Background report generation
messagingService.generateReport(tenantId, Map.of(
    "reportType", "PAYROLL_MONTHLY",
    "month", 1, "year", 2025
));

// New employee onboarding event
messagingService.sendOnboardingEvent(tenantId, Map.of(
    "employeeId",  employeeId,
    "companyId",   companyId,
    "joiningDate", joiningDate
));
```

---

## Queues and Routing Keys

| Queue | Routing Key | Priority | Purpose |
|---|---|---|---|
| `vve.email.queue` | `email` | MEDIUM | Background email sending |
| `vve.payroll.queue` | `payroll` | HIGH | Monthly payroll processing |
| `vve.notification.queue` | `notification` | MEDIUM | Push/in-app notifications |
| `vve.ticket.queue` | `ticket` | MEDIUM | Ticket events |
| `vve.support.queue` | `support` | HIGH | Support ticket escalations |
| `vve.audit.queue` | `audit` | LOW | Async audit logging |
| `vve.report.queue` | `report` | LOW | Background report generation |
| `vve.onboarding.queue` | `onboarding` | HIGH | Employee onboarding events |

All queues are **durable** — messages survive RabbitMQ restarts.

Exchange name: `vve.platform.exchange` (DirectExchange)

---

## MessagePayload Structure

Every message sent through `MessagingService` is wrapped in `MessagePayload`:

```json
{
  "messageId":    "uuid-string",
  "type":         "PAYROLL",
  "sourceModule": "PAYROLL",
  "tenantId":     "company-uuid",
  "data": {
    "companyId": "...",
    "month": 1,
    "year": 2025
  },
  "priority":   "HIGH",
  "createdAt":  "2024-01-15T10:23:45"
}
```

---

## Writing a Consumer (Queue Listener)

Consumers are written in the module that processes the message:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollConsumer {

    @RabbitListener(queues = RabbitMQConfig.PAYROLL_QUEUE)
    public void handlePayroll(MessagePayload payload) {
        log.info("Processing payroll for tenant: {}", payload.getTenantId());

        Map<String, Object> data = payload.getData();
        UUID companyId = UUID.fromString((String) data.get("companyId"));
        int month = (int) data.get("month");
        int year  = (int) data.get("year");

        // ... process payroll ...
    }
}
```

---

## Configuration

```yaml
spring:
  rabbitmq:
    host:         ${RABBITMQ_HOST:localhost}
    port:         ${RABBITMQ_PORT:5672}
    username:     ${RABBITMQ_USER:admin}
    password:     ${RABBITMQ_PASS:admin123}
    virtual-host: /
    listener:
      simple:
        acknowledge-mode: auto
        retry:
          enabled: true
          initial-interval: 3000   # 3 seconds
          max-attempts: 3
          multiplier: 2            # 3s, 6s, 12s
```

RabbitMQ Management UI: http://localhost:15672 (admin / admin123)

Start with:
```bash
docker compose up -d rabbitmq
```

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-amqp</artifactId></dependency>
```
