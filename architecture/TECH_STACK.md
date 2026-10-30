# Company Platform - Complete Technology Stack & Architecture

## Overview

This document describes the complete technology stack chosen for the Enterprise Company Platform.
The platform covers HRMS, Payroll, Chat, Audio/Video Calls, Ticket Management, Internal Support,
Chatbot, Finance, and more — supporting both Web and Mobile applications.

---

## Architecture Decision → Modular Monolith

### What We Chose
```
NOT Microservices  ❌  → Too complex to start with
NOT a messy single app ❌  → Hard to maintain and scale

Modular Monolith   ✅  → Best of both worlds
```

### What is a Modular Monolith
A Modular Monolith is ONE Spring Boot application that is internally
organized into clean, independent modules. Each module owns its own
controllers, services, repositories, and models — but they all run
in the same application and share the same database connection.

### Why Modular Monolith over Microservices

| Reason | Explanation |
|--------|-------------|
| Faster to build | No API gateway, no service discovery, no inter-service networking |
| Simpler deployment | One Docker container for the entire backend |
| Easier debugging | One application log, one place to look |
| Lower cost | One server instead of 10+ servers |
| Same database | No distributed transaction complexity |
| Team size friendly | Small team can manage one codebase easily |
| Still scalable | Clean module boundaries make future split easy |
| Less DevOps overhead | No Kubernetes needed to start |

### Migration Path to Microservices (When Ready)

```
Phase 1 → Modular Monolith (NOW)
          Build fast, ship features, get users

Phase 2 → Modular Monolith with clean boundaries (6-12 months)
          Enforce module isolation, prepare APIs between modules

Phase 3 → Extract microservices one by one (when scale demands)
          Extract Chat service first (highest load)
          Extract Payroll service (isolated business logic)
          Extract each module gradually — no big bang rewrite
```

---

## Final Technology Stack

| Layer | Technology | License | Cost |
|-------|-----------|---------|------|
| Architecture | Modular Monolith | - | FREE |
| Backend Framework | Java + Spring Boot | Apache 2.0 | FREE |
| Authentication | Keycloak + JWT | Apache 2.0 | FREE |
| Cache & Real-time | Valkey | BSD | FREE |
| Message Broker | RabbitMQ | Mozilla Public | FREE |
| Primary Database | PostgreSQL | PostgreSQL License | FREE |
| Secondary Database | MongoDB | SSPL (free tier) | FREE |
| Video / Audio | Jitsi Meet | Apache 2.0 | FREE |
| Web Frontend | React.js + TypeScript | MIT | FREE |
| Mobile App | React Native + TypeScript | MIT | FREE |
| Web Styling | Tailwind CSS | MIT | FREE |
| Mobile Styling | NativeWind | MIT | FREE |
| State Management | Redux Toolkit | MIT | FREE |
| File Storage | MinIO | AGPL-3.0 | FREE |
| Push Notifications | Firebase FCM | Google ToS | FREE tier |
| Search Engine | OpenSearch | Apache 2.0 | FREE |
| PDF / Reports | JasperReports + Apache POI | LGPL / Apache 2.0 | FREE |
| DevOps | Docker + Docker Compose | Apache 2.0 | FREE |

---

## Complete Project Folder Structure

