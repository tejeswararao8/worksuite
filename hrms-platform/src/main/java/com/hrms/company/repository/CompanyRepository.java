package com.hrms.company.repository;

import com.hrms.company.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByIdAndActiveTrue(UUID id);

    boolean existsByRegistrationNumber(String registrationNumber);

    Page<Company> findAllByActiveTrue(Pageable pageable);
}
