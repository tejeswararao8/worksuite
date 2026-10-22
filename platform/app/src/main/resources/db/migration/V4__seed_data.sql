-- V4: Seed default permissions and super admin tenant

INSERT INTO tenants (id, name, slug, email, status, is_active, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Platform Admin', 'platform', 'admin@platform.com', 'ACTIVE', TRUE, NOW());

-- Core permissions
INSERT INTO permissions (id, name, description, module) VALUES
('10000000-0000-0000-0000-000000000001', 'TENANT_READ',       'View tenants',         'ADMIN'),
('10000000-0000-0000-0000-000000000002', 'TENANT_WRITE',      'Manage tenants',       'ADMIN'),
('10000000-0000-0000-0000-000000000003', 'USER_READ',         'View users',           'SECURITY'),
('10000000-0000-0000-0000-000000000004', 'USER_WRITE',        'Manage users',         'SECURITY'),
('10000000-0000-0000-0000-000000000005', 'ROLE_READ',         'View roles',           'SECURITY'),
('10000000-0000-0000-0000-000000000006', 'ROLE_WRITE',        'Manage roles',         'SECURITY'),
('10000000-0000-0000-0000-000000000007', 'AUDIT_READ',        'View audit logs',      'AUDIT'),
('10000000-0000-0000-0000-000000000008', 'DOCUMENT_READ',     'View documents',       'DOCUMENT'),
('10000000-0000-0000-0000-000000000009', 'DOCUMENT_WRITE',    'Manage documents',     'DOCUMENT'),
('10000000-0000-0000-0000-000000000010', 'WORKFLOW_READ',     'View workflows',       'WORKFLOW'),
('10000000-0000-0000-0000-000000000011', 'WORKFLOW_WRITE',    'Manage workflows',     'WORKFLOW'),
('10000000-0000-0000-0000-000000000012', 'REPORT_EXPORT',     'Export reports',       'REPORTING'),
('10000000-0000-0000-0000-000000000013', 'CONFIG_READ',       'View config',          'CONFIG'),
('10000000-0000-0000-0000-000000000014', 'CONFIG_WRITE',      'Manage config',        'CONFIG'),
('10000000-0000-0000-0000-000000000015', 'NOTIFICATION_READ', 'View notifications',   'NOTIFICATION');

-- Super admin role
INSERT INTO roles (id, company_id, name, description, is_system_role, is_active, created_at, version)
VALUES ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001',
        'SUPER_ADMIN', 'Platform super administrator', TRUE, TRUE, NOW(), 0);

-- Assign all permissions to super admin
INSERT INTO role_permissions (role_id, permission_id)
SELECT '20000000-0000-0000-0000-000000000001', id FROM permissions;

-- Default admin user (password: Admin@1234 — BCrypt hashed)
INSERT INTO users (id, company_id, email, password_hash, first_name, last_name,
                   status, is_active, must_change_password, failed_login_attempts, created_at, version)
VALUES ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001',
        'admin@platform.com',
        '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
        'Platform', 'Admin', 'ACTIVE', TRUE, TRUE, 0, NOW(), 0);

INSERT INTO user_roles (user_id, role_id)
VALUES ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001');
