-- V6: Platform role separation, security indexes, Keycloak user ID column
-- Phase 3A security hardening

-- ─── 1. Add keycloak_user_id to users table ──────────────────────────────────
-- Links the platform user record to the Keycloak subject (UUID).
-- Used to validate that a JWT subject actually maps to a known platform user.
ALTER TABLE users ADD COLUMN IF NOT EXISTS keycloak_user_id VARCHAR(36);
CREATE INDEX IF NOT EXISTS idx_users_keycloak_id ON users(keycloak_user_id);

-- ─── 2. Platform role permissions ────────────────────────────────────────────
-- Add permissions for platform-level operations not covered in V4.
INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000020', 'PLATFORM_TENANT_MANAGE', 'Create and manage tenants', 'PLATFORM'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PLATFORM_TENANT_MANAGE');

INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000021', 'PLATFORM_USER_MANAGE', 'Manage platform users', 'PLATFORM'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PLATFORM_USER_MANAGE');

INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000022', 'PLATFORM_SUPPORT_READ', 'Read-only support access across tenants', 'PLATFORM'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PLATFORM_SUPPORT_READ');

INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000023', 'PLATFORM_IMPL_CONFIGURE', 'Configure tenant workspaces', 'PLATFORM'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PLATFORM_IMPL_CONFIGURE');

-- HR permissions (used by @PreAuthorize in HR controllers)
INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000030', 'EMPLOYEE_READ',  'View employees',   'HR'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EMPLOYEE_READ');

INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000031', 'EMPLOYEE_WRITE', 'Manage employees', 'HR'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EMPLOYEE_WRITE');

INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000032', 'DEPARTMENT_READ',  'View departments',   'HR'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'DEPARTMENT_READ');

INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000033', 'DEPARTMENT_WRITE', 'Manage departments', 'HR'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'DEPARTMENT_WRITE');

INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000034', 'ASSET_READ',  'View assets',   'HR'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ASSET_READ');

INSERT INTO permissions (id, name, description, module)
SELECT '10000000-0000-0000-0000-000000000035', 'ASSET_WRITE', 'Manage assets', 'HR'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ASSET_WRITE');

-- ─── 3. Platform roles (scoped to platform pseudo-tenant) ────────────────────
INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
SELECT '20000000-0000-0000-0000-000000000010',
       '00000000-0000-0000-0000-000000000001',
       'PLATFORM_SUPER_ADMIN', 'Full platform access', TRUE, TRUE, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'PLATFORM_SUPER_ADMIN'
                  AND company_id = '00000000-0000-0000-0000-000000000001');

INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
SELECT '20000000-0000-0000-0000-000000000011',
       '00000000-0000-0000-0000-000000000001',
       'PLATFORM_ADMIN', 'Platform administration', TRUE, TRUE, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'PLATFORM_ADMIN'
                  AND company_id = '00000000-0000-0000-0000-000000000001');

INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
SELECT '20000000-0000-0000-0000-000000000012',
       '00000000-0000-0000-0000-000000000001',
       'PLATFORM_SUPPORT', 'Read-only support access', TRUE, TRUE, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'PLATFORM_SUPPORT'
                  AND company_id = '00000000-0000-0000-0000-000000000001');

INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
SELECT '20000000-0000-0000-0000-000000000013',
       '00000000-0000-0000-0000-000000000001',
       'PLATFORM_IMPLEMENTATION', 'Tenant implementation and configuration', TRUE, TRUE, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'PLATFORM_IMPLEMENTATION'
                  AND company_id = '00000000-0000-0000-0000-000000000001');

-- ─── 4. Tenant system roles (template — copied per tenant on onboarding) ─────
-- These are seeded under the platform tenant as templates.
-- When a new tenant is onboarded, these roles are cloned with the tenant's company_id.
INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
SELECT '20000000-0000-0000-0000-000000000020',
       '00000000-0000-0000-0000-000000000001',
       'TENANT_OWNER', 'Full access within tenant organization', TRUE, TRUE, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'TENANT_OWNER'
                  AND company_id = '00000000-0000-0000-0000-000000000001');

INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
SELECT '20000000-0000-0000-0000-000000000021',
       '00000000-0000-0000-0000-000000000001',
       'TENANT_ADMIN', 'Tenant administration', TRUE, TRUE, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'TENANT_ADMIN'
                  AND company_id = '00000000-0000-0000-0000-000000000001');

INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
SELECT '20000000-0000-0000-0000-000000000022',
       '00000000-0000-0000-0000-000000000001',
       'HR_ADMIN', 'HR module full access', TRUE, TRUE, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'HR_ADMIN'
                  AND company_id = '00000000-0000-0000-0000-000000000001');

INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
SELECT '20000000-0000-0000-0000-000000000023',
       '00000000-0000-0000-0000-000000000001',
       'HR_MANAGER', 'HR module management access', TRUE, TRUE, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'HR_MANAGER'
                  AND company_id = '00000000-0000-0000-0000-000000000001');

INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
SELECT '20000000-0000-0000-0000-000000000024',
       '00000000-0000-0000-0000-000000000001',
       'EMPLOYEE', 'Self-service employee access', TRUE, TRUE, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'EMPLOYEE'
                  AND company_id = '00000000-0000-0000-0000-000000000001');

-- ─── 5. Assign permissions to PLATFORM_SUPER_ADMIN ───────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT '20000000-0000-0000-0000-000000000010', id FROM permissions
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions
    WHERE role_id = '20000000-0000-0000-0000-000000000010'
    AND permission_id = permissions.id
);

-- ─── 6. Missing performance indexes ──────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_assets_assigned_emp  ON assets(assigned_to_employee_id);
CREATE INDEX IF NOT EXISTS idx_assets_status        ON assets(status);
CREATE INDEX IF NOT EXISTS idx_assets_warranty      ON assets(warranty_expiry);
CREATE INDEX IF NOT EXISTS idx_transfers_company    ON transfers(company_id);
CREATE INDEX IF NOT EXISTS idx_promotions_company   ON promotions(company_id);
CREATE INDEX IF NOT EXISTS idx_probation_company    ON probations(company_id);
CREATE INDEX IF NOT EXISTS idx_onboarding_emp       ON onboarding_checklists(employee_id);
CREATE INDEX IF NOT EXISTS idx_offboarding_emp      ON offboarding_checklists(employee_id);
CREATE INDEX IF NOT EXISTS idx_skill_company        ON employee_skills(company_id);
CREATE INDEX IF NOT EXISTS idx_cert_company         ON employee_certifications(company_id);
