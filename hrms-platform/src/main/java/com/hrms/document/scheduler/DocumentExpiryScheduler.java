package com.hrms.document.scheduler;

import com.hrms.company.repository.CompanyRepository;
import com.hrms.document.entity.EmployeeDocument;
import com.hrms.document.entity.EmployeeDocument.ExpiryStatus;
import com.hrms.document.repository.DocumentRepository;
import com.hrms.notification.entity.Notification.NotificationType;
import com.hrms.notification.service.NotificationService;
import com.hrms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class DocumentExpiryScheduler {

    private static final int[] REMINDER_DAYS = {90, 60, 30, 7};

    private final DocumentRepository documentRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkDocumentExpiry() {
        log.info("Running document expiry check");
        companyRepository.findAllByActiveTrue(org.springframework.data.domain.Pageable.unpaged())
                .forEach(company -> processCompanyDocuments(company.getId()));
    }

    private void processCompanyDocuments(java.util.UUID companyId) {
        LocalDate today = LocalDate.now();
        for (int days : REMINDER_DAYS) {
            LocalDate targetDate = today.plusDays(days);
            List<EmployeeDocument> docs = documentRepository.findExpiringDocuments(
                    companyId, targetDate, targetDate);
            docs.forEach(doc -> sendExpiryNotification(doc, days));
        }
        updateExpiredDocuments(companyId, today);
    }

    private void sendExpiryNotification(EmployeeDocument doc, int daysRemaining) {
        doc.setExpiryStatus(ExpiryStatus.EXPIRING_SOON);
        userRepository.findByEmployeeIdAndActiveTrue(doc.getEmployeeId()).ifPresent(user -> {
            String title = "Document Expiry Reminder";
            String message = String.format("Your document '%s' expires in %d days on %s.",
                    doc.getDocumentName(), daysRemaining, doc.getExpiryDate());
            notificationService.send(doc.getCompanyId(), user.getId(), title, message,
                    NotificationType.DOCUMENT_EXPIRY, doc.getId().toString(), "EmployeeDocument");
        });
    }

    private void updateExpiredDocuments(java.util.UUID companyId, LocalDate today) {
        List<EmployeeDocument> expired = documentRepository.findExpiringDocuments(
                companyId, LocalDate.of(2000, 1, 1), today.minusDays(1));
        expired.forEach(doc -> {
            doc.setExpiryStatus(ExpiryStatus.EXPIRED);
            documentRepository.save(doc);
        });
    }
}
