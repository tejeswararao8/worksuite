# Platform Architecture

## 1. Module Dependency Graph

```mermaid
graph TD
    subgraph CORE["Core Platform"]
        CT[core-tenancy]
        CA[core-api]
        CS[core-security]
        CAU[core-audit]
        CL[core-logging]
        CC[core-config]
        CN[core-notification]
        CD[core-document]
        CW[core-workflow]
        CR[core-reporting]
    end

    subgraph MODULES["Business Modules"]
        MHR[module-hr]
        MFIN[module-finance 🔜]
        MCRM[module-crm 🔜]
    end

    subgraph APP["Application"]
        APP[app]
    end

    CT --> CA
    CT --> CS
    CT --> CL
    CA --> CS
    CS --> CAU
    CS --> CC
    CC --> CN
    CN --> CD
    CN --> CW
    CN --> CR

    CT --> MHR
    CA --> MHR
    CS --> MHR
    CAU --> MHR
    CL --> MHR
    CC --> MHR
    CN --> MHR
    CD --> MHR
    CW --> MHR
    CR --> MHR

    MHR --> APP
    MFIN --> APP
    MCRM --> APP

    style CT fill:#e8f4f8,stroke:#2196F3
    style CA fill:#e8f4f8,stroke:#2196F3
    style CS fill:#e8f4f8,stroke:#2196F3
    style CAU fill:#e8f4f8,stroke:#2196F3
    style CL fill:#e8f4f8,stroke:#2196F3
    style CC fill:#e8f4f8,stroke:#2196F3
    style CN fill:#e8f4f8,stroke:#2196F3
    style CD fill:#e8f4f8,stroke:#2196F3
    style CW fill:#e8f4f8,stroke:#2196F3
    style CR fill:#e8f4f8,stroke:#2196F3
    style MHR fill:#e8f8e8,stroke:#4CAF50
    style MFIN fill:#f5f5f5,stroke:#9E9E9E
    style MCRM fill:#f5f5f5,stroke:#9E9E9E
    style APP fill:#fff3e0,stroke:#FF9800
```

---

## 2. Request Lifecycle

```mermaid
sequenceDiagram
    participant C as Client
    participant MF as MdcFilter<br/>[core-logging]
    participant JF as JwtAuthFilter<br/>[core-security]
    participant TI as TenantInterceptor<br/>[core-tenancy]
    participant CTL as Controller<br/>[module-*]
    participant SVC as Service<br/>[module-*]
    participant AA as AuditAspect<br/>[core-audit]
    participant REPO as Repository<br/>[module-*]
    participant DB as Database

    C->>MF: HTTP Request
    MF->>MF: inject traceId into MDC
    MF->>JF: forward
    JF->>JF: validate JWT, extract userId + companyId
    JF->>JF: set SecurityContext
    JF->>JF: set request.attribute("tenantId")
    JF->>TI: forward
    TI->>TI: TenantContext.setTenantId(companyId)
    TI->>CTL: forward
    CTL->>SVC: call service method
    SVC->>AA: @Auditable intercept (Around)
    AA->>REPO: proceed with business logic
    REPO->>DB: SELECT/INSERT WHERE company_id = ?
    DB-->>REPO: result
    REPO-->>SVC: result
    AA->>AA: async log to audit_logs
    SVC-->>CTL: result
    CTL-->>C: ApiResponse<T>
    MF->>MF: log method + URI + status + duration
    MF->>MF: MDC.clear()
    TI->>TI: TenantContext.clear()
```

---

## 3. Multi-Tenancy Strategy

```mermaid
graph LR
    subgraph Request["Single HTTP Request"]
        JWT["JWT Token<br/>sub: userId<br/>companyId: uuid<br/>roles: HR_MANAGER"]
        JF2["JwtAuthFilter<br/>extracts companyId"]
        TC["TenantContext<br/>ThreadLocal&lt;String&gt;"]
        BE["BaseEntity<br/>@PrePersist<br/>sets companyId"]
        REPO2["Repository<br/>findByIdAndCompanyId()"]
    end

    JWT --> JF2
    JF2 --> TC
    TC --> BE
    TC --> REPO2

    subgraph DB["Database (shared schema)"]
        T1["tenant A rows<br/>company_id = aaa"]
        T2["tenant B rows<br/>company_id = bbb"]
        T3["tenant C rows<br/>company_id = ccc"]
    end

    REPO2 -->|WHERE company_id = ?| DB
```

