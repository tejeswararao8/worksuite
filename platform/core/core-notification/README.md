# core-notification

**Async in-app and email notifications for all modules.**

## What It Does

- Saves in-app notifications to PostgreSQL (shown in UI notification bell)
- Sends email notifications via SMTP (JavaMailSender)
- Both are `@Async` — never blocks your request
- All notifications are scoped per tenant

## Package

```
com.platform.notification
```

## Files

| File | Purpose |
|---|---|
| `Notification.java` | JPA entity for in-app notifications |
| `NotificationRequest.java` | Builder DTO to describe what to send |
| `NotificationService.java` | Async send, query, markRead methods |
| `NotificationRepository.java` | JPA repository with tenant-scoped queries |

---

## How to Use — Send a Notification

```java
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final NotificationService notificationService;

    public LeaveDto approve(UUID leaveId, UUID approverId) {
        // ... approve leave logic ...

        // Notify the employee — in-app + email
        notificationService.send(NotificationRequest.builder()
            .companyId(leave.getCompanyId())
            .recipientId(leave.getEmployeeId())
            .recipientEmail(employee.getEmail())   // triggers email too
            .title("Leave Approved")
            .message("Your leave request from " + leave.getFromDate() + " has been approved.")
            .type(Notification.NotificationType.WORKFLOW_APPROVAL)
            .referenceId(leaveId.toString())
            .referenceType("LEAVE")
            .sendEmail(true)
            .build());

        return leaveDto;
    }
}
```

---

## In-App Only (no email)

```java
notificationService.send(NotificationRequest.builder()
    .companyId(companyId)
    .recipientId(userId)
    .title("Document Expiring")
    .message("Your passport expires in 30 days.")
    .type(Notification.NotificationType.DOCUMENT_EXPIRY)
    .referenceId(documentId.toString())
    .referenceType("DOCUMENT")
    .build());
// sendEmail defaults to false — no email sent
```

---

## Email Only

```java
notificationService.sendEmail("user@company.com", "Subject", "Body text");
```

---

## Query Notifications (for notification controller)

```java
// Get all notifications for a user (paginated, newest first)
Page<Notification> notifications = notificationService.getForCurrentUser(userId, pageable);

// Count unread (for notification bell badge)
long unread = notificationService.countUnread(userId);

// Mark one as read
notificationService.markRead(notificationId);

// Mark all as read for a user
notificationService.markAllRead(userId);
```

---

## NotificationRequest Builder Fields

| Field | Required | Description |
|---|---|---|
| `companyId` | Yes | Tenant UUID |
| `recipientId` | Yes | User UUID to receive the notification |
| `title` | Yes | Short title (shown in notification list) |
| `message` | Yes | Full message body |
| `type` | Yes | NotificationType enum value |
| `recipientEmail` | No | If set and `sendEmail=true`, sends email |
| `sendEmail` | No | Default `false` |
| `referenceId` | No | UUID of the related entity |
| `referenceType` | No | Type of related entity e.g. `"LEAVE"`, `"TRANSFER"` |

---

## NotificationType Values

```java
Notification.NotificationType.DOCUMENT_EXPIRY      // passport/visa expiry reminders
Notification.NotificationType.WORKFLOW_APPROVAL    // leave/transfer/promotion approvals
Notification.NotificationType.ONBOARDING_TASK      // new employee onboarding tasks
Notification.NotificationType.OFFBOARDING_TASK     // employee offboarding tasks
Notification.NotificationType.SYSTEM               // system-level alerts
Notification.NotificationType.GENERAL              // general purpose
```

---

## NotificationChannel Values

```java
Notification.NotificationChannel.IN_APP   // saved to DB, shown in UI
Notification.NotificationChannel.EMAIL    // sent via SMTP
```

---

## Database Table

```sql
CREATE TABLE notifications (
    id             UUID         NOT NULL PRIMARY KEY,
    company_id     UUID         NOT NULL,
    recipient_id   UUID         NOT NULL,
    title          VARCHAR(255) NOT NULL,
    message        TEXT         NOT NULL,
    type           VARCHAR(100) NOT NULL,
    channel        VARCHAR(50)  NOT NULL,
    is_read        BOOLEAN      NOT NULL DEFAULT FALSE,
    reference_id   VARCHAR(255),
    reference_type VARCHAR(100),
    created_at     TIMESTAMP    NOT NULL,
    version        BIGINT       NOT NULL DEFAULT 0,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE
);
```

---

## Configuration

```yaml
spring:
  mail:
    host: ${SMTP_HOST:smtp.gmail.com}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME:}
    password: ${SMTP_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

If `SMTP_USERNAME` is empty, email sending fails silently — logged as ERROR, does not crash the app.

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-config</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-mail</artifactId></dependency>
```
