package com.platform.document;

import com.platform.notification.NotificationRequest;
import com.platform.notification.NotificationService;
import com.platform.notification.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentExpiryScheduler {

    private final DocumentRepository documentRepository;
    private final NotificationService notificationService;

    private static final int[] REMINDER_DAYS = {90, 60, 30, 7};

    @Scheduled(cron = "0 0 8 * * *") // every day at 8am
    @Transactional
    public void checkExpiry() {
        LocalDate today = LocalDate.now();

        for (int days : REMINDER_DAYS) {
            LocalDate target = today.plusDays(days);
            List<Document> expiring = documentRepository.findExpiringBetween(target, target);

            for (Document doc : expiring) {
                doc.setExpiryStatus(days <= 7 ? Document.ExpiryStatus.EXPIRING_SOON : Document.ExpiryStatus.EXPIRING_SOON);
                documentRepository.save(doc);

                notificationService.send(NotificationRequest.builder()
                    .companyId(doc.getCompanyId())
                    .title("Document Expiring in " + days + " days")
                    .message("Document '" + doc.getFileName() + "' expires on " + doc.getExpiryDate())
                    .type(Notification.NotificationType.DOCUMENT_EXPIRY)
                    .referenceId(doc.getId().toString())
                    .referenceType("DOCUMENT")
                    .build());
            }
        }

        // Mark expired
        List<Document> expired = documentRepository.findExpiringBetween(LocalDate.MIN, today.minusDays(1));
        expired.forEach(doc -> {
            doc.setExpiryStatus(Document.ExpiryStatus.EXPIRED);
            documentRepository.save(doc);
        });

        log.info("Document expiry check complete");
    }
}
