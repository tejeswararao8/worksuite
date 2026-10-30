-- V3: Workflow Tables

CREATE TABLE workflow_definitions (
    id              CHAR(36)     NOT NULL PRIMARY KEY,
    company_id      CHAR(36)     NOT NULL,
    name            VARCHAR(255) NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    steps_config    TEXT,
    escalation_days INT          NOT NULL DEFAULT 3,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE workflow_instances (
    id              CHAR(36)     NOT NULL PRIMARY KEY,
    company_id      CHAR(36)     NOT NULL,
    definition_id   CHAR(36)     NOT NULL,
    entity_id       CHAR(36)     NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    current_step    INT          NOT NULL DEFAULT 1,
    initiated_by    CHAR(36)     NOT NULL,
    comments        TEXT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,
    version         BIGINT       NOT NULL DEFAULT 0,
    FOREIGN KEY (definition_id) REFERENCES workflow_definitions(id)
);

CREATE TABLE workflow_steps (
    id             CHAR(36)     NOT NULL PRIMARY KEY,
    company_id     CHAR(36)     NOT NULL,
    instance_id    CHAR(36)     NOT NULL,
    step_number    INT          NOT NULL,
    approver_id    CHAR(36),
    approver_role  VARCHAR(100),
    action         VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    comments       TEXT,
    acted_at       TIMESTAMP,
    due_at         TIMESTAMP,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP,
    version        BIGINT       NOT NULL DEFAULT 0,
    FOREIGN KEY (instance_id) REFERENCES workflow_instances(id)
);

CREATE INDEX idx_wf_instance_entity ON workflow_instances(entity_id, entity_type);
CREATE INDEX idx_wf_instance_status ON workflow_instances(status);
CREATE INDEX idx_wf_step_instance   ON workflow_steps(instance_id);
CREATE INDEX idx_wf_step_due        ON workflow_steps(action, due_at);
