package com.platform.tenancy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findByEmail(String email);
    boolean existsBySlug(String slug);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.id <> :excludeId")
    long countExcluding(@Param("excludeId") UUID excludeId);

    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.id <> :excludeId AND t.status = :status AND t.isActive = true")
    long countByStatusExcluding(@Param("excludeId") UUID excludeId, @Param("status") Tenant.TenantStatus status);

    @Query("SELECT t FROM Tenant t WHERE t.id <> :excludeId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Tenant> searchExcluding(
            @Param("excludeId") UUID excludeId,
            @Param("status") Tenant.TenantStatus status,
            @Param("search") String search,
            Pageable pageable);
}