```
company-platform/
│
├── backend/                                  ← ONE Spring Boot Application
│   └── src/main/java/com/company/
│       │
│       ├── CompanyApplication.java           ← Main entry point
│       │
│       ├── config/                           ← Global configurations
│       │   ├── SecurityConfig.java           ← Keycloak + JWT setup
│       │   ├── ValkeyConfig.java             ← Cache configuration
│       │   ├── RabbitMQConfig.java           ← Queue configuration
│       │   ├── MongoConfig.java              ← MongoDB configuration
│       │   └── SwaggerConfig.java            ← API documentation
│       │
│       ├── common/                           ← Shared across all modules
│       │   ├── exception/                    ← Global exception handling
│       │   ├── response/                     ← Standard API response
│       │   ├── utils/                        ← Helper utilities
│       │   └── constants/                    ← App-wide constants
│       │
│       ├── modules/
│       │   │
│       │   ├── hrms/                         ← HRMS Module
│       │   │   ├── controller/
│       │   │   │   ├── EmployeeController.java
│       │   │   │   ├── DepartmentController.java
│       │   │   │   ├── LeaveController.java
│       │   │   │   └── AttendanceController.java
│       │   │   ├── service/
│       │   │   │   ├── EmployeeService.java
│       │   │   │   ├── DepartmentService.java
│       │   │   │   ├── LeaveService.java
│       │   │   │   └── AttendanceService.java
│       │   │   ├── repository/
│       │   │   │   ├── EmployeeRepository.java
│       │   │   │   ├── DepartmentRepository.java
│       │   │   │   ├── LeaveRepository.java
│       │   │   │   └── AttendanceRepository.java
│       │   │   └── model/
│       │   │       ├── Employee.java
│       │   │       ├── Department.java
│       │   │       ├── LeaveRequest.java
│       │   │       └── Attendance.java
│       │   │
│       │   ├── payroll/                      ← Payroll Module
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   └── model/
│       │   │
│       │   ├── ticket/                       ← Ticket Module (Jira-like)
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   └── model/
│       │   │
│       │   ├── chat/                         ← Chat Module
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/               ← MongoDB repositories
│       │   │   └── model/
│       │   │
│       │   ├── support/                      ← Internal Support Module
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   └── model/
│       │   │
│       │   ├── finance/                      ← Finance Module
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   └── model/
│       │   │
│       │   ├── recruitment/                  ← Recruitment Module
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   └── model/
│       │   │
│       │   ├── notification/                 ← Notification Module
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   └── model/
│       │   │
│       │   ├── chatbot/                      ← AI Chatbot Module
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   └── model/
│       │   │
│       │   └── video/                        ← Jitsi Video Module
│       │       ├── controller/
│       │       ├── service/
│       │       └── model/
│       │
│       └── resources/
│           ├── application.yml               ← Main config
│           ├── application-dev.yml           ← Dev environment
│           └── application-prod.yml          ← Production environment
│
├── frontend/
│   ├── web/                                  ← React.js Web App
│   │   └── src/
│   │       ├── pages/
│   │       │   ├── dashboard/
│   │       │   ├── hrms/
│   │       │   ├── payroll/
│   │       │   ├── tickets/
│   │       │   ├── chat/
│   │       │   ├── video/
│   │       │   ├── support/
│   │       │   ├── finance/
│   │       │   └── reports/
│   │       ├── components/
│   │       ├── layouts/
│   │       ├── hooks/
│   │       └── store/
│   │
│   ├── mobile/                               ← React Native App
│   │   └── src/
│   │       ├── screens/
│   │       │   ├── dashboard/
│   │       │   ├── hrms/
│   │       │   ├── payroll/
│   │       │   ├── tickets/
│   │       │   ├── chat/
│   │       │   └── video/
│   │       ├── components/
│   │       ├── navigation/
│   │       └── store/
│   │
│   └── shared/                               ← Shared Web + Mobile Code
│       ├── api/                              ← All API calls (80% shared)
│       ├── store/                            ← Redux state management
│       ├── types/                            ← TypeScript interfaces
│       ├── utils/                            ← Helper functions
│       └── constants/                        ← App constants
│
├── infrastructure/
│   ├── docker-compose.yml                    ← All services one command ✅
│   ├── docker-compose.prod.yml               ← Production overrides
│   └── nginx/
│       └── nginx.conf                        ← Reverse proxy config
│
└── architecture/
    └── TECH_STACK.md                         ← This document ✅
```

---

## Complete Architecture Diagram (Modular Monolith)

