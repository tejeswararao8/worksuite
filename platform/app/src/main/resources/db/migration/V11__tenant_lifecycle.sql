-- V11: Tenant lifecycle statuses and audit trail
-- Adds PENDING_REVIEW and REJECTED to the tenant status domain.
-- Adds tenant_lifecycle_events for a full, immutable audit trail.

-- 1. Extend the status check constraint to include new values.
--    PostgreSQL does not support ALTER TYPE inside a transaction when the type
--    is used in a table, so we use a CHECK constraint approach instead.
--    The existing column is VARCHAR(50) — no DDL change needed for the values.
--    We add a CHECK constraint to document the valid domain.
ALTER TABLE tenants
    ADD CONSTRAINT chk_tenant_status
    CHECK (status IN ('ACTIVE','SUSPENDED','TRIAL','CANCELLED','PENDING_REVIEW','REJECTED'));

-- 2. Add columns needed for lifecycle management.
ALTER TABLE tenants
    ADD COLUMN IF NOT EXISTS reviewed_by  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS reviewed_at  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS review_notes TEXT;

-- 3. Tenant lifecycle events — immutable audit trail.
--    One row per status transition. Never updated or deleted.
CREATE TABLE tenant_lifecycle_events (
    id            CHAR(36)     NOT NULL PRIMARY KEY,
    tenant_id     CHAR(36)     NOT NULL,
    actor_id      VARCHAR(255) NOT NULL,   -- Keycloak user ID of the platform admin
    actor_role    VARCHAR(100) NOT NULL,   -- Role at time of action
    from_status   VARCHAR(50)  NOT NULL,
    to_status     VARCHAR(50)  NOT NULL,
    reason        TEXT,
    occurred_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE INDEX idx_lifecycle_tenant ON tenant_lifecycle_events(tenant_id);
CREATE INDEX idx_lifecycle_occurred ON tenant_lifecycle_events(occurred_at DESC);

-- 4. Seed: update the platform pseudo-tenant status to ACTIVE (already is, but
--    make explicit so the CHECK constraint passes on existing data).
UPDATE tenants SET status = 'ACTIVE' WHERE id = '00000000-0000-0000-0000-000000000001';
