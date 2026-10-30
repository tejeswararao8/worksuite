-- V2: Audit, Notifications, Config, Documents

CREATE TABLE audit_logs (
    id             CHAR(36)     NOT NULL PRIMARY KEY,
    company_id     CHAR(36)     NOT NULL,
    entity_name    VARCHAR(100) NOT NULL,
    entity_id      VARCHAR(100) NOT NULL,
    action         VARCHAR(50)  NOT NULL,
    old_value      TEXT,
    new_value      TEXT,
    performed_by   VARCHAR(255),
    ip_address     VARCHAR(100),
    description    VARCHAR(500),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE notifications (
    id             CHAR(36)     NOT NULL PRIMARY KEY,
    company_id     CHAR(36)     NOT NULL,
    recipient_id   CHAR(36)     NOT NULL,
    title          VARCHAR(255) NOT NULL,
    message        TEXT         NOT NULL,
    type           VARCHAR(100) NOT NULL,
    channel        VARCHAR(50)  NOT NULL,
    is_read        BOOLEAN      NOT NULL DEFAULT FALSE,
    reference_id   VARCHAR(100),
    reference_type VARCHAR(100),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE platform_configs (
    id           CHAR(36)     NOT NULL PRIMARY KEY,
    company_id   CHAR(36)     NOT NULL,
    config_key   VARCHAR(255) NOT NULL,
    config_value TEXT,
    category     VARCHAR(100),
    description  VARCHAR(500),
    is_encrypted BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by   VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(255),
    updated_at   TIMESTAMP,
    version      BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (config_key, company_id)
);

CREATE TABLE documents (
    id                CHAR(36)     NOT NULL PRIMARY KEY,
    company_id        CHAR(36)     NOT NULL,
    entity_id         CHAR(36)     NOT NULL,
    entity_type       VARCHAR(100) NOT NULL,
    file_name         VARCHAR(255) NOT NULL,
    file_path         VARCHAR(500) NOT NULL,
    file_size         BIGINT,
    content_type      VARCHAR(100),
    document_type     VARCHAR(100),
    document_number   VARCHAR(100),
    issue_date        DATE,
    expiry_date       DATE,
    issuing_authority VARCHAR(255),
    expiry_status     VARCHAR(50)  NOT NULL DEFAULT 'VALID',
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by        VARCHAR(255),
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(255),
    updated_at        TIMESTAMP,
    version           BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_audit_company   ON audit_logs(company_id);
CREATE INDEX idx_audit_entity    ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_notif_recipient ON notifications(recipient_id, company_id);
CREATE INDEX idx_notif_unread    ON notifications(recipient_id, is_read);
CREATE INDEX idx_doc_entity      ON documents(entity_id, company_id);
CREATE INDEX idx_doc_expiry      ON documents(expiry_date);
