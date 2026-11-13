-- V13: Replace plaintext invitation token with a SHA-256 hash.
--
-- Security rationale:
--   Storing raw tokens in the database means a database read grants the ability
--   to accept any pending invitation. Storing only the SHA-256 hex digest means
--   the raw token is never at rest — it exists only in the email link.
--
-- Migration strategy:
--   1. Add the new token_hash column (nullable initially).
--   2. Revoke all existing PENDING invitations — their raw tokens are now
--      unrecoverable and cannot be re-hashed. Admins must resend.
--   3. Drop the old token column.
--   4. Add NOT NULL + UNIQUE constraints on token_hash.
--
-- This is a forward-only migration. V12 is not modified.

-- Step 1: Add token_hash column (nullable to allow the UPDATE below)
ALTER TABLE platform_invitations
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(64);

-- Step 2: Revoke all PENDING invitations — raw tokens cannot be hashed retroactively.
--         Admins must resend invitations after this migration.
UPDATE platform_invitations
SET    status     = 'REVOKED',
       updated_at = CURRENT_TIMESTAMP
WHERE  status = 'PENDING';

-- Step 3: Set a placeholder hash for non-PENDING rows so NOT NULL can be applied.
--         ACCEPTED/EXPIRED/REVOKED invitations are historical records; their token
--         is no longer usable. We store a deterministic sentinel derived from the id.
UPDATE platform_invitations
SET    token_hash = encode(digest(id::text, 'sha256'), 'hex')
WHERE  token_hash IS NULL;

-- Step 4: Apply NOT NULL and UNIQUE constraints.
ALTER TABLE platform_invitations
    ALTER COLUMN token_hash SET NOT NULL;

ALTER TABLE platform_invitations
    ADD CONSTRAINT uq_invitation_token_hash UNIQUE (token_hash);

-- Step 5: Drop the old plaintext token column.
ALTER TABLE platform_invitations
    DROP COLUMN IF EXISTS token;

-- Step 6: Update the index to point to the new column.
DROP INDEX IF EXISTS idx_invitation_token;
CREATE INDEX idx_invitation_token_hash ON platform_invitations(token_hash);
