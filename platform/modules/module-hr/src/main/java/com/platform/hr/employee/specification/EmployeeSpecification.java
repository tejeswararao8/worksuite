package com.platform.hr.employee.specification;

import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class EmployeeSpecification {

    public static Specification<Employee> filter(UUID companyId, String query,
                                                  UUID departmentId, UUID branchId,
                                                  EmploymentStatus status) {
        return Specification
            .where(hasCompany(companyId))
            .and(isActive())
            .and(hasQuery(query))
            .and(hasDepartment(departmentId))
            .and(hasBranch(branchId))
            .and(hasStatus(status));
    }

    private static Specification<Employee> hasCompany(UUID companyId) {
        return (r, q, cb) -> cb.equal(r.get("companyId"), companyId);
    }

    private static Specification<Employee> isActive() {
        return (r, q, cb) -> cb.isTrue(r.get("isActive"));
    }

    private static Specification<Employee> hasQuery(String query) {
        if (query == null || query.isBlank()) return null;
        String like = "%" + query.toLowerCase() + "%";
        return (r, q, cb) -> cb.or(
            cb.like(cb.lower(r.get("firstName")), like),
            cb.like(cb.lower(r.get("lastName")), like),
            cb.like(cb.lower(r.get("email")), like),
            cb.like(cb.lower(r.get("employeeCode")), like)
        );
    }

    private static Specification<Employee> hasDepartment(UUID departmentId) {
        if (departmentId == null) return null;
        return (r, q, cb) -> cb.equal(r.get("departmentId"), departmentId);
    }

    private static Specification<Employee> hasBranch(UUID branchId) {
        if (branchId == null) return null;
        return (r, q, cb) -> cb.equal(r.get("branchId"), branchId);
    }

    private static Specification<Employee> hasStatus(EmploymentStatus status) {
        if (status == null) return null;
        return (r, q, cb) -> cb.equal(r.get("employmentStatus"), status);
    }
}
