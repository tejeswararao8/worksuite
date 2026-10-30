-- V5: HR Module Tables

CREATE TABLE branches (
    id             CHAR(36)     NOT NULL PRIMARY KEY,
    company_id     CHAR(36)     NOT NULL,
    name           VARCHAR(255) NOT NULL,
    code           VARCHAR(50)  NOT NULL,
    address        TEXT,
    city           VARCHAR(100),
    country        VARCHAR(100),
    phone          VARCHAR(50),
    email          VARCHAR(255),
    is_head_office BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP,
    version        BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (code, company_id)
);

CREATE TABLE departments (
    id                   CHAR(36)     NOT NULL PRIMARY KEY,
    company_id           CHAR(36)     NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    code                 VARCHAR(50)  NOT NULL,
    description          VARCHAR(500),
    parent_department_id CHAR(36),
    head_employee_id     CHAR(36),
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by           VARCHAR(255),
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(255),
    updated_at           TIMESTAMP,
    version              BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (code, company_id)
);

CREATE TABLE designations (
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    company_id  CHAR(36)     NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    level       INT,
    grade       VARCHAR(50),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by  VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(255),
    updated_at  TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE teams (
    id               CHAR(36)     NOT NULL PRIMARY KEY,
    company_id       CHAR(36)     NOT NULL,
    name             VARCHAR(255) NOT NULL,
    description      VARCHAR(500),
    department_id    CHAR(36)     NOT NULL,
    lead_employee_id CHAR(36),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by       VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),
    updated_at       TIMESTAMP,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE employees (
    id                  CHAR(36)     NOT NULL PRIMARY KEY,
    company_id          CHAR(36)     NOT NULL,
    employee_code       VARCHAR(20)  NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    mobile              VARCHAR(50),
    alternate_mobile    VARCHAR(50),
    gender              VARCHAR(20),
    date_of_birth       DATE,
    nationality         VARCHAR(100),
    marital_status      VARCHAR(50),
    blood_group         VARCHAR(10),
    photo_url           VARCHAR(500),
    current_address     TEXT,
    permanent_address   TEXT,
    joining_date        DATE         NOT NULL,
    branch_id           CHAR(36),
    department_id       CHAR(36),
    team_id             CHAR(36),
    designation_id      CHAR(36),
    manager_id          CHAR(36),
    work_location       VARCHAR(255),
    employment_status   VARCHAR(50)  NOT NULL DEFAULT 'PROBATION',
    employment_type     VARCHAR(50),
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP,
    version             BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (email, company_id),
    UNIQUE (employee_code, company_id)
);

CREATE TABLE emergency_contacts (
    id               CHAR(36)     NOT NULL PRIMARY KEY,
    company_id       CHAR(36)     NOT NULL,
    employee_id      CHAR(36)     NOT NULL,
    name             VARCHAR(255) NOT NULL,
    relationship     VARCHAR(100),
    phone            VARCHAR(50)  NOT NULL,
    alternate_phone  VARCHAR(50),
    email            VARCHAR(255),
    address          TEXT,
    is_primary       BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by       VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),
    updated_at       TIMESTAMP,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE bank_details (
    id               CHAR(36)     NOT NULL PRIMARY KEY,
    company_id       CHAR(36)     NOT NULL,
    employee_id      CHAR(36)     NOT NULL,
    bank_name        VARCHAR(255) NOT NULL,
    account_number   VARCHAR(100) NOT NULL,
    account_name     VARCHAR(255) NOT NULL,
    iban             VARCHAR(100),
    swift_code       VARCHAR(50),
    branch_name      VARCHAR(255),
    is_primary       BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by       VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),
    updated_at       TIMESTAMP,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE probations (
    id                CHAR(36)   NOT NULL PRIMARY KEY,
    company_id        CHAR(36)   NOT NULL,
    employee_id       CHAR(36)   NOT NULL,
    start_date        DATE       NOT NULL,
    end_date          DATE       NOT NULL,
    extended_end_date DATE,
    status            VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    remarks           TEXT,
    approved_by       CHAR(36),
    approved_at       TIMESTAMP,
    is_active         BOOLEAN    NOT NULL DEFAULT TRUE,
    created_by        VARCHAR(255),
    created_at        TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(255),
    updated_at        TIMESTAMP,
    version           BIGINT     NOT NULL DEFAULT 0
);

CREATE TABLE transfers (
    id                 CHAR(36)   NOT NULL PRIMARY KEY,
    company_id         CHAR(36)   NOT NULL,
    employee_id        CHAR(36)   NOT NULL,
    transfer_type      VARCHAR(50) NOT NULL,
    from_department_id CHAR(36),
    to_department_id   CHAR(36),
    from_branch_id     CHAR(36),
    to_branch_id       CHAR(36),
    from_manager_id    CHAR(36),
    to_manager_id      CHAR(36),
    from_team_id       CHAR(36),
    to_team_id         CHAR(36),
    effective_date     DATE       NOT NULL,
    reason             TEXT,
    status             VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by        CHAR(36),
    approved_at        TIMESTAMP,
    is_active          BOOLEAN    NOT NULL DEFAULT TRUE,
    created_by         VARCHAR(255),
    created_at         TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),
    updated_at         TIMESTAMP,
    version            BIGINT     NOT NULL DEFAULT 0
);

