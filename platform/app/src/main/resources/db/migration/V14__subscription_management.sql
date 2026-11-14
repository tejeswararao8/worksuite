-- V14: Subscription Plans and Tenant Subscriptions
--
-- subscription_plans  — platform-managed plan catalogue (prices stored server-side only)
-- tenant_subscriptions — one active subscription per tenant at a time
-- subscription_history — append-only audit trail of every subscription change
--
-- Design decisions:
--   * Prices are stored in the database (integer cents) and never accepted from the browser.
--   * enabled_modules is a comma-separated list of module keys (e.g. 'HR,PAYROLL,RECRUITMENT').
--   * max_employees = -1 means unlimited.
--   * storage_gb = -1 means unlimited.
--   * tenant_subscriptions has a partial unique index so only one ACTIVE/TRIAL/PAST_DUE
--     subscription can exist per tenant at a time.

-- ── Subscription plans ────────────────────────────────────────────────────────

CREATE TABLE subscription_plans (
    id                  UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(100) NOT NULL UNIQUE,
    description         TEXT,
    monthly_price_cents INTEGER      NOT NULL DEFAULT 0,   -- price in smallest currency unit
    annual_price_cents  INTEGER      NOT NULL DEFAULT 0,
    trial_days          INTEGER      NOT NULL DEFAULT 14,
    max_employees       INTEGER      NOT NULL DEFAULT 50,  -- -1 = unlimited
    storage_gb          INTEGER      NOT NULL DEFAULT 5,   -- -1 = unlimited
    enabled_modules     TEXT         NOT NULL DEFAULT 'HR',-- comma-separated module keys
    plan_status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_plan_status CHECK (plan_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_monthly_price CHECK (monthly_price_cents >= 0),
    CONSTRAINT chk_annual_price  CHECK (annual_price_cents  >= 0),
    CONSTRAINT chk_trial_days    CHECK (trial_days >= 0),
    CONSTRAINT chk_max_employees CHECK (max_employees >= -1),
    CONSTRAINT chk_storage_gb    CHECK (storage_gb >= -1)
);

-- ── Tenant subscriptions ──────────────────────────────────────────────────────

CREATE TABLE tenant_subscriptions (
    id              UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL REFERENCES tenants(id),
    plan_id         UUID         NOT NULL REFERENCES subscription_plans(id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'TRIAL',
    billing_cycle   VARCHAR(10)  NOT NULL DEFAULT 'MONTHLY', -- MONTHLY | ANNUAL
    started_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    trial_ends_at   TIMESTAMP,
    current_period_start TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    current_period_end   TIMESTAMP,
    cancelled_at    TIMESTAMP,
    notes           TEXT,
    assigned_by     VARCHAR(255) NOT NULL,   -- Keycloak user ID of platform admin
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_sub_status CHECK (status IN (
        'TRIAL', 'ACTIVE', 'PAST_DUE', 'SUSPENDED', 'EXPIRED', 'CANCELLED'
    )),
    CONSTRAINT chk_billing_cycle CHECK (billing_cycle IN ('MONTHLY', 'ANNUAL'))
);

-- Only one non-terminal subscription per tenant at a time.
-- EXPIRED and CANCELLED are terminal — multiple historical records are allowed.
CREATE UNIQUE INDEX uq_tenant_active_subscription
    ON tenant_subscriptions (tenant_id)
    WHERE status IN ('TRIAL', 'ACTIVE', 'PAST_DUE', 'SUSPENDED');

CREATE INDEX idx_sub_tenant   ON tenant_subscriptions(tenant_id);
CREATE INDEX idx_sub_plan     ON tenant_subscriptions(plan_id);
CREATE INDEX idx_sub_status   ON tenant_subscriptions(status);
CREATE INDEX idx_sub_period   ON tenant_subscriptions(current_period_end);

-- ── Subscription history ──────────────────────────────────────────────────────

CREATE TABLE subscription_history (
    id              UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID         NOT NULL REFERENCES tenant_subscriptions(id),
    tenant_id       UUID         NOT NULL REFERENCES tenants(id),
    plan_id         UUID         NOT NULL REFERENCES subscription_plans(id),
    from_status     VARCHAR(20),
    to_status       VARCHAR(20)  NOT NULL,
    from_plan_id    UUID         REFERENCES subscription_plans(id),
    to_plan_id      UUID         NOT NULL REFERENCES subscription_plans(id),
    actor_id        VARCHAR(255) NOT NULL,
    reason          TEXT,
    occurred_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sub_history_sub    ON subscription_history(subscription_id);
CREATE INDEX idx_sub_history_tenant ON subscription_history(tenant_id);

-- ── Seed: default plans ───────────────────────────────────────────────────────
-- Prices in USD cents. Plans are ACTIVE by default.

INSERT INTO subscription_plans
    (id, name, description, monthly_price_cents, annual_price_cents,
     trial_days, max_employees, storage_gb, enabled_modules, plan_status)
VALUES
    (gen_random_uuid(), 'Starter',
     'For small teams getting started with HR management.',
     2900, 29000, 14, 25, 5,
     'HR', 'ACTIVE'),

    (gen_random_uuid(), 'Growth',
     'For growing companies needing full HR capabilities.',
     7900, 79000, 14, 100, 20,
     'HR,PAYROLL,RECRUITMENT', 'ACTIVE'),

    (gen_random_uuid(), 'Professional',
     'For established organizations with advanced HR needs.',
     14900, 149000, 14, 500, 100,
     'HR,PAYROLL,RECRUITMENT,PERFORMANCE,ATTENDANCE', 'ACTIVE'),

    (gen_random_uuid(), 'Enterprise',
     'Unlimited scale for large enterprises.',
     29900, 299000, 30, -1, -1,
     'HR,PAYROLL,RECRUITMENT,PERFORMANCE,ATTENDANCE,LEARNING,ANALYTICS', 'ACTIVE');