```
┌──────────────────────────────────────────────────────────────┐
│               Users (Browser / iOS / Android)                 │
└───────────────────────────┬──────────────────────────────────┘
                            │
               ┌────────────┴────────────┐
               │                          │
  ┌────────────▼───────────┐  ┌───────────▼────────────┐
  │    React.js (Web)      │  │  React Native (Mobile)  │
  │    + TypeScript        │  │  + TypeScript           │
  │    + Tailwind CSS      │  │  + NativeWind           │
  └────────────┬───────────┘  └───────────┬────────────┘
               │                          │
               └────────────┬─────────────┘
                            │
               ┌────────────▼────────────┐
               │     Shared Code Layer    │
               │  (API, Redux, Types)     │
               └────────────┬────────────┘
                            │ REST API calls
               ┌────────────▼────────────┐
               │        Nginx            │
               │    (Reverse Proxy)      │
               │      Port: 80/443       │
               └────────────┬────────────┘
                            │
               ┌────────────▼────────────┐
               │   Keycloak Auth Server  │
               │   JWT Token Validation  │
               │      Port: 8080         │
               └────────────┬────────────┘
                            │ Validated Request
               ┌────────────▼────────────────────────────────┐
               │         Spring Boot Application              │
               │              Port: 8081                      │
               │                                              │
               │  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
               │  │  HRMS    │  │ Payroll  │  │ Ticket   │  │
               │  │  Module  │  │  Module  │  │  Module  │  │
               │  └──────────┘  └──────────┘  └──────────┘  │
               │  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
               │  │  Chat    │  │ Support  │  │ Finance  │  │
               │  │  Module  │  │  Module  │  │  Module  │  │
               │  └──────────┘  └──────────┘  └──────────┘  │
               │  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
               │  │Notification│ │ Chatbot  │  │  Video   │  │
               │  │  Module  │  │  Module  │  │  Module  │  │
               │  └──────────┘  └──────────┘  └──────────┘  │
               └──────────────────────┬──────────────────────┘
                                      │
        ┌─────────────────────────────┼──────────────────────┐
        │                             │                       │
┌───────▼────────┐       ┌────────────▼────┐      ┌──────────▼──────┐
│  PostgreSQL    │       │    MongoDB       │      │    Valkey        │
│  Primary DB    │       │  Chat + Logs     │      │  Cache + PubSub  │
│  Port: 5432    │       │  Port: 27017     │      │  Port: 6379      │
└────────────────┘       └─────────────────┘      └─────────────────┘
┌───────▼────────┐       ┌─────────────────┐      ┌─────────────────┐
│   RabbitMQ     │       │   OpenSearch     │      │     MinIO        │
│  Task Queues   │       │    Search        │      │  File Storage    │
│  Port: 5672    │       │  Port: 9200      │      │  Port: 9000      │
└────────────────┘       └─────────────────┘      └─────────────────┘
┌───────▼────────┐
│   Jitsi Meet   │
│  Video/Audio   │
│  Port: 8443    │
└────────────────┘
```

---

## Service Port Map

| Service | Port | Purpose |
|---------|------|---------|
| Spring Boot App | 8081 | Main backend application (all modules) |
| Keycloak | 8080 | Authentication and SSO |
| PostgreSQL | 5432 | Primary relational database |
| MongoDB | 27017 | Chat and logs database |
| Valkey | 6379 | Cache and real-time pub/sub |
| RabbitMQ | 5672 | Message queues |
| RabbitMQ UI | 15672 | Queue monitoring dashboard |
| OpenSearch | 9200 | Search engine |
| OpenSearch Dashboards | 5601 | Search monitoring UI |
| MinIO | 9000 | File object storage |
| MinIO UI | 9001 | File storage dashboard |
| Jitsi Meet | 8443 | Video/audio calls |
| Nginx | 80/443 | Reverse proxy |

---

## 1. Backend Framework → Java + Spring Boot (Modular Monolith)

### What it is
Spring Boot is an open-source Java framework used to build production-grade
REST APIs with minimal configuration. We use it as a single application
with internally separated modules.

### Why we chose it
- Industry standard for enterprise Java applications
- Built-in support for REST APIs, security, database, queues, and caching
- Massive community, well documented, easy to hire developers
- Native integration with Keycloak, RabbitMQ, Valkey, PostgreSQL, MongoDB
- Spring Security handles JWT validation with minimal code
- Spring Data JPA removes boilerplate database code
- Spring Scheduler handles background jobs like payroll processing
- Single deployable JAR — simple to run and manage
- Battle tested by companies like Netflix, Amazon, LinkedIn

### Why Modular Monolith over Microservices
- No API gateway needed — saves complexity and cost
- No inter-service network calls — direct method calls between modules
- One database connection pool — simpler and more efficient
- One deployment — one Docker container for entire backend
- Easier to debug — one log file, one application
- Faster development — no distributed system overhead
- Can split into microservices later when scale demands it

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| Microservices now | Too complex for current team size and stage |
| Django (Python) | Team uses Java |
| Node.js | Less suitable for complex business logic |