**Strategy: Shared schema, row-level isolation via `company_id`**

Every table has `company_id CHAR(36) NOT NULL`. Every query is automatically scoped. No tenant can ever see another tenant's data.

---

## 4. Security Architecture

```mermaid
graph TD
    subgraph AUTH["Authentication Flow"]
        LOGIN["POST /auth/login<br/>email + password"]
        VALIDATE["Validate credentials<br/>check locked/inactive"]
        ISSUE["Issue tokens<br/>accessToken 24h<br/>refreshToken 7d"]
        BLACKLIST["TokenBlacklist<br/>Caffeine cache<br/>on logout"]
    end

    subgraph REQUEST["Per-Request Auth"]
        HEADER["Authorization: Bearer token"]
        JWTFILTER["JwtAuthFilter<br/>validate + parse"]
        SECCTX["SecurityContext<br/>userId + authorities"]
        RBAC["@PreAuthorize<br/>hasAuthority('EMPLOYEE_WRITE')"]
    end

    subgraph RBAC_MODEL["RBAC Model"]
        USER["User"]
        ROLE["Role (per tenant)"]
        PERM["Permission<br/>EMPLOYEE_READ<br/>EMPLOYEE_WRITE<br/>etc."]
    end

    LOGIN --> VALIDATE --> ISSUE
    ISSUE --> BLACKLIST

    HEADER --> JWTFILTER
    JWTFILTER -->|blacklisted?| BLACKLIST
    JWTFILTER --> SECCTX --> RBAC

    USER -->|many-to-many| ROLE
    ROLE -->|many-to-many| PERM
```

---

## 5. Core Module Responsibilities

```mermaid
graph LR
    subgraph WHAT_CORE_PROVIDES["What Core Provides to Every Module"]
        direction TB
        A["🏢 core-tenancy<br/>BaseEntity, TenantContext<br/>auto company_id on every row"]
        B["📡 core-api<br/>ApiResponse&lt;T&gt;, PagedResponse&lt;T&gt;<br/>PlatformException, GlobalExceptionHandler"]
        C["🔐 core-security<br/>JWT tokens, TokenBlacklist<br/>User, Role, Permission, UserPrincipal"]
        D["📋 core-audit<br/>@Auditable annotation<br/>async AuditLog on every write"]
        E["🔍 core-logging<br/>traceId in every log line<br/>slow query detection"]
        F["⚙️ core-config<br/>per-tenant key/value config<br/>Caffeine cached"]
        G["🔔 core-notification<br/>in-app + email<br/>async, non-blocking"]
        H["📁 core-document<br/>local file storage<br/>expiry scheduler + reminders"]
        I["✅ core-workflow<br/>approval state machine<br/>escalation scheduler"]
        J["📊 core-reporting<br/>Excel / CSV / PDF<br/>one call: ReportService.export()"]
    end

    subgraph MODULE["Your Business Module"]
        M["module-xyz<br/>Entity extends BaseEntity<br/>Controller returns ApiResponse<br/>Service uses @Auditable<br/>Zero boilerplate"]
    end

    A --> M
    B --> M
    C --> M
    D --> M
    E --> M
    F --> M
    G --> M
    H --> M
    I --> M
    J --> M
```

---

## 6. Data Architecture

