# Worksuite Platform — Architecture Diagrams

---

## 1. Overall System Architecture

```mermaid
graph TB
    subgraph CLIENTS["🖥️ Clients"]
        WEB["React SPA<br/>(Browser)"]
        MOB["Mobile App<br/>(Future)"]
    end

    subgraph EDGE["🌐 Edge Layer"]
        CDN["AWS CloudFront<br/>(CDN + Static Assets)"]
        GW["API Gateway / Load Balancer<br/>(AWS ALB)"]
    end

    subgraph APP["⚙️ Application Layer — Spring Boot Monolith"]
        direction TB
        subgraph CORE["🔩 Core Modules"]
            SEC["core-security<br/>JWT · RBAC · Account Lock"]
            TEN["core-tenancy<br/>TenantContext · company_id"]
            AUD["core-audit<br/>AuditLog · AuditService"]
            LOG["core-logging<br/>MDC · HttpFilter · AOP"]
            NOT["core-notification<br/>In-app · Email"]
            DOC["core-document<br/>S3StorageService"]
            WFL["core-workflow<br/>Approve · Reject · Escalate"]
            RPT["core-reporting<br/>Excel · CSV · PDF"]
            API["core-api<br/>BaseEntity · ApiResponse · Exceptions"]
            CFG["core-config<br/>Redis · AWS · WebSocket · CORS"]
        end

        subgraph MODS["📦 Product Modules"]
            HR["module-hr<br/>Employee · Branch · Dept<br/>Onboarding · Payroll · Assets"]
            CHAT["module-chat<br/>Channels · DM · Threads<br/>WebSocket · Reactions"]
            VID["module-video<br/>Rooms · Schedule<br/>LiveKit · Recordings"]
            HD["module-helpdesk<br/>Tickets · SLA · Agents<br/>Ratings · Categories"]
            BOT["module-chatbot<br/>Intents · Conversations<br/>LLM · Escalation"]
            TKT["module-ticketing<br/>Projects · Boards · Sprints<br/>Kanban · Time Logs"]
        end
    end

    subgraph INFRA["🗄️ Infrastructure Layer"]
        DB[("PostgreSQL 16<br/>Primary Database")]
        REDIS[("Redis 7<br/>Cache · Pub/Sub · Sessions")]
        S3["AWS S3<br/>Files · Photos · Recordings"]
        SES["AWS SES<br/>Email Delivery"]
        LK["LiveKit Server<br/>WebRTC SFU"]
        LLM["LLM Provider<br/>OpenAI / AWS Bedrock"]
        ES["Elasticsearch<br/>Chat Search · Audit Search"]
    end

    subgraph OBS["📊 Observability"]
        ELK["ELK Stack<br/>Centralized Logs"]
        PROM["Prometheus + Grafana<br/>Metrics · Alerts"]
        ACT["Spring Actuator<br/>Health · Info"]
    end

    WEB --> CDN
    MOB --> GW
    CDN --> GW
    GW --> APP

    APP --> DB
    APP --> REDIS
    APP --> S3
    APP --> SES
    APP --> LK
    APP --> LLM
    APP --> ES

    APP --> ELK
    APP --> PROM
    APP --> ACT
```

---

## 2. Core Module Dependency Map

