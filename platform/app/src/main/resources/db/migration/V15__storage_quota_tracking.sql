-- V15: Storage quota tracking on tenant_subscriptions
--
-- Adds storage_used_bytes to tenant_subscriptions so the platform can enforce
-- per-tenant storage quotas without querying MinIO on every upload.
--
-- Design decisions:
--   * storage_used_bytes is updated atomically via UPDATE ... WHERE id = ?
--     combined with a CHECK constraint to prevent negative values.
--   * The column is on tenant_subscriptions (not a separate table) so the
--     quota check and the subscription entitlement read are a single row fetch.
--   * Existing rows default to 0 (no usage recorded before this migration).

ALTER TABLE tenant_subscriptions
    ADD COLUMN storage_used_bytes BIGINT NOT NULL DEFAULT 0;

ALTER TABLE tenant_subscriptions
    ADD CONSTRAINT chk_storage_used_bytes CHECK (storage_used_bytes >= 0);

COMMENT ON COLUMN tenant_subscriptions.storage_used_bytes
    IS 'Cumulative bytes stored by this tenant under the current subscription period.';
