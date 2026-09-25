package com.hrms.document.repository;

import com.hrms.document.entity.EmployeeDocument;
import com.hrms.document.entity.EmployeeDocument.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<EmployeeDocument, UUID> {

    Page<EmployeeDocument> findByEmployeeIdAndCompanyIdAndActiveTrue(UUID employeeId, UUID companyId, Pageable pageable);

    Optional<EmployeeDocument> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);

    @Query("SELECT d FROM EmployeeDocument d WHERE d.companyId = :companyId AND d.active = true " +
           "AND d.expiryDate IS NOT NULL AND d.expiryDate BETWEEN :from AND :to")
    List<EmployeeDocument> findExpiringDocuments(UUID companyId, LocalDate from, LocalDate to);

    @Query("SELECT COUNT(d) FROM EmployeeDocument d WHERE d.companyId = :companyId AND d.active = true " +
           "AND d.expiryDate IS NOT NULL AND d.expiryDate <= :before")
    long countExpiringBefore(UUID companyId, LocalDate before);

    List<EmployeeDocument> findByEmployeeIdAndDocumentTypeAndActiveTrue(UUID employeeId, DocumentType type);
}
