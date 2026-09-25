-- V2__seed_data.sql
-- Default Permissions
INSERT INTO permissions (id, company_id, name, description, module, created_at, updated_at, version, is_active) VALUES
(gen_random_uuid(), NULL, 'EMPLOYEE_CREATE', 'Create employees', 'EMPLOYEE', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'EMPLOYEE_VIEW', 'View employees', 'EMPLOYEE', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'EMPLOYEE_UPDATE', 'Update employees', 'EMPLOYEE', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'EMPLOYEE_DELETE', 'Delete employees', 'EMPLOYEE', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'EMPLOYEE_EXPORT', 'Export employees', 'EMPLOYEE', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'DOCUMENT_CREATE', 'Upload documents', 'DOCUMENT', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'DOCUMENT_VIEW', 'View documents', 'DOCUMENT', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'DOCUMENT_DELETE', 'Delete documents', 'DOCUMENT', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'ASSET_CREATE', 'Create assets', 'ASSET', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'ASSET_VIEW', 'View assets', 'ASSET', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'ASSET_UPDATE', 'Update assets', 'ASSET', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'REPORT_EXPORT', 'Export reports', 'REPORT', NOW(), NOW(), 0, TRUE),
(gen_random_uuid(), NULL, 'AUDIT_VIEW', 'View audit logs', 'AUDIT', NOW(), NOW(), 0, TRUE);
