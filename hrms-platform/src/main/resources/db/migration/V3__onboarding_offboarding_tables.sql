-- V3: Onboarding and Offboarding checklist tables

CREATE TABLE IF NOT EXISTS onboarding_checklists (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id       UUID NOT NULL,
    employee_id      UUID NOT NULL,
    task_name        VARCHAR(255) NOT NULL,
    description      TEXT,
    is_mandatory     BOOLEAN NOT NULL DEFAULT false,
    is_completed     BOOLEAN NOT NULL DEFAULT false,
    completed_by     UUID,
    completed_at     TIMESTAMP,
    sequence_order   INTEGER,
    is_active        BOOLEAN NOT NULL DEFAULT true,
    version          BIGINT NOT NULL DEFAULT 0,
    created_by       VARCHAR(255),
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_by       VARCHAR(255),
    updated_at       TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_onboarding_employee ON onboarding_checklists(employee_id, company_id);

CREATE TABLE IF NOT EXISTS offboarding_checklists (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id          UUID NOT NULL,
    employee_id         UUID NOT NULL,
    task_name           VARCHAR(255) NOT NULL,
    description         TEXT,
    is_mandatory        BOOLEAN NOT NULL DEFAULT false,
    is_completed        BOOLEAN NOT NULL DEFAULT false,
    completed_by        UUID,
    completed_at        TIMESTAMP,
    last_working_date   DATE,
    exit_reason         TEXT,
    offboarding_status  VARCHAR(50) DEFAULT 'IN_PROGRESS',
    is_active           BOOLEAN NOT NULL DEFAULT true,
    version             BIGINT NOT NULL DEFAULT 0,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_offboarding_employee ON offboarding_checklists(employee_id, company_id);