```mermaid
graph TD
    API["core-api<br/>BaseEntity · ApiResponse<br/>Exceptions · PagedResponse"]

    API --> SEC["core-security<br/>JWT · RBAC · UserPrincipal"]
    API --> TEN["core-tenancy<br/>TenantContext · Interceptor"]
    API --> LOG["core-logging<br/>HttpFilter · AOP · MDC"]
    API --> RPT["core-reporting<br/>Excel · CSV · PDF"]
    API --> CFG["core-config<br/>Redis · AWS · WebSocket"]

    SEC --> AUD["core-audit<br/>AuditLog · AuditService"]
    TEN --> AUD

    SEC --> NOT["core-notification<br/>NotificationService · EmailService"]
    TEN --> NOT

    SEC --> DOC["core-document<br/>S3StorageService · DocumentMetadata"]
    TEN --> DOC

    NOT --> WFL["core-workflow<br/>WorkflowInstance · WorkflowHistory"]
    TEN --> WFL

    AUD --> HR["module-hr"]
    NOT --> HR
    DOC --> HR
    WFL --> HR
    RPT --> HR

    AUD --> HD["module-helpdesk"]
    NOT --> HD
    WFL --> HD

    AUD --> TKT["module-ticketing"]
    NOT --> TKT

    SEC --> CHAT["module-chat"]
    TEN --> CHAT
    NOT --> CHAT
    DOC --> CHAT

    SEC --> VID["module-video"]
    TEN --> VID
    NOT --> VID
    DOC --> VID

    SEC --> BOT["module-chatbot"]
    TEN --> BOT
    NOT --> BOT
    HD --> BOT
```

---

## 3. Request Flow (Per API Call)

```mermaid
sequenceDiagram
    participant C as React Client
    participant GW as ALB / Gateway
    participant JF as JwtAuthFilter
    participant TI as TenantInterceptor
    participant RL as RateLimitInterceptor
    participant AI as AuditInterceptor
    participant SVC as Module Service
    participant DB as PostgreSQL
    participant RD as Redis
    participant AUD as AuditService

    C->>GW: HTTP Request + Bearer Token
    GW->>JF: Forward Request
    JF->>RD: Check token blacklist
    RD-->>JF: Not blacklisted
    JF->>JF: Validate JWT, set SecurityContext
    JF->>TI: Pass to TenantInterceptor
    TI->>TI: Extract company_id → TenantContext
    TI->>RL: Pass to RateLimitInterceptor
    RL->>RD: Check rate limit counter
    RD-->>RL: Within limit
    RL->>AI: Pass to AuditInterceptor
    AI->>AI: Capture IP, URI, method
    AI->>SVC: Call Module Service
    SVC->>DB: Query with company_id filter
    DB-->>SVC: Result
    SVC->>AUD: Log audit entry (async)
    SVC-->>C: ApiResponse { success, data }
```

---

## 4. Real-Time Architecture (Chat + Notifications)

```mermaid
graph LR
    subgraph CLIENTS["Clients"]
        U1["User A<br/>(Browser)"]
        U2["User B<br/>(Browser)"]
        U3["User C<br/>(Browser)"]
    end

    subgraph WS["WebSocket Layer"]
        STOMP["Spring STOMP<br/>WebSocket Server<br/>/ws/chat"]
        BROKER["In-Memory<br/>Message Broker"]
    end

    subgraph REDIS_PS["Redis Pub/Sub"]
        CH1["channel:{channelId}"]
        CH2["user:{userId}:dm"]
        CH3["user:{userId}:notifications"]
    end

    subgraph TOPICS["STOMP Topics"]
        T1["/topic/channel/{id}"]
        T2["/user/queue/dm"]
        T3["/user/queue/notifications"]
    end

    U1 -->|"SEND /app/chat.send"| STOMP
    U2 -->|"SUBSCRIBE /topic/channel/123"| STOMP
    U3 -->|"SUBSCRIBE /user/queue/dm"| STOMP

    STOMP --> BROKER
    BROKER --> REDIS_PS
    REDIS_PS --> TOPICS
    TOPICS --> U1
    TOPICS --> U2
    TOPICS --> U3
```

---

## 5. Video Call Architecture (LiveKit WebRTC)