---

## 2. Authentication → Keycloak + JWT

### What it is
- Keycloak is an open-source Identity and Access Management solution
- JWT (JSON Web Token) is an open standard for stateless API authentication

### Why we chose it
- Keycloak provides SSO across all platform modules in one login
- Built-in user management, role management, password reset, 2FA
- Supports Google/Microsoft OAuth2 login out of the box
- JWT tokens are stateless — no database lookup needed per request
- Spring Boot has native OAuth2 resource server support for Keycloak
- Self-hosted — user data stays on company servers
- Apache 2.0 license — free forever, no per-user cost

### How they work together
```
User Login → Keycloak validates → Issues JWT Token
API Call   → Sends JWT Token   → Spring Boot validates → Access granted
```

### Roles defined in Keycloak
```
SUPER_ADMIN      → Full access to everything
HR_ADMIN         → HRMS + Payroll full access
HR_MANAGER       → HRMS read/write
EMPLOYEE         → Own profile, leave, tickets
FINANCE_ADMIN    → Finance + Payroll
PROJECT_MANAGER  → Projects + Tickets
SUPPORT_AGENT    → Support tickets only
IT_ADMIN         → IT support tickets
MANAGER          → Team management
```

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| Auth0 | Paid after free tier, vendor lock-in |
| Firebase Auth | Vendor lock-in, limited enterprise features |
| Custom JWT only | No SSO, no user management UI, more code to maintain |

---

## 3. Cache & Real-time → Valkey

### What it is
Valkey is a free, open-source, in-memory data store forked from Redis 7.2.
Backed by AWS, Google, Oracle, and the Linux Foundation.

### Why we chose Valkey over Redis
- Redis changed license to SSPL in 2024 — not fully open source, paid for enterprise
- Valkey is 100% free BSD license forever
- Exact same API and commands as Redis — zero code difference
- Backed by major cloud providers — not going away
- AWS ElastiCache officially supports Valkey

### What Valkey handles
| Use Case | Module |
|----------|--------|
| Employee data caching | HRMS |
| Payslip caching | Payroll |
| Chat pub/sub broadcasting | Chat |
| User session storage | All modules |
| Real-time notifications | Notifications |
| Login rate limiting | Auth |
| API response caching | All modules |

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| Redis 7.4+ | SSPL license, paid for enterprise use |
| EhCache | No pub/sub, in-JVM only, not suitable for our needs |
| Caffeine | In-JVM only, no real-time features |

---

## 4. Message Broker → RabbitMQ

### What it is
RabbitMQ is an open-source message broker for asynchronous background
task processing using queues and exchanges.

### Why we chose it
- Simple to set up compared to Kafka
- Perfect for task queues — email, payroll, notifications
- Spring Boot has native AMQP support
- Management UI dashboard for monitoring
- Reliable delivery with acknowledgements and dead letter queues
- Free and open source (Mozilla Public License)

### Queues in our platform
| Queue | Purpose |
|-------|---------|
| email.queue | Send emails in background |
| payroll.queue | Process monthly payroll calculations |
| notification.queue | Push and in-app notifications |
| ticket.queue | Ticket assignment notifications |
| support.queue | Support ticket escalations |
| audit.queue | Write audit logs asynchronously |

### Why not Kafka
- Kafka needs ZooKeeper/KRaft + 3 brokers minimum for production
- Overkill for current scale
- RabbitMQ handles all our needs with much simpler setup
- Kafka can be added alongside RabbitMQ later when needed

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| Apache Kafka | Too complex for current scale |
| AWS SQS | Paid service, vendor lock-in |
| ActiveMQ | Older, less community activity |

---

## 5. Primary Database → PostgreSQL

### What it is
PostgreSQL is a powerful open-source relational database with over
35 years of active development.

### Why we chose it
- Best relational database for enterprise Java applications
- Full ACID compliance — critical for payroll and finance
- Native JSONB support for flexible data
- Better performance than MySQL for complex queries
- Spring Data JPA integrates perfectly
- Free and open source forever
- Used by Instagram, Spotify, GitHub, Apple

