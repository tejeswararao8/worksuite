package com.platform.messaging.service;

import com.platform.messaging.config.RabbitMQConfig;
import com.platform.messaging.model.MessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService {

    private final RabbitTemplate rabbitTemplate;

    // ─────────────────────────────────────────────
    // SEND EMAIL - Trigger background email sending
    // Example: Leave approval email, payslip email
    // ─────────────────────────────────────────────
    public void sendEmail(String tenantId, Map<String, Object> emailData) {
        send(RabbitMQConfig.EMAIL_ROUTING_KEY, MessagePayload.builder()
                .messageId(UUID.randomUUID().toString())
                .type("EMAIL")
                .sourceModule("PLATFORM")
                .tenantId(tenantId)
                .data(emailData)
                .priority("MEDIUM")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ─────────────────────────────────────────────
    // PROCESS PAYROLL - Trigger payroll calculation
    // Example: Monthly payroll processing
    // ─────────────────────────────────────────────
    public void processPayroll(String tenantId, Map<String, Object> payrollData) {
        send(RabbitMQConfig.PAYROLL_ROUTING_KEY, MessagePayload.builder()
                .messageId(UUID.randomUUID().toString())
                .type("PAYROLL")
                .sourceModule("PAYROLL")
                .tenantId(tenantId)
                .data(payrollData)
                .priority("HIGH")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ─────────────────────────────────────────────
    // SEND NOTIFICATION - Push/in-app notification
    // Example: Ticket assigned, leave approved
    // ─────────────────────────────────────────────
    public void sendNotification(String tenantId, Map<String, Object> notificationData) {
        send(RabbitMQConfig.NOTIFICATION_ROUTING_KEY, MessagePayload.builder()
                .messageId(UUID.randomUUID().toString())
                .type("NOTIFICATION")
                .sourceModule("PLATFORM")
                .tenantId(tenantId)
                .data(notificationData)
                .priority("MEDIUM")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ─────────────────────────────────────────────
    // TICKET EVENT - Ticket created/updated/assigned
    // ─────────────────────────────────────────────
    public void sendTicketEvent(String tenantId, Map<String, Object> ticketData) {
        send(RabbitMQConfig.TICKET_ROUTING_KEY, MessagePayload.builder()
                .messageId(UUID.randomUUID().toString())
                .type("TICKET")
                .sourceModule("TICKET")
                .tenantId(tenantId)
                .data(ticketData)
                .priority("MEDIUM")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ─────────────────────────────────────────────
    // SUPPORT EVENT - Support ticket raised/updated
    // ─────────────────────────────────────────────
    public void sendSupportEvent(String tenantId, Map<String, Object> supportData) {
        send(RabbitMQConfig.SUPPORT_ROUTING_KEY, MessagePayload.builder()
                .messageId(UUID.randomUUID().toString())
                .type("SUPPORT")
                .sourceModule("SUPPORT")
                .tenantId(tenantId)
                .data(supportData)
                .priority("HIGH")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ─────────────────────────────────────────────
    // AUDIT LOG - Record audit trail asynchronously
    // ─────────────────────────────────────────────
    public void sendAuditLog(String tenantId, Map<String, Object> auditData) {
        send(RabbitMQConfig.AUDIT_ROUTING_KEY, MessagePayload.builder()
                .messageId(UUID.randomUUID().toString())
                .type("AUDIT")
                .sourceModule("PLATFORM")
                .tenantId(tenantId)
                .data(auditData)
                .priority("LOW")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ─────────────────────────────────────────────
    // GENERATE REPORT - Trigger background report
    // Example: Monthly payroll report, HR report
    // ─────────────────────────────────────────────
    public void generateReport(String tenantId, Map<String, Object> reportData) {
        send(RabbitMQConfig.REPORT_ROUTING_KEY, MessagePayload.builder()
                .messageId(UUID.randomUUID().toString())
                .type("REPORT")
                .sourceModule("REPORTING")
                .tenantId(tenantId)
                .data(reportData)
                .priority("LOW")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ─────────────────────────────────────────────
    // ONBOARDING EVENT - New employee onboarding
    // ─────────────────────────────────────────────
    public void sendOffboardingEvent(String tenantId, Map<String, Object> offboardingData) {
        send(RabbitMQConfig.ONBOARDING_ROUTING_KEY, MessagePayload.builder()
                .messageId(UUID.randomUUID().toString())
                .type("OFFBOARDING")
                .sourceModule("HRMS")
                .tenantId(tenantId)
                .data(offboardingData)
                .priority("HIGH")
                .createdAt(LocalDateTime.now())
                .build());
    }

    public void sendOnboardingEvent(String tenantId, Map<String, Object> onboardingData) {
        send(RabbitMQConfig.ONBOARDING_ROUTING_KEY, MessagePayload.builder()
                .messageId(UUID.randomUUID().toString())
                .type("ONBOARDING")
                .sourceModule("HRMS")
                .tenantId(tenantId)
                .data(onboardingData)
                .priority("HIGH")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ─────────────────────────────────────────────
    // INTERNAL - Send message to exchange
    // ─────────────────────────────────────────────
    private void send(String routingKey, MessagePayload payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                routingKey,
                payload
        );
        log.debug("Message SENT → routing: {}, id: {}, type: {}",
                routingKey, payload.getMessageId(), payload.getType());
    }
}
