-- V16: Tenant Employee Invitations and Employee Identity Linking
--
-- Adds:
--   1. keycloak_user_id column to employees — set when an invitation is accepted.
--   2. PENDING_INVITATION status to the employees employment_status check constraint.
--   3. tenant_employee_invitations table — mirrors platform_invitations but scoped
--      to a specific tenant and linked to a pre-created employee record.
--
-- Security design:
--   * Token hashes only — raw tokens are never stored.
--   * Unique constraint prevents duplicate active invitations per employee.
--   * Partial unique index: only one PENDING invitation per (tenant, employee).
--   * role_name is stored on the invitation and never accepted from the client
--     during acceptance — prevents role injection.
--   * keycloak_user_id on employees is set only on accepted invitation — never
--     assigned speculatively before acceptance.

-- ── 1. Add keycloak_user_id to employees ─────────────────────────────────────
ALTER TABLE employees
    ADD COLUMN IF NOT EXISTS keycloak_user_id VARCHAR(36);

CREATE INDEX IF NOT EXISTS idx_emp_keycloak_user
    ON employees(keycloak_user_id)
    WHERE keycloak_user_id IS NOT NULL;

-- ── 2. Add PENDING_INVITATION to employment_status ───────────────────────────
-- PostgreSQL does not support ALTER TABLE ... MODIFY CONSTRAINT directly.
-- We drop the old check and add a new one that includes PENDING_INVITATION.
-- The column default and existing data are unaffected.
ALTER TABLE employees
    DROP CONSTRAINT IF EXISTS employees_employment_status_check;

ALTER TABLE employees
    ADD CONSTRAINT employees_employment_status_check
    CHECK (employment_status IN (
        'ACTIVE', 'PROBATION', 'ON_LEAVE', 'RESIGNED',
        'TERMINATED', 'RETIRED', 'PENDING_INVITATION'
    ));

-- ── 3. Create tenant_employee_invitations table ───────────────────────────────
CREATE TABLE IF NOT EXISTS tenant_employee_invitations (
    id              UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL REFERENCES tenants(id),
    employee_id     UUID         NOT NULL REFERENCES employees(id),
    email           VARCHAR(255) NOT NULL,
    role_name       VARCHAR(100) NOT NULL,

    -- SHA-256 hex digest of the raw token. Raw token is NEVER stored.
    token_hash      VARCHAR(64)  NOT NULL UNIQUE,

    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    invited_by      VARCHAR(255) NOT NULL,   -- Keycloak user ID of the inviting admin
    expires_at      TIMESTAMP    NOT NULL,
    accepted_at     TIMESTAMP,
    keycloak_user_id VARCHAR(36),            -- set when invitation is accepted

    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_tenant_inv_status CHECK (
        status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'REVOKED')
    ),
    CONSTRAINT chk_tenant_inv_role CHECK (
        role_name IN (
            'TENANT_OWNER', 'TENANT_ADMIN', 'HR_ADMIN', 'HR_MANAGER', 'EMPLOYEE'
        )
    )
);

-- Only one PENDING invitation per employee at a time.
-- ACCEPTED/EXPIRED/REVOKED are historical — multiple are allowed.
CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_inv_employee_pending
    ON tenant_employee_invitations (employee_id)
    WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_tenant_inv_tenant   ON tenant_employee_invitations(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_inv_employee ON tenant_employee_invitations(employee_id);
CREATE INDEX IF NOT EXISTS idx_tenant_inv_email    ON tenant_employee_invitations(email);
CREATE INDEX IF NOT EXISTS idx_tenant_inv_status   ON tenant_employee_invitations(status);
CREATE INDEX IF NOT EXISTS idx_tenant_inv_hash     ON tenant_employee_invitations(token_hash);