### What PostgreSQL stores
- Employees, Departments, Attendance, Leave
- Payroll, Salary, Tax, Deductions, Payslips
- Tickets, Projects, Comments, Assignments
- Support Tickets, SLA, Escalations
- Finance, Invoices, Expenses, Budget
- Users, Roles, Permissions

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| MySQL | Weaker JSON support, less powerful for complex queries |
| Oracle DB | Expensive licensing |
| MS SQL Server | Expensive licensing |

---

## 6. Secondary Database → MongoDB

### What it is
MongoDB is a document-oriented NoSQL database storing flexible
JSON-like documents.

### Why we chose it
- Chat messages have variable structure — documents fit naturally
- No schema migrations needed for chat and log data
- Faster reads/writes for high-volume chat data
- Spring Data MongoDB integrates seamlessly
- Append-only audit logs are ideal for document storage

### What MongoDB stores
| Collection | Data |
|------------|------|
| chat_messages | All messages with metadata |
| chat_rooms | Room info, members, last message |
| audit_logs | Who did what and when |
| notifications | Notification history per user |
| chatbot_conversations | AI chatbot history |
| activity_feeds | Timeline events per user |

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| PostgreSQL for chat | Slower for high-volume unstructured data |
| Cassandra | Too complex for our scale |
| Firebase Firestore | Vendor lock-in, paid at scale |

---

## 7. Video / Audio → Jitsi Meet

### What it is
Jitsi Meet is a free, open-source video conferencing solution built
on WebRTC. Fully self-hosted on your own server.

### Why we chose it
- 100% free forever — no per-minute cost
- Self-hosted — all call data stays on company servers
- Official React SDK and React Native SDK available
- HD video, screen sharing, recording, chat, whiteboard all included
- Unlimited participants and call duration
- Apache 2.0 license

### Cost comparison
```
Jitsi Meet (self-hosted)  → $0/month forever
Agora.io (100 users)      → ~$12-50/month
Daily.co (100 users)      → ~$6-20/month
Zoom API                  → Paid per host
```

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| Agora.io | Pay per minute, cost grows with company |
| Daily.co | Pay per minute, vendor lock-in |
| Twilio Video | Expensive per minute |
| Zoom SDK | Expensive licensing |

---

## 8. Web Frontend → React.js + TypeScript

### Why we chose it
- Most popular frontend framework worldwide
- Official Jitsi React SDK available
- Huge ecosystem of UI libraries (Ant Design, Material UI)
- TypeScript catches bugs at compile time
- 80% code shared with React Native mobile app
- Works perfectly with Spring Boot REST APIs

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| Angular | More verbose, steeper learning curve |
| Vue.js | Smaller community, less enterprise adoption |

---

## 9. Mobile App → React Native + TypeScript

### Why we chose it
- Same TypeScript language as web — one team handles both
- 80% code shared between web and mobile
- Official Jitsi React Native SDK available
- Produces truly native iOS and Android apps
- NativeWind brings Tailwind CSS to mobile

### Code sharing strategy
```
Shared (80%)                    Platform Specific (20%)
────────────────                ──────────────────────
API service calls               Mobile navigation
Redux state management          Web routing
TypeScript interfaces           Push notification setup
Business logic                  Camera/microphone access
Validation logic                Platform layouts
Constants and configs           Native device features
```

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| Flutter | Requires Dart, no official Jitsi SDK |
| Ionic | Web wrapper, not truly native |
| Native Java/Swift | Two separate codebases, double cost |

---

## 10. File Storage → MinIO

### Why we chose it
- 100% free, self-hosted
- AWS S3 compatible API — zero code change if migrating to S3 later
- Stores employee documents, payslips, chat attachments, profile pictures
- Full control — data stays on company servers

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| AWS S3 | Paid per GB and per request |
| Google Cloud Storage | Paid, vendor lock-in |
| Local filesystem | Not scalable, no redundancy |

---

## 11. Push Notifications → Firebase FCM

### Why we chose it
- Free for unlimited notifications
- Official React Native SDK available
- Spring Boot Java SDK available
- Industry standard for mobile push notifications

---

## 12. Search → OpenSearch

