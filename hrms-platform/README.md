# HRMS Platform — Setup & Run Guide

## Prerequisites

| Tool | Required Version | Check Command |
|------|-----------------|---------------|
| Java JDK | 21 or higher | `java -version` |
| Maven | Not needed (wrapper included) | `mvnw.cmd -version` |
| Git | Any | `git --version` |
| Docker (optional) | 20+ | `docker --version` |

> **Note:** The project was built for Java 21 but runs on Java 25 with the fixes applied.

---

## Option 1 — Local Profile (Quickest, No External Services)

Uses **H2 in-memory database** and **no Redis**. Everything runs out of the box.

### Step 1 — Clone / Open the project

```
cd hrms-platform
```

### Step 2 — Run

**Windows:**
```cmd
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Step 3 — Verify startup

Look for this line in the console:
```
Started HrmsPlatformApplication in X.XXX seconds
```

### Step 4 — Access the application

| URL | Description |
|-----|-------------|
| http://localhost:8080/api/v1/swagger-ui.html | Swagger UI (API docs) |
| http://localhost:8080/api/v1/h2-console | H2 Database Console |
| http://localhost:8080/api/v1/actuator/health | Health check |

**H2 Console connection settings:**
- JDBC URL: `jdbc:h2:mem:hrmsdb`
- Username: `sa`
- Password: *(leave blank)*

---

## Option 2 — Dev Profile (PostgreSQL + Redis via Docker)

### Step 1 — Start infrastructure

```cmd
docker-compose up -d
```

This starts:
- PostgreSQL on port `5432`
- Redis on port `6379`

### Step 2 — Run

```cmd
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

### Step 3 — Environment variables (optional overrides)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | hrms_db | Database name |
| `DB_USERNAME` | hrms_user | DB username |
| `DB_PASSWORD` | hrms_pass | DB password |
| `REDIS_HOST` | localhost | Redis host |
| `REDIS_PORT` | 6379 | Redis port |
| `JWT_SECRET` | (default key) | JWT signing secret |
| `SERVER_PORT` | 8080 | App port |

Set them before running:
```cmd
set DB_PASSWORD=mypassword
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Option 3 — Build & Run as JAR

```cmd
mvnw.cmd package -DskipTests
java -jar target\hrms-platform-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

---

## Option 4 — Docker (Full Stack)

```cmd
docker-compose up --build
```

The app will be available at `http://localhost:8080/api/v1`

---

## First Login

After startup, seed data (V2 migration) creates a default admin user.

Check `src/main/resources/db/migration/V2__seed_data.sql` for the default credentials.

Use the `/api/v1/auth/login` endpoint via Swagger UI to get a JWT token.

---

## Project Structure

```
hrms-platform/
├── src/main/java/com/hrms/
│   ├── auth/          # Authentication (login, refresh, logout)
│   ├── employee/      # Employee management
│   ├── company/       # Company & multi-tenant setup
│   ├── department/    # Departments
│   ├── branch/        # Branches
│   ├── role/          # Roles & permissions
│   ├── document/      # Employee documents
│   ├── onboarding/    # Onboarding checklists
│   ├── offboarding/   # Offboarding checklists
│   ├── security/      # JWT, filters
│   └── config/        # Spring configs
├── src/main/resources/
│   ├── db/migration/  # Flyway SQL migrations
│   ├── application.yml           # Base config (prod)
│   ├── application-local.yml     # Local (H2, no Redis)
│   └── application-dev.yml       # Dev (PostgreSQL + Redis)
└── pom.xml
```

---

## Troubleshooting

### Port 8080 already in use
```cmd
set SERVER_PORT=9090
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

### Flyway migration error on restart
H2 is in-memory so it resets on every restart — this is expected. If you see a checksum error, it means the SQL files changed after a previous run. Just restart the app fresh.

### Java version issues
The project targets Java 21 bytecode. If you have Java 25, it works but you may see deprecation warnings — these are harmless.

### Out of memory
```cmd
set MAVEN_OPTS=-Xmx512m
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```
