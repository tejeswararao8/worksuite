package com.platform.hr.invitation;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantEmployeeInvitationRepository extends JpaRepository<TenantEmployeeInvitation, UUID> {

    /**
     * Finds an invitation by token hash with a pessimistic write lock.
     * Prevents two concurrent acceptance requests from both reading PENDING
     * and proceeding to create a membership.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM TenantEmployeeInvitation i WHERE i.tokenHash = :tokenHash")
    Optional<TenantEmployeeInvitation> findByTokenHash(@Param("tokenHash") String tokenHash);

    boolean existsByEmployeeIdAndStatus(UUID employeeId, TenantEmployeeInvitation.InvitationStatus status);

    @Query("SELECT i FROM TenantEmployeeInvitation i WHERE i.tenantId = :tenantId ORDER BY i.createdAt DESC")
    Page<TenantEmployeeInvitation> findByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT i FROM TenantEmployeeInvitation i WHERE i.tenantId = :tenantId AND i.status = :status ORDER BY i.createdAt DESC")
    Page<TenantEmployeeInvitation> findByTenantIdAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("status") TenantEmployeeInvitation.InvitationStatus status,
            Pageable pageable);

    Optional<TenantEmployeeInvitation> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<TenantEmployeeInvitation> findByEmployeeIdAndStatus(
            UUID employeeId, TenantEmployeeInvitation.InvitationStatus status);
}