### What it is
OpenSearch is a free, open-source search and analytics engine forked
from Elasticsearch 7.10. Fully backed by AWS under Apache 2.0 license.

### Why we chose OpenSearch over Elasticsearch
- Elasticsearch changed license to Elastic License (not fully open source)
- OpenSearch is 100% free Apache 2.0 license forever
- AWS backed — not going away
- Same API as Elasticsearch — Spring Data Elasticsearch client works with it
- OpenSearch Dashboards included free (like Kibana)
- Self-hosted via Docker
- Active development and growing community

### What OpenSearch handles
| Use Case | Module |
|----------|--------|
| Employee search by name, skill, department | HRMS |
| Ticket search by title, description, status | Tickets |
| Support ticket search | Support |
| Chat message search | Chat |
| Document search | File Management |

### Alternatives considered
| Alternative | Reason Rejected |
|-------------|----------------|
| Elasticsearch | Changed to Elastic License, not fully free |
| Meilisearch | Less mature for enterprise use |
| PostgreSQL full-text | Limited for complex cross-module search |

---

## 13. Reports & PDF → JasperReports + Apache POI

### Why we chose it
- JasperReports: industry standard Java PDF generation (payslips, HR reports)
- Apache POI: Excel export for HR and finance reports
- Both free and open source
- Native Spring Boot integration

---

## 14. DevOps → Docker + Docker Compose

### Why we chose it
- All services start with one command: `docker-compose up -d`
- Same environment across dev, staging, production
- No "works on my machine" problems
- Free and open source
- Easy path to Kubernetes when scaling is needed

---

## Build Phase Order (Monolith)

```
Phase 1  → Docker setup + all services running        (Week 1)
Phase 2  → Keycloak auth + Spring Boot base project   (Week 2)
Phase 3  → HRMS module (Employee, Leave, Attendance)  (Week 3-4)
Phase 4  → Payroll module                             (Week 5-6)
Phase 5  → Ticket module                              (Week 7-8)
Phase 6  → Chat module (WebSocket + Valkey)           (Week 9-10)
Phase 7  → Video module (Jitsi integration)           (Week 11)
Phase 8  → Support module                             (Week 12)
Phase 9  → Notification module (FCM + RabbitMQ)       (Week 13)
Phase 10 → Chatbot module (OpenAI)                    (Week 14-15)
Phase 11 → Finance module                             (Week 16-17)
Phase 12 → Reports + Analytics dashboard              (Week 18-19)
Phase 13 → React Native mobile app                    (Week 20-22)
Phase 14 → Testing + Security audit                   (Week 23-24)
```

---

## Monthly Infrastructure Cost Estimate

```
All software licenses          → $0 (all open source / free)
─────────────────────────────────────────────────────────────
Application Server (1 server)  → $30  - $60  / month
Database Server (AWS RDS)      → $30  - $50  / month
MongoDB Atlas (free tier)      → $0   - $30  / month
Valkey Server                  → $15  - $20  / month
Jitsi Server (4 CPU / 8GB)     → $20  - $30  / month
MinIO Storage Server           → $10  - $20  / month
OpenAI API (chatbot)           → $20  - $50  / month
Firebase FCM (push notify)     → $0   (free tier)
Apple Developer Account        → $99  / year ($8/month)
Google Play Account            → $25  one time
─────────────────────────────────────────────────────────────
Total Estimate                 → $125 - $270 / month

Note: Monolith needs FEWER servers than microservices
      Saves $100-200/month compared to microservices approach
```

---

## Why This Stack is Future Proof

```
Scale to 500+ employees    → Scale up single server, no code change
Scale to 5000+ employees   → Split modules into microservices gradually
Add AI features            → OpenAI API in chatbot module
Add analytics              → Add Grafana + Prometheus (free, Docker)
Add biometric attendance   → Hardware API in HRMS module
Add SMS notifications      → Twilio API in notification module
Add Kafka later            → Runs alongside RabbitMQ, no removal needed
Move storage to AWS S3     → MinIO uses same S3 API, zero code change
Move to Kubernetes         → Same Docker containers, just orchestrated
```

---

*Document created: 2024*
*Stack version: 3.0*
*Architecture: Modular Monolith*
*Status: Finalized and approved*