CREATE TABLE promotions (
    id                  CHAR(36)   NOT NULL PRIMARY KEY,
    company_id          CHAR(36)   NOT NULL,
    employee_id         CHAR(36)   NOT NULL,
    from_designation_id CHAR(36),
    to_designation_id   CHAR(36)   NOT NULL,
    effective_date      DATE       NOT NULL,
    reason              TEXT,
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by         CHAR(36),
    approved_at         TIMESTAMP,
    is_active           BOOLEAN    NOT NULL DEFAULT TRUE,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP,
    version             BIGINT     NOT NULL DEFAULT 0
);

CREATE TABLE assets (
    id                      CHAR(36)     NOT NULL PRIMARY KEY,
    company_id              CHAR(36)     NOT NULL,
    asset_code              VARCHAR(50)  NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    asset_type              VARCHAR(50)  NOT NULL,
    serial_number           VARCHAR(100),
    model                   VARCHAR(100),
    brand                   VARCHAR(100),
    purchase_date           DATE,
    warranty_expiry         DATE,
    status                  VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE',
    assigned_to_employee_id CHAR(36),
    assigned_at             TIMESTAMP,
    returned_at             TIMESTAMP,
    notes                   TEXT,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by              VARCHAR(255),
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              VARCHAR(255),
    updated_at              TIMESTAMP,
    version                 BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (asset_code, company_id)
);

CREATE TABLE employee_skills (
    id                  CHAR(36)     NOT NULL PRIMARY KEY,
    company_id          CHAR(36)     NOT NULL,
    employee_id         CHAR(36)     NOT NULL,
    skill_name          VARCHAR(255) NOT NULL,
    skill_type          VARCHAR(100),
    proficiency         VARCHAR(50),
    years_of_experience INT,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP,
    version             BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE employee_certifications (
    id             CHAR(36)     NOT NULL PRIMARY KEY,
    company_id     CHAR(36)     NOT NULL,
    employee_id    CHAR(36)     NOT NULL,
    name           VARCHAR(255) NOT NULL,
    issuing_org    VARCHAR(255),
    issue_date     DATE,
    expiry_date    DATE,
    credential_url VARCHAR(500),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE onboarding_checklists (
    id           CHAR(36)     NOT NULL PRIMARY KEY,
    company_id   CHAR(36)     NOT NULL,
    employee_id  CHAR(36)     NOT NULL,
    task_name    VARCHAR(255) NOT NULL,
    description  TEXT,
    status       VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    assigned_to  CHAR(36),
    due_date     DATE,
    completed_at DATE,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by   VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(255),
    updated_at   TIMESTAMP,
    version      BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE offboarding_checklists (
    id           CHAR(36)     NOT NULL PRIMARY KEY,
    company_id   CHAR(36)     NOT NULL,
    employee_id  CHAR(36)     NOT NULL,
    task_name    VARCHAR(255) NOT NULL,
    description  TEXT,
    status       VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    assigned_to  CHAR(36),
    due_date     DATE,
    completed_at DATE,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by   VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(255),
    updated_at   TIMESTAMP,
    version      BIGINT       NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_emp_company    ON employees(company_id);
CREATE INDEX idx_emp_dept       ON employees(department_id);
CREATE INDEX idx_emp_status     ON employees(employment_status);
CREATE INDEX idx_branch_company ON branches(company_id);
CREATE INDEX idx_dept_company   ON departments(company_id);
CREATE INDEX idx_team_dept      ON teams(department_id);
CREATE INDEX idx_probation_emp  ON probations(employee_id);
CREATE INDEX idx_transfer_emp   ON transfers(employee_id);
CREATE INDEX idx_promotion_emp  ON promotions(employee_id);
CREATE INDEX idx_asset_company  ON assets(company_id);
CREATE INDEX idx_skill_emp      ON employee_skills(employee_id);
CREATE INDEX idx_cert_emp       ON employee_certifications(employee_id);
CREATE INDEX idx_emergency_emp  ON emergency_contacts(employee_id);
CREATE INDEX idx_bank_emp       ON bank_details(employee_id);
