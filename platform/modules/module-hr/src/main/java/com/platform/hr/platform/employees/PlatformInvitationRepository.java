package com.platform.hr.platform.employees;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformInvitationRepository extends JpaRepository<PlatformInvitation, UUID> {

    Optional<PlatformInvitation> findByTokenHash(String tokenHash);

    boolean existsByEmailAndStatus(String email, PlatformInvitation.InvitationStatus status);

    Page<PlatformInvitation> findByStatusOrderByCreatedAtDesc(
            PlatformInvitation.InvitationStatus status, Pageable pageable);
}
