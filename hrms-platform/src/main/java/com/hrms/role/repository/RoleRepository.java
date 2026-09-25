package com.hrms.role.repository;

import com.hrms.role.entity.Permission;
import com.hrms.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByNameAndCompanyId(String name, UUID companyId);
    List<Role> findByCompanyIdAndActiveTrue(UUID companyId);
    boolean existsByNameAndCompanyId(String name, UUID companyId);
}
