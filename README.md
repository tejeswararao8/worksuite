# HRMS Platform — Complete Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Modules](#modules)
6. [Database Schema](#database-schema)
7. [API Reference](#api-reference)
8. [Authentication & Security](#authentication--security)
9. [Frontend](#frontend)
10. [Infrastructure & DevOps](#infrastructure--devops)
11. [Configuration](#configuration)
12. [Getting Started](#getting-started)

---

## Overview

HRMS Platform is a full-stack, multi-tenant Human Resource Management System built with:
- **Backend**: Spring Boot 3.2.5 (Java 21)
- **Frontend**: React 19 + TailwindCSS
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Storage**: AWS S3

It supports complete employee lifecycle management — from onboarding to offboarding — with role-based access control, audit trails, document management, and multi-language support.

---

## Architecture

```
hrms/
├── hrms-platform/       # Spring Boot backend (REST API)
└── hrms-frontend/       # React frontend (SPA)
```

### Backend Architecture
- **Layered architecture** per module: `Controller → Service → Repository → Entity`
- **Multi-tenancy** via `company_id` on every entity
- **JWT-based stateless auth** with Redis token blacklist
- **Flyway** for database migrations
- **MapStruct** for DTO ↔ Entity mapping
- **AOP** for logging and audit trail
- **Interceptors** for rate limiting, tenant validation, request logging

### Frontend Architecture
- **React Query (TanStack)** for server state management
- **React Hook Form** for form handling
- **React Router v7** for routing
- **i18next** for internationalization (EN / AR / HI)
- **Recharts** for dashboard charts
- **Axios** with interceptors for API calls

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 3.2.5 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Security | Spring Security + JWT (JJWT 0.12.5) |
| Mapping | MapStruct 1.5.5 |
| Boilerplate | Lombok 1.18.32 |
| API Docs | SpringDoc OpenAPI 2.5.0 |
| File Storage | AWS S3 SDK 2.25.60 |
| Email | Spring Mail (SMTP) |
| Export | Apache POI (Excel), OpenCSV, iText7 (PDF) |
| Frontend | React 19, TailwindCSS 3 |
| State | TanStack React Query v5 |
| Forms | React Hook Form v7 |
| Charts | Recharts v3 |
| i18n | i18next v26 |
| Containerization | Docker + Docker Compose |

---

## Project Structure

### Backend (`hrms-platform/`)

```
src/main/java/com/hrms/
├── HrmsPlatformApplication.java
├── asset/
├── audit/
├── auth/
├── branch/
├── common/
│   ├── dto/          # ApiResponse, PagedResponse
│   ├── entity/       # BaseEntity
│   ├── exception/    # GlobalExceptionHandler, custom exceptions
│   ├── interceptor/  # Audit, RateLimit, Logging, TenantValidation
│   ├── logging/      # HttpLoggingFilter, LoggingAspect
│   └── util/         # SecurityUtils, TenantContext
├── company/
├── config/           # Security, Redis, AWS, OpenAPI, WebMvc
├── dashboard/
├── department/
├── designation/
├── document/
├── employee/
├── infrastructure/
│   ├── email/        # EmailService
│   └── storage/      # S3StorageService
├── notification/
├── offboarding/
├── onboarding/
├── probation/
├── promotion/
├── report/
├── role/
├── security/
│   ├── jwt/          # JwtAuthenticationFilter, JwtTokenProvider, TokenBlacklist
│   └── service/      # UserDetailsServiceImpl, UserPrincipal
├── skills/
├── team/
├── transfer/
└── user/
```

### Frontend (`hrms-frontend/src/`)

```
├── api/              # axios.js, services.js
├── components/
│   ├── layout/       # AppLayout, Navbar, Sidebar
│   └── ui/           # CrudPage, LanguageSwitcher
├── constants/        # enums, queryKeys, routes
├── context/          # AuthContext, LanguageContext
├── hooks/            # useCrud, useDashboard, useEmployees, useNotifications, useReferenceData
├── i18n/             # locales (en, ar, hi)
├── pages/            # One page per module
├── services/         # storageService
└── utils/            # dateUtils, download, errorHandler
```

---

## Modules

### 1. Company
Manages the top-level tenant entity.
- CRUD for company profile (name, legal name, registration, tax, logo, industry)
- Each company is an isolated tenant

### 2. Branch
Manages physical office locations under a company.
- Fields: name, code, address, city, country, phone, email, `is_head_office`
- Unique constraint: `(code, company_id)`

### 3. Department
Hierarchical department structure.
- Supports parent department (self-referencing)
- Tracks department head employee
- Unique constraint: `(code, company_id)`

### 4. Team
Teams within departments.
- Linked to a department
- Has a lead employee

### 5. Designation
Job titles / grades.
- Fields: title, description, level, grade

### 6. Employee
Core module — full employee profile.
- Personal: name, email, mobile, gender, DOB, nationality, marital status, blood group, photo
- Address: current and permanent
- Employment: joining date, branch, department, team, designation, manager, work location
- Status: `PROBATION`, `ACTIVE`, `INACTIVE`, `TERMINATED`
- Type: employment type (full-time, part-time, contract, etc.)
- Unique: `(email, company_id)`, `(employee_code, company_id)`
- Supports advanced filtering via `EmployeeSpecification`

### 7. User
System login accounts linked to employees.
- JWT-based authentication
- Role assignment
- Account locking after failed login attempts
- Password change enforcement

### 8. Role & Permission
RBAC system.
- Roles scoped per company
- Permissions define module-level access
- Many-to-many: `role_permissions`

### 9. Document Management
Employee document storage with expiry tracking.
- Types: passport, visa, ID, certificates, etc.
- Fields: document number, issuing authority, issue/expiry dates
- File stored in AWS S3 (`file_key`)
- Expiry status: `VALID`, `EXPIRING_SOON`, `EXPIRED`
- Scheduled job (`DocumentExpiryScheduler`) sends reminders at 90/60/30/7 days before expiry

### 10. Onboarding
Task checklist for new joiners.
- Tasks assigned to responsible users
- Status: `PENDING`, `IN_PROGRESS`, `COMPLETED`
- Due dates tracked

### 11. Offboarding
Exit checklist for departing employees.
- Same structure as onboarding
- Tracks asset returns, access revocation, etc.

### 12. Probation
Tracks probation period for new employees.
- Start/end dates, optional extension
- Status: `IN_PROGRESS`, `PASSED`, `EXTENDED`, `FAILED`
- Approval workflow

### 13. Transfer
Employee movement between departments, branches, teams, or managers.
- Transfer types: department, branch, manager, team
- Tracks from/to for each dimension
- Status: `PENDING`, `APPROVED`, `REJECTED`

### 14. Promotion
Designation change records.
- From/to designation
- Effective date, reason
- Approval workflow

### 15. Asset Management
Company asset tracking and assignment.
- Asset types: laptop, phone, vehicle, etc.
- Status: `AVAILABLE`, `ASSIGNED`, `UNDER_MAINTENANCE`, `RETIRED`
- Assignment/return timestamps

### 16. Skills & Certifications
Employee competency tracking.
- Skills: name, type, proficiency level, years of experience
- Certifications: name, issuing org, issue/expiry dates, credential URL

### 17. Notifications
In-app notification system.
- Types: document expiry, onboarding tasks, approvals, etc.
- Read/unread tracking
- Linked to reference entity (reference_id, reference_type)

### 18. Audit Log
Immutable change history.
- Captures entity name, entity ID, action (CREATE/UPDATE/DELETE)
- Stores old and new values (JSON)
- Records performer and IP address
- Populated automatically via `AuditTrailInterceptor` and `LoggingAspect`

### 19. Dashboard
Aggregated statistics for the home screen.
- Total employees, departments, active assets
- Pending onboarding/offboarding tasks
- Recent activities

### 20. Reports
Export employee and HR data.
- Formats: Excel (Apache POI), CSV (OpenCSV), PDF (iText7)

---

## Database Schema

### Key Tables

| Table | Description |
|---|---|
| `companies` | Tenant root |
| `branches` | Office locations |
| `departments` | Org hierarchy |
| `teams` | Sub-groups within departments |
| `designations` | Job titles |
| `employees` | Core employee records |
| `users` | Login accounts |
| `roles` | RBAC roles |
| `permissions` | Module permissions |
| `role_permissions` | Role ↔ Permission join |
| `employee_documents` | Documents with expiry |
| `onboarding_checklists` | New joiner tasks |
| `offboarding_checklists` | Exit tasks |
| `probations` | Probation periods |
| `transfers` | Movement records |
| `promotions` | Designation changes |
| `assets` | Company assets |
| `employee_skills` | Skills |
| `employee_certifications` | Certifications |
| `notifications` | In-app alerts |
| `audit_logs` | Change history |

### Common Columns (all tables)
```sql
company_id   UUID        -- tenant isolation
created_by   VARCHAR
created_at   TIMESTAMP
updated_by   VARCHAR
updated_at   TIMESTAMP
version      BIGINT      -- optimistic locking
is_active    BOOLEAN     -- soft delete
```

### Migrations
| File | Description |
|---|---|
| `V1__initial_schema.sql` | All core tables and indexes |
| `V2__seed_data.sql` | Default roles, permissions, admin user |
| `V3__onboarding_offboarding_tables.sql` | Onboarding/offboarding checklists |

---

## API Reference

Base URL: `http://localhost:8080/api/v1`

Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`

OpenAPI JSON: `http://localhost:8080/api/v1/api-docs`

### Endpoints Summary

| Module | Base Path |
|---|---|
| Auth | `/auth` |
| Company | `/companies` |
| Branch | `/branches` |
| Department | `/departments` |
| Team | `/teams` |
| Designation | `/designations` |
| Employee | `/employees` |
| User | `/users` |
| Role | `/roles` |
| Document | `/documents` |
| Onboarding | `/onboarding` |
| Offboarding | `/offboarding` |
| Probation | `/probation` |
| Transfer | `/transfers` |
| Promotion | `/promotions` |
| Asset | `/assets` |
| Skills | `/skills` |
| Notification | `/notifications` |
| Audit | `/audit-logs` |
| Dashboard | `/dashboard` |
| Report | `/reports` |

### Auth Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/auth/login` | Login, returns JWT + refresh token |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Blacklist token |
| POST | `/auth/change-password` | Change password |

### Standard CRUD Pattern

All resource endpoints follow:

| Method | Path | Description |
|---|---|---|
| GET | `/{resource}` | List (paginated) |
| POST | `/{resource}` | Create |
| GET | `/{resource}/{id}` | Get by ID |
| PUT | `/{resource}/{id}` | Update |
| DELETE | `/{resource}/{id}` | Soft delete |

### Standard Response Format

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

Paginated response:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

---

## Authentication & Security

### JWT Flow
1. Client POSTs credentials to `/auth/login`
2. Server validates, returns `accessToken` (24h) + `refreshToken` (7d)
3. Client sends `Authorization: Bearer <token>` on every request
4. `JwtAuthenticationFilter` validates token on each request
5. On logout, token is added to Redis blacklist (`TokenBlacklist`)

### Security Features
- **Rate limiting** via `RateLimitInterceptor`
- **Tenant isolation** via `TenantValidationInterceptor` — every request scoped to `company_id`
- **Account locking** after repeated failed logins
- **Password change enforcement** (`must_change_password` flag)
- **CORS** configured in `WebMvcConfig`
- **Optimistic locking** via `version` column on all entities

### Roles & Permissions
- Roles are company-scoped
- Permissions are module-level (e.g., `EMPLOYEE_READ`, `EMPLOYEE_WRITE`)
- `SecurityUtils` provides helper methods to check current user permissions

---

## Frontend

### Pages

| Page | Path | Description |
|---|---|---|
| Login | `/login` | Authentication |
| Dashboard | `/dashboard` | KPIs and charts |
| Employees | `/employees` | Employee list and management |
| Departments | `/departments` | Department CRUD |
| Designations | `/designations` | Designation CRUD |
| Branches | `/branches` | Branch CRUD |
| Teams | `/teams` | Team CRUD |
| Users | `/users` | User account management |
| Roles | `/roles` | Role and permission management |
| Documents | `/documents` | Document upload and tracking |
| Onboarding | `/onboarding` | Onboarding checklists |
| Offboarding | `/offboarding` | Offboarding checklists |
| Probation | `/probation` | Probation tracking |
| Transfers | `/transfers` | Transfer requests |
| Promotions | `/promotions` | Promotion records |
| Assets | `/assets` | Asset management |
| Skills | `/skills` | Skills and certifications |
| Notifications | `/notifications` | In-app notifications |
| Audit | `/audit` | Audit log viewer |
| Reports | `/reports` | Export reports |
| Profile | `/profile` | Current user profile |
| Company | `/company` | Company settings |

### Key Hooks

| Hook | Purpose |
|---|---|
| `useCrud` | Generic CRUD operations with React Query |
| `useEmployees` | Employee-specific queries and filters |
| `useDashboard` | Dashboard stats |
| `useNotifications` | Notification list and mark-as-read |
| `useReferenceData` | Dropdowns (departments, branches, etc.) |

### Internationalization
Supported languages: **English**, **Arabic (RTL)**, **Hindi**

Language files: `src/i18n/locales/{en,ar,hi}/translation.json`

Switch language via `LanguageSwitcher` component in the navbar.

---

## Infrastructure & DevOps

### Docker Compose

```bash
docker-compose up -d
```

Services started:
- `hrms-postgres` — PostgreSQL 16 on port `5432`
- `hrms-redis` — Redis 7 on port `6379`
- `hrms-app` — Spring Boot app on port `8080`

### Dockerfile
Multi-stage build in `hrms-platform/Dockerfile`.

### Health Checks
- PostgreSQL: `pg_isready`
- Redis: `redis-cli ping`
- App depends on both being healthy before starting

### Actuator
Spring Boot Actuator enabled at `/api/v1/actuator`

---

## Configuration

### Backend Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `hrms_db` | Database name |
| `DB_USERNAME` | `hrms_user` | DB username |
| `DB_PASSWORD` | `hrms_pass` | DB password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | _(empty)_ | Redis password |
| `JWT_SECRET` | _(default)_ | JWT signing secret (min 256-bit) |
| `JWT_EXPIRATION_MS` | `86400000` | Access token TTL (24h) |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` | Refresh token TTL (7d) |
| `AWS_REGION` | `ap-south-1` | AWS region |
| `S3_BUCKET_NAME` | `hrms-documents` | S3 bucket for documents |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | _(empty)_ | SMTP username |
| `MAIL_PASSWORD` | _(empty)_ | SMTP password |
| `SERVER_PORT` | `8080` | App server port |

### Frontend Environment Variables (`.env`)

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
```

### Profiles
| Profile | File | Use |
|---|---|---|
| default | `application.yml` | Base config |
| dev | `application-dev.yml` | Local development |
| prod | `application-prod.yml` | Production |

---

## Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- Docker & Docker Compose
- Maven 3.9+

### 1. Start Infrastructure

```bash
cd hrms-platform
docker-compose up -d postgres redis
```

### 2. Run Backend

```bash
cd hrms-platform
mvn spring-boot:run
```

Backend starts at: `http://localhost:8080/api/v1`

### 3. Run Frontend

```bash
cd hrms-frontend
npm install
npm start
```

Frontend starts at: `http://localhost:3000`

### 4. Run Full Stack with Docker

```bash
cd hrms-platform
docker-compose up -d
```

### 5. Default Login

After seed data migration (`V2__seed_data.sql`):

| Field | Value |
|---|---|
| Email | `admin@hrms.com` |
| Password | _(set in seed data)_ |

### 6. API Documentation

Open `http://localhost:8080/api/v1/swagger-ui.html` in your browser.

---

## Development Notes

- All entities extend `BaseEntity` which provides `id`, `companyId`, `createdBy`, `createdAt`, `updatedBy`, `updatedAt`, `version`, `isActive`
- Soft deletes are used throughout (`is_active = false`)
- Pagination defaults: page size 20, max 100 (configurable via `app.pagination`)
- Document expiry reminders fire at 90, 60, 30, and 7 days before expiry
- Structured JSON logging in production via Logstash encoder
- Request tracing via MDC: `traceId`, `userId`, `companyId` in every log line
