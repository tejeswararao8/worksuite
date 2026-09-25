-- V1__initial_schema.sql
-- HRMS Platform - Initial Database Schema

-- =============================================
-- COMPANIES
-- =============================================
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID,
    name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),
    registration_number VARCHAR(100) UNIQUE,
    tax_number VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    website VARCHAR(255),
    address TEXT,
    country VARCHAR(100),
    city VARCHAR(100),
    logo_url VARCHAR(500),
    industry VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- BRANCHES
-- =============================================
CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(255),
    is_head_office BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (code, company_id)
);

-- =============================================
-- DEPARTMENTS
-- =============================================
CREATE TABLE departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    parent_department_id UUID,
    head_employee_id UUID,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (code, company_id)
);

-- =============================================
-- TEAMS
-- =============================================
CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    department_id UUID NOT NULL,
    lead_employee_id UUID,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- DESIGNATIONS
-- =============================================
CREATE TABLE designations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    level INTEGER,
    grade VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- EMPLOYEES
-- =============================================
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_code VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    mobile VARCHAR(50),
    alternate_mobile VARCHAR(50),
    gender VARCHAR(20),
    date_of_birth DATE,
    nationality VARCHAR(100),
    marital_status VARCHAR(50),
    blood_group VARCHAR(10),
    photo_url VARCHAR(500),
    current_address TEXT,
    permanent_address TEXT,
    joining_date DATE NOT NULL,
    branch_id UUID,
    department_id UUID,
    team_id UUID,
    designation_id UUID,
    manager_id UUID,
    work_location VARCHAR(255),
    employment_status VARCHAR(50) NOT NULL DEFAULT 'PROBATION',
    employment_type VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (email, company_id),
    UNIQUE (employee_code, company_id)
);

-- =============================================
-- PERMISSIONS
-- =============================================
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    module VARCHAR(100),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- ROLES
-- =============================================
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (name, company_id)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

-- =============================================
-- USERS
-- =============================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    employee_id UUID,
    role_id UUID NOT NULL REFERENCES roles(id),
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    must_change_password BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- EMPLOYEE DOCUMENTS
-- =============================================
CREATE TABLE employee_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    document_type VARCHAR(100) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    document_number VARCHAR(100),
    issuing_country VARCHAR(100),
    issuing_authority VARCHAR(255),
    issue_date DATE,
    expiry_date DATE,
    file_key VARCHAR(500),
    file_name VARCHAR(255),
    document_version INTEGER DEFAULT 1,
    expiry_status VARCHAR(50) DEFAULT 'VALID',
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- NOTIFICATIONS
-- =============================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    recipient_user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(100) NOT NULL,
    reference_id VARCHAR(255),
    reference_type VARCHAR(100),
    is_read BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- AUDIT LOGS
-- =============================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    performed_by VARCHAR(255),
    performed_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(50)
);

-- =============================================
-- ONBOARDING CHECKLISTS
-- =============================================
CREATE TABLE onboarding_checklists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    task_description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    assigned_to UUID,
    completed_at TIMESTAMP,
    due_date DATE,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- PROBATION
-- =============================================
CREATE TABLE probations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    extended_end_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    remarks TEXT,
    approved_by UUID,
    approved_at TIMESTAMP,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- TRANSFERS
-- =============================================
CREATE TABLE transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    transfer_type VARCHAR(50) NOT NULL,
    from_department_id UUID,
    to_department_id UUID,
    from_branch_id UUID,
    to_branch_id UUID,
    from_manager_id UUID,
    to_manager_id UUID,
    from_team_id UUID,
    to_team_id UUID,
    effective_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by UUID,
    approved_at TIMESTAMP,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- PROMOTIONS
-- =============================================
CREATE TABLE promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    from_designation_id UUID,
    to_designation_id UUID NOT NULL,
    effective_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by UUID,
    approved_at TIMESTAMP,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- ASSETS
-- =============================================
CREATE TABLE assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    asset_code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    asset_type VARCHAR(100) NOT NULL,
    serial_number VARCHAR(255),
    model VARCHAR(255),
    brand VARCHAR(255),
    purchase_date DATE,
    warranty_expiry DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    assigned_to_employee_id UUID,
    assigned_at TIMESTAMP,
    returned_at TIMESTAMP,
    notes TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (asset_code, company_id)
);

-- =============================================
-- SKILLS & CERTIFICATIONS
-- =============================================
CREATE TABLE employee_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    skill_name VARCHAR(255) NOT NULL,
    skill_type VARCHAR(50) NOT NULL,
    proficiency_level VARCHAR(50),
    years_of_experience INTEGER,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE employee_certifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    certification_name VARCHAR(255) NOT NULL,
    issuing_organization VARCHAR(255),
    issue_date DATE,
    expiry_date DATE,
    credential_id VARCHAR(255),
    credential_url VARCHAR(500),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =============================================
-- INDEXES
-- =============================================
CREATE INDEX idx_employees_company ON employees(company_id);
CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_status ON employees(employment_status);
CREATE INDEX idx_employees_manager ON employees(manager_id);
CREATE INDEX idx_documents_employee ON employee_documents(employee_id);
CREATE INDEX idx_documents_expiry ON employee_documents(expiry_date) WHERE expiry_date IS NOT NULL;
CREATE INDEX idx_notifications_user ON notifications(recipient_user_id, company_id);
CREATE INDEX idx_notifications_unread ON notifications(recipient_user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_audit_logs_company ON audit_logs(company_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_transfers_employee ON transfers(employee_id);
CREATE INDEX idx_promotions_employee ON promotions(employee_id);
CREATE INDEX idx_assets_company ON assets(company_id);
CREATE INDEX idx_assets_employee ON assets(assigned_to_employee_id);
