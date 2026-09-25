package com.hrms.asset.repository;

import com.hrms.asset.entity.Asset;
import com.hrms.asset.entity.Asset.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    Optional<Asset> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
    Page<Asset> findByCompanyIdAndActiveTrue(UUID companyId, Pageable pageable);
    List<Asset> findByAssignedToEmployeeIdAndCompanyIdAndActiveTrue(UUID employeeId, UUID companyId);
    boolean existsByAssetCodeAndCompanyId(String code, UUID companyId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(a.assetCode, 4) AS int)), 0) FROM Asset a WHERE a.companyId = :companyId")
    int findMaxAssetCodeSequence(UUID companyId);
}