```mermaid
erDiagram
    tenants {
        char id PK
        varchar name
        varchar slug UK
        varchar email UK
        varchar status
    }

    users {
        char id PK
        char company_id FK
        varchar email
        varchar password_hash
        varchar status
        int failed_login_attempts
    }

    roles {
        char id PK
        char company_id FK
        varchar name
        boolean is_system_role
    }

    permissions {
        char id PK
        varchar name UK
        varchar module
    }

    audit_logs {
        char id PK
        char company_id FK
        varchar entity_name
        varchar entity_id
        varchar action
        text old_value
        text new_value
        varchar performed_by
    }

    notifications {
        char id PK
        char company_id FK
        char recipient_id FK
        varchar title
        varchar type
        varchar channel
        boolean is_read
    }

    documents {
        char id PK
        char company_id FK
        char entity_id
        varchar entity_type
        varchar file_path
        date expiry_date
        varchar expiry_status
    }

    workflow_instances {
        char id PK
        char company_id FK
        char entity_id
        varchar entity_type
        varchar status
        int current_step
    }

    platform_configs {
        char id PK
        char company_id FK
        varchar config_key
        text config_value
        varchar category
    }

    tenants ||--o{ users : "has"
    tenants ||--o{ roles : "has"
    users }o--o{ roles : "user_roles"
    roles }o--o{ permissions : "role_permissions"
    tenants ||--o{ audit_logs : "scoped to"
    tenants ||--o{ notifications : "scoped to"
    tenants ||--o{ documents : "scoped to"
    tenants ||--o{ workflow_instances : "scoped to"
    tenants ||--o{ platform_configs : "scoped to"
```

---

## 7. Workflow State Machine

```mermaid
stateDiagram-v2
    [*] --> PENDING : WorkflowService.start()
    PENDING --> IN_PROGRESS : first step actioned
    IN_PROGRESS --> APPROVED : all steps approved
    IN_PROGRESS --> REJECTED : any step rejected
    IN_PROGRESS --> ESCALATED : step overdue (hourly cron)
    ESCALATED --> IN_PROGRESS : manual re-assignment
    APPROVED --> [*]
    REJECTED --> [*]
```

---

## 8. Document Expiry Lifecycle

```mermaid
graph LR
    UPLOAD["Document Uploaded<br/>expiry_status = VALID"]
    D90["90 days before expiry<br/>→ EXPIRING_SOON<br/>→ send notification"]
    D60["60 days before expiry<br/>→ send reminder"]
    D30["30 days before expiry<br/>→ send reminder"]
    D7["7 days before expiry<br/>→ urgent reminder"]
    EXP["Past expiry date<br/>expiry_status = EXPIRED"]

    UPLOAD --> D90 --> D60 --> D30 --> D7 --> EXP

    CRON["DocumentExpiryScheduler<br/>runs daily at 08:00"]
    CRON -.->|checks all documents| D90
```

---

## 9. Infrastructure

```mermaid
graph TB
    subgraph LOCAL["Local Profile"]
        H2["H2 In-Memory DB<br/>auto schema create"]
        LAPP["Spring Boot App<br/>:8080"]
        H2 --- LAPP
    end

    subgraph DEV["Dev Profile (Docker)"]
        MYSQL["MySQL 8<br/>:3306"]
        DAPP["Spring Boot App<br/>:8080"]
        MYSQL --- DAPP
    end

    subgraph FRONTEND["Frontend"]
        REACT["React 19 SPA<br/>:3000"]
    end

    subgraph STORAGE["File Storage"]
        FS["Local Filesystem<br/>./uploads/{tenantId}/{entityType}/{entityId}/"]
    end

    REACT -->|API calls /api/v1| DAPP
    DAPP --> STORAGE
```

---

## 10. Adding a New Module — Step by Step

```mermaid
graph TD
    S1["1. Create modules/module-xyz/pom.xml<br/>depend on all 10 core modules"]
    S2["2. Add to root pom.xml modules list"]
    S3["3. Add module-xyz dep to app/pom.xml"]
    S4["4. Create entities extending BaseEntity<br/>package: com.platform.xyz.*"]
    S5["5. Create repositories<br/>always filter by companyId"]
    S6["6. Create services<br/>use @Auditable, NotificationService,<br/>WorkflowService, LocalStorageService"]
    S7["7. Create controllers<br/>return ApiResponse&lt;T&gt;<br/>throw PlatformException"]
    S8["8. Add Flyway migration VN__xyz_tables.sql"]
    S9["9. Add permissions to V4 seed or new migration"]
    S10["10. Add module-xyz README.md"]

    S1 --> S2 --> S3 --> S4 --> S5 --> S6 --> S7 --> S8 --> S9 --> S10
```