```mermaid
graph TB
    subgraph PARTICIPANTS["Meeting Participants"]
        H["Host Browser<br/>(Camera + Mic)"]
        P1["Participant 1<br/>(Camera + Mic)"]
        P2["Participant 2<br/>(Camera + Mic)"]
    end

    subgraph BACKEND["Spring Boot Backend"]
        VCTRL["VideoRoomController<br/>POST /video/rooms/{id}/join"]
        VSVC["VideoRoomService<br/>Generate LiveKit Token"]
        LKADMIN["LiveKit Admin SDK<br/>(Room Management)"]
    end

    subgraph LIVEKIT["LiveKit SFU Server"]
        ROOM["Meeting Room<br/>(room_code)"]
        REC["Recording Engine<br/>(Egress)"]
    end

    subgraph STORAGE["Storage"]
        S3["AWS S3<br/>Recording Files"]
    end

    H -->|"1. POST /video/rooms/{id}/join"| VCTRL
    VCTRL --> VSVC
    VSVC --> LKADMIN
    LKADMIN -->|"2. Create Room + Token"| ROOM
    VSVC -->|"3. Return LiveKit Token"| H

    H -->|"4. Connect via LiveKit JS SDK"| ROOM
    P1 -->|"Connect via LiveKit JS SDK"| ROOM
    P2 -->|"Connect via LiveKit JS SDK"| ROOM

    ROOM -->|"WebRTC Streams"| H
    ROOM -->|"WebRTC Streams"| P1
    ROOM -->|"WebRTC Streams"| P2

    ROOM --> REC
    REC --> S3
```

---

## 6. Chatbot Flow (LLM + Escalation)

```mermaid
sequenceDiagram
    participant U as Employee
    participant W as Chat Widget
    participant BOT as Chatbot Service
    participant INT as Intent Matcher
    participant LLM as OpenAI / Bedrock
    participant HD as Helpdesk Module
    participant AGT as Support Agent

    U->>W: "What is my leave balance?"
    W->>BOT: POST /chatbot/conversations/{id}/messages
    BOT->>INT: Match intent from bot_intents table
    INT-->>BOT: Intent: LEAVE_BALANCE_QUERY (confidence 0.92)
    BOT->>LLM: Send message + system prompt + context
    LLM-->>BOT: "Your leave balance is 12 days."
    BOT-->>U: Bot reply displayed

    U->>W: "I have a payroll issue" (3rd unresolved query)
    W->>BOT: POST /chatbot/conversations/{id}/messages
    BOT->>BOT: escalation_threshold reached (3 attempts)
    BOT->>HD: POST /helpdesk/tickets (auto-create ticket)
    HD-->>BOT: Ticket #HD-1042 created
    BOT-->>U: "Escalated to support. Ticket #HD-1042 created."
    HD->>AGT: Notify agent via NotificationService
```

---

## 7. Multi-Tenancy Isolation

```mermaid
graph TB
    subgraph REQ["Incoming Request"]
        JWT["JWT Token<br/>{ userId, companyId, roles }"]
    end

    subgraph FILTER["Filter Chain"]
        JF["JwtAuthFilter<br/>Extract companyId from JWT"]
        TC["TenantContext<br/>ThreadLocal companyId = 'company-uuid-A'"]
        TI["TenantValidationInterceptor<br/>Reject if no companyId"]
    end

    subgraph QUERY["Every DB Query"]
        Q1["SELECT * FROM employees<br/>WHERE company_id = 'company-uuid-A'<br/>AND is_active = true"]
        Q2["SELECT * FROM hd_tickets<br/>WHERE company_id = 'company-uuid-A'"]
        Q3["SELECT * FROM chat_messages<br/>WHERE company_id = 'company-uuid-A'"]
    end

    subgraph TENANTS["Tenant Data (Same DB, Isolated by company_id)"]
        TA["Company A Data"]
        TB["Company B Data"]
        TC2["Company C Data"]
    end

    JWT --> JF --> TC --> TI
    TI --> Q1 & Q2 & Q3
    Q1 --> TA
    Q2 --> TA
    Q3 --> TA
    TB -.->|"Never accessible<br/>to Company A"| Q1
```

---

