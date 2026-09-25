package com.hrms.user.repository;

import com.hrms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndActiveTrue(String email);

    boolean existsByEmailAndCompanyId(String email, UUID companyId);

    Optional<User> findByEmployeeIdAndActiveTrue(UUID employeeId);
}
