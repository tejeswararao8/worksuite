package com.platform.hr.asset.repository;

import com.platform.hr.asset.entity.Asset;
import com.platform.hr.asset.entity.Asset.AssetStatus;
import com.platform.hr.asset.entity.Asset.AssetType;
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
public interface AssetRepository extends JpaRepository<Asset, UUID> {

    Optional<Asset> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    List<Asset> findByAssignedToEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);
    long countByCompanyIdAndStatusAndIsActiveTrue(UUID companyId, AssetStatus status);

    @Query("SELECT a FROM Asset a WHERE a.companyId = :companyId AND a.isActive = true " +
           "AND (:query IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:assetType IS NULL OR a.assetType = :assetType)")
    Page<Asset> search(UUID companyId, String query, AssetStatus status, AssetType assetType, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.companyId = :companyId AND a.isActive = true " +
           "AND a.warrantyExpiry IS NOT NULL AND a.warrantyExpiry BETWEEN :from AND :to")
    List<Asset> findWarrantyExpiring(UUID companyId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(a.assetCode, 4) AS int)), 0) FROM Asset a WHERE a.companyId = :companyId")
    int findMaxAssetCodeSequence(UUID companyId);
}