## 8. Shared Resources Map

```mermaid
graph TD
    subgraph SHARED["🔗 Shared Resources — Used by ALL Modules"]
        DB[("PostgreSQL 16<br/>Single DB · company_id isolation")]
        REDIS[("Redis 7<br/>JWT Blacklist · Rate Limit<br/>Chat Pub/Sub · Cache")]
        S3["AWS S3<br/>All file uploads"]
        SES["AWS SES<br/>All outbound emails"]
        JWT_AUTH["JWT Auth<br/>One token for all modules"]
        RBAC["RBAC<br/>Roles + Permissions"]
        AUDIT["Audit Log<br/>Every write captured"]
        NOTIF["Notification Service<br/>In-app + Email"]
        TENANT["Tenant Context<br/>company_id on every request"]
    end

    subgraph MODULES["Product Modules"]
        HR["module-hr"]
        CHAT["module-chat"]
        VID["module-video"]
        HD["module-helpdesk"]
        BOT["module-chatbot"]
        TKT["module-ticketing"]
    end

    DB --- HR & CHAT & VID & HD & BOT & TKT
    REDIS --- HR & CHAT & VID & HD & BOT & TKT
    S3 --- HR & CHAT & VID & HD & TKT
    SES --- HR & HD & BOT & TKT
    JWT_AUTH --- HR & CHAT & VID & HD & BOT & TKT
    RBAC --- HR & CHAT & VID & HD & BOT & TKT
    AUDIT --- HR & HD & TKT & BOT
    NOTIF --- HR & CHAT & VID & HD & BOT & TKT
    TENANT --- HR & CHAT & VID & HD & BOT & TKT
```

---

## 9. Database Schema Relationships

```mermaid
erDiagram
    companies ||--o{ branches : has
    companies ||--o{ departments : has
    companies ||--o{ employees : employs
    companies ||--o{ users : has
    companies ||--o{ roles : defines

    departments ||--o{ teams : contains
    departments ||--o{ employees : "belongs to"
    branches ||--o{ employees : "works at"
    teams ||--o{ employees : "member of"
    designations ||--o{ employees : "holds"
    employees ||--o| users : "has account"
    roles ||--o{ users : "assigned to"
    roles }o--o{ permissions : "role_permissions"

    employees ||--o{ employee_documents : has
    employees ||--o{ onboarding_checklists : has
    employees ||--o{ offboarding_checklists : has
    employees ||--o{ probations : has
    employees ||--o{ transfers : has
    employees ||--o{ promotions : has
    employees ||--o{ assets : "assigned"
    employees ||--o{ employee_skills : has
    employees ||--o{ employee_certifications : has

    users ||--o{ chat_messages : sends
    users ||--o{ hd_tickets : raises
    users ||--o{ tk_tickets : "assigned to"
    users ||--o{ meeting_rooms : hosts
    users ||--o{ bot_conversations : starts
    users ||--o{ notifications : receives
    users ||--o{ audit_logs : performs

    chat_workspaces ||--o{ chat_channels : contains
    chat_channels ||--o{ chat_messages : has
    chat_messages ||--o{ chat_reactions : has
    chat_messages ||--o{ chat_attachments : has

    meeting_rooms ||--o{ meeting_participants : has
    meeting_rooms ||--o{ meeting_recordings : has
    meeting_schedules ||--o{ meeting_invitees : has

    hd_tickets ||--o{ hd_ticket_replies : has
    hd_tickets ||--o{ hd_ratings : rated
    hd_categories ||--o{ hd_tickets : categorizes
    hd_sla_policies ||--o{ hd_tickets : governs

    tk_projects ||--o{ tk_boards : has
    tk_projects ||--o{ tk_sprints : has
    tk_projects ||--o{ tk_tickets : contains
    tk_boards ||--o{ tk_board_columns : has
    tk_tickets ||--o{ tk_comments : has
    tk_tickets ||--o{ tk_time_logs : has

    bot_configs ||--o{ bot_conversations : drives
    bot_configs ||--o{ bot_intents : defines
    bot_conversations ||--o{ bot_messages : contains
```

