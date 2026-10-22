package com.platform.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, UUID> {
    Optional<PlatformConfig> findByKeyAndCompanyId(String key, UUID companyId);
    List<PlatformConfig> findByCategoryAndCompanyId(String category, UUID companyId);
}
