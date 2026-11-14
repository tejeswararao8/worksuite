package com.platform.hr.invitation;

import com.platform.config.ConfigService;
import com.platform.hr.department.repository.DepartmentRepository;
import com.platform.hr.designation.repository.DesignationRepository;
import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.invitation.bulk.BulkOnboardingDto;
import com.platform.hr.invitation.bulk.BulkOnboardingService;
import com.platform.hr.onboarding.service.OnboardingService;
import com.platform.hr.platform.subscription.SubscriptionService;
import com.platform.hr.probation.service.ProbationService;
import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.TenantSecurityService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkOnboardingService — Unit Tests")
class BulkOnboardingServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private DesignationRepository designationRepository;
    @Mock private TenantEmployeeInvitationRepository invitationRepository;
    @Mock private TenantInvitationService invitationService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private OnboardingService onboardingService;
    @Mock private ProbationService probationService;
    @Mock private ConfigService configService;

    @InjectMocks
    private BulkOnboardingService service;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID DEPT_ID   = UUID.randomUUID();
    private static final UUID DESIG_ID  = UUID.randomUUID();

    @BeforeEach
    void setup() {
        TenantContext.setTenantId(TENANT_ID.toString());
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    private MultipartFile csvFile(String content) {
        return new MockMultipartFile("file", "import.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    private String validCsvRow(String email) {
        return "first_name,last_name,email,mobile,joining_date,department_id,designation_id,employment_type,role_name,work_location\n" +
               "John,Doe," + email + ",+1234567890,2024-01-15," + DEPT_ID + "," + DESIG_ID + ",FULL_TIME,EMPLOYEE,HQ\n";
    }

    private com.platform.hr.department.entity.Department mockDept() {
        com.platform.hr.department.entity.Department d = new com.platform.hr.department.entity.Department();
        d.setId(DEPT_ID);
        d.setCompanyId(TENANT_ID);
        d.setIsActive(true);
        return d;
    }

    private com.platform.hr.designation.entity.Designation mockDesig() {
        com.platform.hr.designation.entity.Designation d = new com.platform.hr.designation.entity.Designation();
        d.setId(DESIG_ID);
        d.setCompanyId(TENANT_ID);
        d.setIsActive(true);
        return d;
    }

    private Employee savedEmployee() {
        Employee e = new Employee();
        e.setId(UUID.randomUUID());
        e.setCompanyId(TENANT_ID);
        e.setEmail("john.doe@company.com");
        e.setFirstName("John");
        e.setLastName("Doe");
        e.setEmployeeCode("EMP00001");
        e.setJoiningDate(LocalDate.of(2024, 1, 15));
        e.setEmploymentStatus(EmploymentStatus.PENDING_INVITATION);
        e.setIsActive(true);
        return e;
    }

    // ── 1. Valid CSV import ───────────────────────────────────────────────────

    @Test
    @DisplayName("importFile() processes valid CSV row successfully")
    void importFile_validCsv_success() throws IOException {
        when(departmentRepository.findByIdAndCompanyIdAndIsActiveTrue(DEPT_ID, TENANT_ID))
                .thenReturn(Optional.of(mockDept()));
        when(designationRepository.findByIdAndCompanyIdAndIsActiveTrue(DESIG_ID, TENANT_ID))
                .thenReturn(Optional.of(mockDesig()));
        when(employeeRepository.existsByEmailAndCompanyId(anyString(), eq(TENANT_ID))).thenReturn(false);
        when(employeeRepository.findMaxEmployeeCodeSequence(TENANT_ID)).thenReturn(0);
        when(employeeRepository.save(any())).thenAnswer(i -> {
            Employee e = i.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(configService.getInt(anyString(), anyInt())).thenReturn(90);
        doNothing().when(subscriptionService).enforceEmployeeLimit(TENANT_ID);

        TenantInvitationDto.Response invResp = TenantInvitationDto.Response.builder()
                .id(UUID.randomUUID()).status(TenantEmployeeInvitation.InvitationStatus.PENDING)
                .emailDelivered(true).build();
        when(invitationService.invite(any())).thenReturn(invResp);

        BulkOnboardingDto.ImportResponse result = service.importFile(
                csvFile(validCsvRow("john.doe@company.com")));

        assertThat(result.getTotalRows()).isEqualTo(1);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailedCount()).isEqualTo(0);
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getStatus())
                .isEqualTo(BulkOnboardingDto.RowStatus.INVITATION_PENDING);
    }

    // ── 2. Invalid row handling ───────────────────────────────────────────────

    @Test
    @DisplayName("importFile() reports invalid rows with field and message, does not discard silently")
    void importFile_invalidRow_reportedNotDiscarded() throws IOException {
        String csv = "first_name,last_name,email,mobile,joining_date,department_id,designation_id,employment_type,role_name,work_location\n" +
                     ",Doe,not-an-email,,2024-01-15," + DEPT_ID + "," + DESIG_ID + ",FULL_TIME,EMPLOYEE,HQ\n";

        BulkOnboardingDto.ImportResponse result = service.importFile(csvFile(csv));

        assertThat(result.getTotalRows()).isEqualTo(1);
        assertThat(result.getFailedCount()).isEqualTo(1);
        assertThat(result.getResults().get(0).getStatus()).isEqualTo(BulkOnboardingDto.RowStatus.FAILED);
        assertThat(result.getResults().get(0).getErrorField()).isNotBlank();
        assertThat(result.getResults().get(0).getErrorMessage()).isNotBlank();
    }

    // ── 3. Duplicate email in file ────────────────────────────────────────────

    @Test
    @DisplayName("importFile() detects duplicate email within the uploaded file")
    void importFile_duplicateEmailInFile_secondRowFails() throws IOException {
        String csv = "first_name,last_name,email,mobile,joining_date,department_id,designation_id,employment_type,role_name,work_location\n" +
                     "John,Doe,dup@company.com,+1,2024-01-15," + DEPT_ID + "," + DESIG_ID + ",FULL_TIME,EMPLOYEE,HQ\n" +
                     "Jane,Doe,dup@company.com,+2,2024-01-15," + DEPT_ID + "," + DESIG_ID + ",FULL_TIME,EMPLOYEE,HQ\n";

        when(departmentRepository.findByIdAndCompanyIdAndIsActiveTrue(DEPT_ID, TENANT_ID))
                .thenReturn(Optional.of(mockDept()));
        when(designationRepository.findByIdAndCompanyIdAndIsActiveTrue(DESIG_ID, TENANT_ID))
                .thenReturn(Optional.of(mockDesig()));
        when(employeeRepository.existsByEmailAndCompanyId(anyString(), eq(TENANT_ID))).thenReturn(false);
        when(employeeRepository.findMaxEmployeeCodeSequence(TENANT_ID)).thenReturn(0);
        when(employeeRepository.save(any())).thenAnswer(i -> {
            Employee e = i.getArgument(0); e.setId(UUID.randomUUID()); return e;
        });
        when(configService.getInt(anyString(), anyInt())).thenReturn(90);
        doNothing().when(subscriptionService).enforceEmployeeLimit(TENANT_ID);
        TenantInvitationDto.Response invResp = TenantInvitationDto.Response.builder()
                .id(UUID.randomUUID()).status(TenantEmployeeInvitation.InvitationStatus.PENDING)
                .emailDelivered(true).build();
        when(invitationService.invite(any())).thenReturn(invResp);

        BulkOnboardingDto.ImportResponse result = service.importFile(csvFile(csv));

        assertThat(result.getTotalRows()).isEqualTo(2);
        assertThat(result.getFailedCount()).isEqualTo(1);
        assertThat(result.getResults().stream()
                .filter(r -> r.getStatus() == BulkOnboardingDto.RowStatus.FAILED)
                .findFirst().get().getErrorField()).isEqualTo("email");
    }

    // ── 4. Duplicate email against existing records ───────────────────────────

    @Test
    @DisplayName("importFile() rejects email that already exists in the database")
    void importFile_duplicateEmailInDb_rowFails() throws IOException {
        when(employeeRepository.existsByEmailAndCompanyId("existing@company.com", TENANT_ID)).thenReturn(true);

        String csv = "first_name,last_name,email,mobile,joining_date,department_id,designation_id,employment_type,role_name,work_location\n" +
                     "John,Doe,existing@company.com,+1,2024-01-15," + DEPT_ID + "," + DESIG_ID + ",FULL_TIME,EMPLOYEE,HQ\n";

        BulkOnboardingDto.ImportResponse result = service.importFile(csvFile(csv));

        assertThat(result.getFailedCount()).isEqualTo(1);
        assertThat(result.getResults().get(0).getErrorField()).isEqualTo("email");
    }

    // ── 5. Cross-tenant department rejected ──────────────────────────────────

    @Test
    @DisplayName("importFile() rejects department belonging to another tenant")
    void importFile_crossTenantDepartment_rowFails() throws IOException {
        when(employeeRepository.existsByEmailAndCompanyId(anyString(), eq(TENANT_ID))).thenReturn(false);
        when(departmentRepository.findByIdAndCompanyIdAndIsActiveTrue(DEPT_ID, TENANT_ID))
                .thenReturn(Optional.empty()); // not found for this tenant

        BulkOnboardingDto.ImportResponse result = service.importFile(
                csvFile(validCsvRow("new@company.com")));

        assertThat(result.getFailedCount()).isEqualTo(1);
        assertThat(result.getResults().get(0).getErrorField()).isEqualTo("department_id");
    }

    // ── 6. Subscription limit enforcement ────────────────────────────────────

    @Test
    @DisplayName("importFile() enforces subscription limit — throws if limit exceeded")
    void importFile_subscriptionLimitExceeded_throws() throws IOException {
        when(departmentRepository.findByIdAndCompanyIdAndIsActiveTrue(DEPT_ID, TENANT_ID))
                .thenReturn(Optional.of(mockDept()));
        when(designationRepository.findByIdAndCompanyIdAndIsActiveTrue(DESIG_ID, TENANT_ID))
                .thenReturn(Optional.of(mockDesig()));
        when(employeeRepository.existsByEmailAndCompanyId(anyString(), eq(TENANT_ID))).thenReturn(false);
        doThrow(new TenantException("Employee limit reached"))
                .when(subscriptionService).enforceEmployeeLimit(TENANT_ID);

        assertThatThrownBy(() -> service.importFile(csvFile(validCsvRow("new@company.com"))))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Employee limit reached");
    }

    // ── 7. Platform tenant rejected ───────────────────────────────────────────

    @Test
    @DisplayName("importFile() rejects when TenantContext is the platform pseudo-tenant")
    void importFile_platformTenant_throws() throws IOException {
        TenantContext.setTenantId(TenantSecurityService.PLATFORM_TENANT_ID.toString());

        assertThatThrownBy(() -> service.importFile(csvFile(validCsvRow("x@x.com"))))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("platform pseudo-tenant");
    }

    // ── 8. Email delivery failure does not lose employee record ───────────────

    @Test
    @DisplayName("importFile() marks row EMAIL_DELIVERY_FAILED when SMTP fails, employee record preserved")
    void importFile_emailDeliveryFailure_employeePreserved() throws IOException {
        when(departmentRepository.findByIdAndCompanyIdAndIsActiveTrue(DEPT_ID, TENANT_ID))
                .thenReturn(Optional.of(mockDept()));
        when(designationRepository.findByIdAndCompanyIdAndIsActiveTrue(DESIG_ID, TENANT_ID))
                .thenReturn(Optional.of(mockDesig()));
        when(employeeRepository.existsByEmailAndCompanyId(anyString(), eq(TENANT_ID))).thenReturn(false);
        when(employeeRepository.findMaxEmployeeCodeSequence(TENANT_ID)).thenReturn(0);
        when(employeeRepository.save(any())).thenAnswer(i -> {
            Employee e = i.getArgument(0); e.setId(UUID.randomUUID()); return e;
        });
        when(configService.getInt(anyString(), anyInt())).thenReturn(90);
        doNothing().when(subscriptionService).enforceEmployeeLimit(TENANT_ID);

        TenantInvitationDto.Response invResp = TenantInvitationDto.Response.builder()
                .id(UUID.randomUUID()).status(TenantEmployeeInvitation.InvitationStatus.PENDING)
                .emailDelivered(false).build();
        when(invitationService.invite(any())).thenReturn(invResp);

        BulkOnboardingDto.ImportResponse result = service.importFile(
                csvFile(validCsvRow("john@company.com")));

        assertThat(result.getEmailDeliveryFailureCount()).isEqualTo(1);
        assertThat(result.getResults().get(0).getStatus())
                .isEqualTo(BulkOnboardingDto.RowStatus.EMAIL_DELIVERY_FAILED);
        // Employee was still saved
        verify(employeeRepository, atLeastOnce()).save(any());
    }

    // ── 9. CSV template generation ────────────────────────────────────────────

    @Test
    @DisplayName("generateCsvTemplate() returns non-empty CSV with expected headers")
    void generateCsvTemplate_containsHeaders() throws IOException {
        byte[] template = service.generateCsvTemplate();
        String content = new String(template, StandardCharsets.UTF_8);
        assertThat(content).contains("first_name", "last_name", "email", "joining_date",
                "department_id", "designation_id", "role_name");
    }
}
