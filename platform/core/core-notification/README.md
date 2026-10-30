# core-notification

**Async in-app and email notifications for all modules.**

## What It Does

- Sends in-app notifications (stored in DB, shown in UI)
- Sends email notifications via SMTP
- Both are async — never blocks your request
- Scoped per tenant

## Package

```
com.platform.notification
```

## How to Use in Your Module

### Inject NotificationService

```java
@Service
@RequiredArgsConstructor
public class TransferService {

    private final NotificationService notificationService;

    public TransferDto approve(UUID transferId, UUID approverId) {
        // ... approve transfer logic ...

        // Notify the employee
        notificationService.send(NotificationRequest.builder()
            .companyId(transfer.getCompanyId())
            .recipientId(transfer.getEmployeeId())
            .recipientEmail(employee.getEmail())   // optional — triggers email too
            .title("Transfer Approved")
            .message("Your transfer to " + newDept + " has been approved.")
            .type(Notification.NotificationType.WORKFLOW_APPROVAL)
            .referenceId(transferId.toString())
            .referenceType("TRANSFER")
            .sendEmail(true)
            .build());

        return transferDto;
    }
}
```

### In-App Only (no email)

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

### Email Only

```java
notificationService.sendEmail("user@company.com", "Subject", "Body text");
```

### Query Notifications (for your notification controller)

```java
// Get all notifications for a user (paginated)
Page<Notification> notifications = notificationService.getForCurrentUser(userId, pageable);

// Count unread
long unread = notificationService.countUnread(userId);

// Mark one as read
notificationService.markRead(notificationId);

// Mark all as read for a user
notificationService.markAllRead(userId);
```

## NotificationType Values

```java
Notification.NotificationType.DOCUMENT_EXPIRY
Notification.NotificationType.WORKFLOW_APPROVAL
Notification.NotificationType.ONBOARDING_TASK
Notification.NotificationType.OFFBOARDING_TASK
Notification.NotificationType.SYSTEM
Notification.NotificationType.GENERAL
```

## NotificationRequest Builder Fields

| Field | Required | Description |
|---|---|---|
| `companyId` | Yes | Tenant ID |
| `recipientId` | Yes | User UUID to receive notification |
| `title` | Yes | Short title |
| `message` | Yes | Full message body |
| `type` | Yes | NotificationType enum |
| `recipientEmail` | No | If set + sendEmail=true, sends email |
| `sendEmail` | No | Default false |
| `referenceId` | No | ID of related entity |
| `referenceType` | No | Type of related entity (e.g. "TRANSFER") |

## Database Table

```sql
CREATE TABLE notifications (
    id             CHAR(36) NOT NULL PRIMARY KEY,
    company_id     CHAR(36) NOT NULL,
    recipient_id   CHAR(36) NOT NULL,
    title          VARCHAR(255) NOT NULL,
    message        TEXT NOT NULL,
    type           VARCHAR(100) NOT NULL,
    channel        VARCHAR(50) NOT NULL,
    is_read        BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id   VARCHAR(255),
    reference_type VARCHAR(100),
    created_at     DATETIME NOT NULL
);
```

## Configuration

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

If `MAIL_USERNAME` is empty, email sending will fail silently (logged as error, does not crash the app).

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-security</artifactId></dependency>
<dependency><groupId>com.platform</groupId><artifactId>core-config</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-mail</artifactId></dependency>
```
