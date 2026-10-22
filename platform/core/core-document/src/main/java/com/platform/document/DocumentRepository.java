package com.platform.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByEntityIdAndCompanyId(UUID entityId, UUID companyId);

    @Query("SELECT d FROM Document d WHERE d.expiryDate IS NOT NULL AND d.expiryDate BETWEEN :from AND :to AND d.isActive = true")
    List<Document> findExpiringBetween(LocalDate from, LocalDate to);
}
