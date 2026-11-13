package com.platform.hr.employee.mapper;

import com.platform.hr.employee.dto.EmployeeDto;
import com.platform.hr.employee.entity.Employee;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-13T12:43:59+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public Employee toEntity(EmployeeDto.CreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Employee.EmployeeBuilder employee = Employee.builder();

        employee.firstName( request.getFirstName() );
        employee.lastName( request.getLastName() );
        employee.email( request.getEmail() );
        employee.mobile( request.getMobile() );
        employee.alternateMobile( request.getAlternateMobile() );
        employee.gender( request.getGender() );
        employee.dateOfBirth( request.getDateOfBirth() );
        employee.nationality( request.getNationality() );
        employee.maritalStatus( request.getMaritalStatus() );
        employee.bloodGroup( request.getBloodGroup() );
        employee.currentAddress( request.getCurrentAddress() );
        employee.permanentAddress( request.getPermanentAddress() );
        employee.joiningDate( request.getJoiningDate() );
        employee.branchId( request.getBranchId() );
        employee.departmentId( request.getDepartmentId() );
        employee.teamId( request.getTeamId() );
        employee.designationId( request.getDesignationId() );
        employee.managerId( request.getManagerId() );
        employee.workLocation( request.getWorkLocation() );
        employee.employmentType( request.getEmploymentType() );

        return employee.build();
    }

    @Override
    public EmployeeDto.Response toResponse(Employee employee) {
        if ( employee == null ) {
            return null;
        }

        EmployeeDto.Response response = new EmployeeDto.Response();

        response.setId( employee.getId() );
        response.setEmployeeCode( employee.getEmployeeCode() );
        response.setFirstName( employee.getFirstName() );
        response.setLastName( employee.getLastName() );
        response.setEmail( employee.getEmail() );
        response.setMobile( employee.getMobile() );
        response.setGender( employee.getGender() );
        response.setDateOfBirth( employee.getDateOfBirth() );
        response.setNationality( employee.getNationality() );
        response.setMaritalStatus( employee.getMaritalStatus() );
        response.setBloodGroup( employee.getBloodGroup() );
        response.setPhotoUrl( employee.getPhotoUrl() );
        response.setJoiningDate( employee.getJoiningDate() );
        response.setBranchId( employee.getBranchId() );
        response.setDepartmentId( employee.getDepartmentId() );
        response.setTeamId( employee.getTeamId() );
        response.setDesignationId( employee.getDesignationId() );
        response.setManagerId( employee.getManagerId() );
        response.setWorkLocation( employee.getWorkLocation() );
        response.setEmploymentStatus( employee.getEmploymentStatus() );
        response.setEmploymentType( employee.getEmploymentType() );
        response.setCreatedAt( employee.getCreatedAt() );

        return response;
    }

    @Override
    public EmployeeDto.Summary toSummary(Employee employee) {
        if ( employee == null ) {
            return null;
        }

        EmployeeDto.Summary summary = new EmployeeDto.Summary();

        summary.setId( employee.getId() );
        summary.setEmployeeCode( employee.getEmployeeCode() );
        summary.setFirstName( employee.getFirstName() );
        summary.setLastName( employee.getLastName() );
        summary.setEmail( employee.getEmail() );
        summary.setEmploymentStatus( employee.getEmploymentStatus() );

        return summary;
    }

    @Override
    public void updateEntity(EmployeeDto.UpdateRequest request, Employee employee) {
        if ( request == null ) {
            return;
        }

        if ( request.getFirstName() != null ) {
            employee.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            employee.setLastName( request.getLastName() );
        }
        if ( request.getMobile() != null ) {
            employee.setMobile( request.getMobile() );
        }
        if ( request.getAlternateMobile() != null ) {
            employee.setAlternateMobile( request.getAlternateMobile() );
        }
        if ( request.getGender() != null ) {
            employee.setGender( request.getGender() );
        }
        if ( request.getDateOfBirth() != null ) {
            employee.setDateOfBirth( request.getDateOfBirth() );
        }
        if ( request.getNationality() != null ) {
            employee.setNationality( request.getNationality() );
        }
        if ( request.getMaritalStatus() != null ) {
            employee.setMaritalStatus( request.getMaritalStatus() );
        }
        if ( request.getBloodGroup() != null ) {
            employee.setBloodGroup( request.getBloodGroup() );
        }
        if ( request.getCurrentAddress() != null ) {
            employee.setCurrentAddress( request.getCurrentAddress() );
        }
        if ( request.getPermanentAddress() != null ) {
            employee.setPermanentAddress( request.getPermanentAddress() );
        }
        if ( request.getWorkLocation() != null ) {
            employee.setWorkLocation( request.getWorkLocation() );
        }
    }
}
