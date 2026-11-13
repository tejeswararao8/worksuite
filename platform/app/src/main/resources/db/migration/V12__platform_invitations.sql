-- V12: Platform Employee Invitations
-- Platform employees are UserTenantMembership records in the platform pseudo-tenant.
-- This table tracks pending invitations before the invitee accepts and logs in.

CREATE TABLE platform_invitations (
    id              UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    role_name       VARCHAR(100) NOT NULL,
    token           VARCHAR(64)  NOT NULL UNIQUE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    invited_by      VARCHAR(255) NOT NULL,   -- Keycloak user ID of the inviting admin
    expires_at      TIMESTAMP    NOT NULL,
    accepted_at     TIMESTAMP,
    keycloak_user_id VARCHAR(36),            -- set when invitation is accepted
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'REVOKED')),
    CONSTRAINT chk_invitation_role CHECK (role_name IN (
        'PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'PLATFORM_IMPLEMENTATION'
    ))
);

CREATE INDEX idx_invitation_email  ON platform_invitations(email);
CREATE INDEX idx_invitation_token  ON platform_invitations(token);
CREATE INDEX idx_invitation_status ON platform_invitations(status);