---

## 10. Frontend Module Map

```mermaid
graph TD
    subgraph APP["React SPA — Single Entry Point"]
        ROOT["App.jsx<br/>React Router v7"]
    end

    subgraph SHARED_FE["Shared Frontend Resources"]
        AX["axios.js<br/>Interceptors · Auth headers"]
        AUTH_CTX["AuthContext<br/>JWT · user · companyId"]
        LANG_CTX["LanguageContext<br/>EN / AR / HI"]
        CHAT_CTX["ChatContext<br/>WebSocket · Unread counts"]
        LAYOUT["AppLayout<br/>Sidebar · Navbar · ProductSwitcher"]
        UI["UI Components<br/>DataTable · Modal · Badge · Avatar"]
        HOOKS["Shared Hooks<br/>useCrud · useWebSocket<br/>useNotifications · useReferenceData"]
    end

    subgraph PAGES["Product Pages"]
        HR_P["HR Pages<br/>Dashboard · Employees · Dept<br/>Onboarding · Assets · Reports"]
        CHAT_P["Chat Pages<br/>ChannelView · DM · Threads"]
        VID_P["Video Pages<br/>MeetingRoom · Schedule · CallHistory"]
        HD_P["Helpdesk Pages<br/>TicketList · TicketDetail · Agents"]
        BOT_P["Chatbot Pages<br/>BotConfig · Conversations · Widget"]
        TKT_P["Ticketing Pages<br/>Board (Kanban) · Backlog · Sprints"]
    end

    ROOT --> LAYOUT
    LAYOUT --> HR_P & CHAT_P & VID_P & HD_P & BOT_P & TKT_P
    AUTH_CTX --> ROOT
    LANG_CTX --> ROOT
    CHAT_CTX --> CHAT_P & VID_P
    AX --> HR_P & CHAT_P & VID_P & HD_P & BOT_P & TKT_P
    HOOKS --> HR_P & CHAT_P & VID_P & HD_P & BOT_P & TKT_P
    UI --> HR_P & HD_P & TKT_P & BOT_P
```

---

## 11. Infrastructure (Production — AWS)

```mermaid
graph TB
    subgraph INTERNET["Internet"]
        USER["Users / Browsers"]
    end

    subgraph AWS["AWS Cloud"]
        subgraph EDGE2["Edge"]
            R53["Route 53<br/>DNS"]
            CF["CloudFront<br/>CDN · SSL"]
            ACM["ACM<br/>SSL Certificates"]
        end

        subgraph COMPUTE["Compute"]
            ALB["Application Load Balancer"]
            EKS["EKS (Kubernetes)<br/>Spring Boot Pods"]
            FE["S3 + CloudFront<br/>React Static Build"]
        end

        subgraph DATA["Data"]
            RDS[("RDS PostgreSQL 16<br/>Multi-AZ")]
            ELASTICACHE[("ElastiCache Redis 7<br/>Cluster Mode")]
            S3_STORE["S3 Buckets<br/>documents · photos · recordings"]
            ES_AWS["OpenSearch<br/>Chat + Audit Search"]
        end

        subgraph MESSAGING["Messaging & AI"]
            MSK["MSK (Kafka)<br/>Async Events"]
            SES2["SES<br/>Email"]
            LEX["AWS Lex<br/>Chatbot NLP (optional)"]
            BEDROCK["AWS Bedrock<br/>LLM (Claude / Titan)"]
        end

        subgraph MEDIA["Media"]
            LK2["LiveKit Server<br/>(Self-hosted on EC2)<br/>or AWS Chime SDK"]
        end

        subgraph OPS["Operations"]
            CW["CloudWatch<br/>Logs · Metrics · Alarms"]
            SM["Secrets Manager<br/>DB · JWT · API Keys"]
            ECR["ECR<br/>Docker Image Registry"]
        end
    end

    USER --> R53 --> CF --> ALB
    CF --> FE
    ALB --> EKS
    EKS --> RDS & ELASTICACHE & S3_STORE & ES_AWS
    EKS --> MSK & SES2 & BEDROCK & LEX
    EKS --> LK2
    EKS --> CW & SM
    ECR --> EKS
```

