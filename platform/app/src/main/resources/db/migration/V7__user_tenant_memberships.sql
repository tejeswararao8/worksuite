-- V7: User-Tenant Membership Model
-- Phase 3B: Proper membership tracking replacing implicit companyId-only access.
--
-- DESIGN:
--   One user (identified by keycloak_user_id = JWT sub) can belong to multiple tenants.
--   Each membership has a tenant-specific role and a lifecycle status.
--   Only ACTIVE memberships grant access — PENDING/SUSPENDED/REVOKED are rejected at the filter.
--
-- BACKFILL STRATEGY:
--   Existing users in the `users` table have a company_id column that reliably
--   identifies their tenant. We backfill ACTIVE memberships for all existing users
--   where keycloak_user_id is populated (set by V6 migration).
--   Users without a keycloak_user_id are NOT backfilled — they require manual reconciliation.
--   See MANUAL STEPS section at the bottom of this file.

-- ─── 1. Create user_tenant_memberships table ──────────────────────────────────
CREATE TABLE IF NOT EXISTS user_tenant_memberships (
    id                CHAR(36)     NOT NULL PRIMARY KEY,
    keycloak_user_id  VARCHAR(36)  NOT NULL,
    tenant_id         CHAR(36)     NOT NULL,
    role_name         VARCHAR(100) NOT NULL,
    membership_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    joined_at         TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_membership_user_tenant UNIQUE (keycloak_user_id, tenant_id),
    CONSTRAINT fk_membership_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT chk_membership_status CHECK (
        membership_status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'REVOKED')
    )
);

-- ─── 2. Indexes ───────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_utm_keycloak_user  ON user_tenant_memberships(keycloak_user_id);
CREATE INDEX IF NOT EXISTS idx_utm_tenant         ON user_tenant_memberships(tenant_id);
CREATE INDEX IF NOT EXISTS idx_utm_status         ON user_tenant_memberships(membership_status);
CREATE INDEX IF NOT EXISTS idx_utm_user_status    ON user_tenant_memberships(keycloak_user_id, membership_status);

-- ─── 3. Backfill ACTIVE memberships from existing users ──────────────────────
-- Only backfills users that have a keycloak_user_id (populated by V6 or manually).
-- Role defaults to EMPLOYEE for safety — admins must manually elevate roles.
-- The platform admin user (company_id = platform pseudo-tenant) gets PLATFORM_SUPER_ADMIN.
-- gen_random_uuid() requires pgcrypto on PostgreSQL < 13; on 13+ it is built-in.

INSERT INTO user_tenant_memberships (
    id, keycloak_user_id, tenant_id, role_name, membership_status, joined_at, created_at, updated_at
)
SELECT
    gen_random_uuid()::text,
    u.keycloak_user_id,
    u.company_id,
    CASE
        WHEN u.company_id = '00000000-0000-0000-0000-000000000001' THEN 'PLATFORM_SUPER_ADMIN'
        ELSE COALESCE(
            (SELECT r.name FROM user_roles ur
             JOIN roles r ON r.id = ur.role_id
             WHERE ur.user_id = u.id
             LIMIT 1),
            'EMPLOYEE'
        )
    END AS role_name,
    'ACTIVE',
    u.created_at,
    NOW(),
    NOW()
FROM users u
WHERE u.keycloak_user_id IS NOT NULL
  AND u.is_active = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM user_tenant_memberships m
      WHERE m.keycloak_user_id = u.keycloak_user_id
        AND m.tenant_id = u.company_id
  );

-- ─── MANUAL DATA RECONCILIATION REQUIRED ─────────────────────────────────────
-- Users in the `users` table with keycloak_user_id IS NULL were NOT backfilled.
-- To find them:
--   SELECT id, email, company_id FROM users WHERE keycloak_user_id IS NULL AND is_active = TRUE;
--
-- For each such user:
--   1. Look up their Keycloak user ID via Keycloak Admin Console or API:
--      GET /admin/realms/{realm}/users?email={email}
--   2. Update the users table:
--      UPDATE users SET keycloak_user_id = '<keycloak-uuid>' WHERE id = '<user-id>';
--   3. Insert the membership:
--      INSERT INTO user_tenant_memberships
--        (id, keycloak_user_id, tenant_id, role_name, membership_status, joined_at, created_at, updated_at)
--      VALUES (gen_random_uuid(), '<keycloak-uuid>', '<company-id>', 'EMPLOYEE', 'ACTIVE', NOW(), NOW(), NOW());
--
-- Do NOT invent memberships for users whose Keycloak identity cannot be confirmed.
