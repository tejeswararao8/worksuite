package com.platform.hr.platform.employees;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformInvitationRepository extends JpaRepository<PlatformInvitation, UUID> {

    /**
     * Finds an invitation by its token hash with a pessimistic write lock.
     *
     * <p>The lock prevents two concurrent acceptance requests from both reading
     * PENDING status and proceeding to create a membership. Only one transaction
     * can hold the lock; the second blocks until the first commits or rolls back,
     * at which point it will see the ACCEPTED status and be rejected.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM PlatformInvitation i WHERE i.tokenHash = :tokenHash")
    Optional<PlatformInvitation> findByTokenHash(String tokenHash);

    boolean existsByEmailAndStatus(String email, PlatformInvitation.InvitationStatus status);

    Page<PlatformInvitation> findByStatusOrderByCreatedAtDesc(
            PlatformInvitation.InvitationStatus status, Pageable pageable);
}
