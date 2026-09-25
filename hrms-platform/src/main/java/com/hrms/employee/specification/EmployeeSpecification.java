package com.hrms.employee.specification;

import com.hrms.employee.entity.Employee;
import com.hrms.employee.entity.Employee.EmploymentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmployeeSpecification {

    private EmployeeSpecification() {}

    public static Specification<Employee> filter(UUID companyId, String search,
                                                  UUID departmentId, UUID branchId,
                                                  EmploymentStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));
            predicates.add(cb.isTrue(root.get("active")));

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("employeeCode")), pattern)
                ));
            }
            if (departmentId != null) predicates.add(cb.equal(root.get("departmentId"), departmentId));
            if (branchId != null) predicates.add(cb.equal(root.get("branchId"), branchId));
            if (status != null) predicates.add(cb.equal(root.get("employmentStatus"), status));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
