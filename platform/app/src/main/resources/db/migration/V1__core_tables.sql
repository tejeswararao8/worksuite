-- V1: Core Platform Tables
-- tenants, roles, permissions, users

CREATE TABLE tenants (
    id           CHAR(36)     NOT NULL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    slug         VARCHAR(100) NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL UNIQUE,
    phone        VARCHAR(50),
    logo_path    VARCHAR(500),
    status       VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    module      VARCHAR(100) NOT NULL
);

CREATE TABLE roles (
    id             CHAR(36)     NOT NULL PRIMARY KEY,
    company_id     CHAR(36)     NOT NULL,
    name           VARCHAR(100) NOT NULL,
    description    VARCHAR(255),
    is_system_role BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP,
    version        BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (name, company_id)
);

CREATE TABLE role_permissions (
    role_id       CHAR(36) NOT NULL,
    permission_id CHAR(36) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id)       REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE users (
    id                    CHAR(36)     NOT NULL PRIMARY KEY,
    company_id            CHAR(36)     NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    first_name            VARCHAR(100),
    last_name             VARCHAR(100),
    status                VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          TIMESTAMP,
    must_change_password  BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at         TIMESTAMP,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by            VARCHAR(255),
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by            VARCHAR(255),
    updated_at            TIMESTAMP,
    version               BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (email, company_id),
    FOREIGN KEY (company_id) REFERENCES tenants(id)
);

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX idx_users_company ON users(company_id);
CREATE INDEX idx_users_email   ON users(email);
CREATE INDEX idx_roles_company ON roles(company_id);