---

## 12. Build Phase Roadmap

```mermaid
gantt
    title Worksuite Build Order
    dateFormat  YYYY-MM-DD
    section Phase 1 — Core
    core-api                :p1a, 2025-01-01, 3d
    core-config             :p1b, after p1a, 2d
    core-security           :p1c, after p1b, 4d
    core-tenancy            :p1d, after p1c, 2d
    core-logging            :p1e, after p1d, 2d
    core-audit              :p1f, after p1e, 2d
    core-notification       :p1g, after p1f, 3d
    core-document           :p1h, after p1g, 2d
    core-workflow           :p1i, after p1h, 3d
    core-reporting          :p1j, after p1i, 2d

    section Phase 2 — HR
    Company·Branch·Dept     :p2a, after p1j, 4d
    Employee·User·Role      :p2b, after p2a, 5d
    Documents·Onboarding    :p2c, after p2b, 4d
    Probation·Transfer·Promo:p2d, after p2c, 4d
    Assets·Skills·Dashboard :p2e, after p2d, 3d

    section Phase 3 — Helpdesk
    Categories·SLA·Agents   :p3a, after p2e, 3d
    Tickets·Replies·Ratings :p3b, after p3a, 4d

    section Phase 4 — Ticketing
    Projects·Boards·Sprints :p4a, after p3b, 4d
    Tickets·Comments·TimeLogs:p4b, after p4a, 4d

    section Phase 5 — Chat
    Workspaces·Channels     :p5a, after p4b, 3d
    Messages·DM·WebSocket   :p5b, after p5a, 5d

    section Phase 6 — Chatbot
    BotConfig·Intents       :p6a, after p5b, 3d
    LLM·Escalation          :p6b, after p6a, 3d

    section Phase 7 — Video
    Rooms·LiveKit           :p7a, after p6b, 4d
    Schedule·Recordings     :p7b, after p7a, 3d

    section Phase 8 — Frontend
    Auth·Layout·HR Pages    :p8a, after p7b, 7d
    Helpdesk·Ticketing Pages:p8b, after p8a, 5d
    Chat·Video Pages        :p8c, after p8b, 6d
    Chatbot Widget          :p8d, after p8c, 3d
```

---

## Summary — Categorized Architecture

| Category | Components |
|---|---|
| **Client Layer** | React SPA, Mobile (future) |
| **Edge Layer** | CloudFront CDN, Route 53, ACM SSL, ALB |
| **Application Layer** | Spring Boot — 10 Core Modules + 6 Product Modules |
| **Real-Time Layer** | Spring WebSocket (STOMP) + Redis Pub/Sub + LiveKit WebRTC |
| **AI Layer** | OpenAI / AWS Bedrock (LLM) + AWS Lex (NLP) |
| **Data Layer** | PostgreSQL (primary), Redis (cache/sessions), Elasticsearch (search) |
| **Storage Layer** | AWS S3 (files, photos, recordings) |
| **Messaging Layer** | AWS MSK/Kafka (async events), AWS SES (email) |
| **Security Layer** | JWT, RBAC, TenantContext, Rate Limiting, Account Locking |
| **Observability Layer** | CloudWatch, ELK Stack, Prometheus + Grafana, Spring Actuator |
| **DevOps Layer** | Docker, Kubernetes (EKS), ECR, GitHub Actions CI/CD |
| **Secrets Layer** | AWS Secrets Manager |
