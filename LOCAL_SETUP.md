# Local Setup Guide

Complete step-by-step guide to run the platform on your local machine.

---

## Prerequisites

Install all of these before starting.

| Tool | Version | Download |
|---|---|---|
| Java (JDK) | 21 | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org/download.cgi |
| Docker Desktop | Latest | https://www.docker.com/products/docker-desktop |
| Node.js | 20+ | https://nodejs.org |

Verify everything is installed:

```bash
java -version
# Expected: openjdk version "21.x.x"

mvn -version
# Expected: Apache Maven 3.9.x

docker -version
# Expected: Docker version 24.x.x

node -version
# Expected: v20.x.x
```

---

## Step 1 — Start Docker Services

Open a terminal and navigate to the infrastructure folder:

```bash
cd path\to\worksuite\infrastructure
```

Start Phase 1 services (PostgreSQL + Valkey + RabbitMQ + Keycloak):

```bash
docker compose up -d postgres valkey rabbitmq keycloak
```

Wait about 60 seconds for all services to start. Then verify:

```bash
docker compose ps
```

You should see all 4 containers with status `running` or `healthy`.

| Service | Port | Purpose |
|---|---|---|
| PostgreSQL | 5433 | Main database |
| Valkey | 6379 | Cache + sessions |
| RabbitMQ | 5672 / 15672 | Message queues |
| Keycloak | 8080 | Authentication |

---

## Step 2 — Setup Keycloak (One-Time Only)

Keycloak is the identity provider. You must configure it once before the backend can start.

### 2.1 — Open Keycloak Admin Console

Go to: http://localhost:8080

Login with:
- Username: `admin`
- Password: `admin123`

### 2.2 — Create the Realm

1. Click the dropdown at the top-left (shows `Keycloak`)
2. Click **Create realm**
3. Set Realm name: `vve-platform`
4. Click **Create**

### 2.3 — Create a Client

1. In the left menu → **Clients** → **Create client**
2. Client ID: `vve-backend`
3. Client type: `OpenID Connect`
4. Click **Next**
5. Turn ON **Client authentication**
6. Click **Next** → **Save**

### 2.4 — Add a Custom Claim (companyId)

The backend reads `companyId` from the JWT token. You must add it as a mapper.

1. Go to **Clients** → `vve-backend` → **Client scopes** tab
2. Click `vve-backend-dedicated`
3. Click **Add mapper** → **By configuration** → **User Attribute**
4. Fill in:
   - Name: `companyId`
   - User Attribute: `companyId`
   - Token Claim Name: `companyId`
   - Claim JSON Type: `String`
   - Add to ID token: ON
   - Add to access token: ON
5. Click **Save**

### 2.5 — Create a Test User

1. Left menu → **Users** → **Create new user**
2. Fill in:
   - Username: `testuser`
   - Email: `test@platform.com`
   - First name: `Test`
   - Last name: `User`
3. Click **Create**
4. Go to **Credentials** tab → **Set password**
   - Password: `Test@1234`
   - Temporary: OFF
5. Click **Save password**

### 2.6 — Set companyId Attribute on the User

> **Note:** In Keycloak 22+, you must first define the attribute in Realm Settings before it appears on the user's Attributes tab.

#### Step A — Define the attribute in Realm Settings

1. Left menu → **Realm settings** → **User profile** tab
2. Click **Add attribute**
3. Fill in:
   - Attribute name: `companyId`
   - Display name: `Company ID`
4. Click **Save**

#### Step B — Assign the attribute to the user

1. Left menu → **Users** → click **`testuser`**
2. Click the **Attributes** tab (now visible after Step A)
3. Click **Add attribute**
4. Fill in:
   - Key: `companyId`
   - Value: `00000000-0000-0000-0000-000000000001`
5. Click **Save**

---

## Step 3 — Build the Backend

Open a new terminal:

```bash
cd path\to\worksuite\platform
```

Build all modules (skipping tests for first run):

```bash
mvn clean install -DskipTests
```

Expected output at the end:
```
[INFO] BUILD SUCCESS
```

If you see `BUILD FAILURE`, check the error message and refer to the Troubleshooting section below.

---

## Step 4 — Run the Backend

```bash
cd app

mvn spring-boot:run -Dspring-boot.run.profiles=docker -Dspring-boot.run.jvmArguments="-Duser.timezone=Asia/Kolkata"
```

The app starts on port **8081**. Wait for this log line:
```
Started PlatformApplication in X.XXX seconds
```

### Verify the backend is running

Open in browser:
- Health check: http://localhost:8081/api/v1/actuator/health
- Swagger UI: http://localhost:8081/api/v1/swagger-ui.html

---

## Step 5 — Run the Frontend

Open a new terminal:

```bash
cd path\to\worksuite\platform\frontend
```

Install dependencies (first time only):

```bash
npm install
```

Start the dev server:

```bash
npm start
```

Opens automatically at: http://localhost:3000

---

## Step 6 — Login to the App

1. Go to http://localhost:3000
2. The login page will redirect to Keycloak
3. Login with:
   - Username: `testuser`
   - Password: `Test@1234`
4. You will be redirected back to the dashboard

---

## Service URLs Summary

| Service | URL | Credentials |
|---|---|---|
| Frontend | http://localhost:3000 | testuser / Test@1234 |
| Backend API | http://localhost:8081/api/v1 | JWT token required |
| Swagger UI | http://localhost:8081/api/v1/swagger-ui.html | — |
| Keycloak Admin | http://localhost:8080 | admin / admin123 |
| RabbitMQ Dashboard | http://localhost:15672 | admin / admin123 |
| H2 Console (local profile) | http://localhost:8081/api/v1/h2-console | sa / (no password) |

---

## Spring Profiles

The app has 3 profiles. Use the right one for your situation.

| Profile | Command | Database | Keycloak | Redis | Use When |
|---|---|---|---|---|---|
| `docker` | `-Dspring-boot.run.profiles=docker` | PostgreSQL (Docker) | Required | Required | Normal development |
| `dev` | `-Dspring-boot.run.profiles=dev` | PostgreSQL (Docker) | Required | Required | Same as docker |
| `local` | `-Dspring-boot.run.profiles=local` | H2 in-memory | Not needed | Not needed | Quick testing, no Docker |

### Run with local profile (no Docker needed)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

With the `local` profile:
- Uses H2 in-memory database (auto-creates all tables)
- No Keycloak required (OAuth2 is disabled)
- No Redis required (uses in-memory cache)
- No RabbitMQ required (listeners disabled)
- All APIs are accessible without a JWT token

---

## Stopping Everything

Stop the backend: `Ctrl+C` in the backend terminal

Stop the frontend: `Ctrl+C` in the frontend terminal

Stop Docker services:
```bash
cd path\to\worksuite\infrastructure

# Stop but keep data
docker compose stop

# Stop and delete all data (fresh start)
docker compose down -v
```

---

## Troubleshooting

### Backend fails to start — `invalid value for parameter "TimeZone": "Asia/Calcutta"`

PostgreSQL 15+ does not recognise the old `Asia/Calcutta` timezone name. The JVM default on some Windows machines is still set to that value.

Always start the backend with the timezone JVM argument:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker -Dspring-boot.run.jvmArguments="-Duser.timezone=Asia/Kolkata"
```

---

### Backend fails to start — `superuser password not specified` (PostgreSQL)

This happens when a stale Docker volume exists from a previous run without a password set. Fix by removing the volume and recreating:

```bash
docker compose down -v
docker compose up -d postgres valkey rabbitmq keycloak
```

> **Warning:** `down -v` deletes all database data. Only do this on a fresh setup.

---

### Backend fails to start — `Connection refused` to PostgreSQL

PostgreSQL container is not running or not healthy yet.

```bash
docker compose ps
docker compose logs postgres
```

If not healthy, restart it:
```bash
docker compose restart postgres
```

---

### Backend fails to start — `Connection refused` to Keycloak / JWT issuer

Keycloak is not running or the realm does not exist yet.

```bash
docker compose logs keycloak
```

Make sure:
1. Keycloak container is running
2. You created the `vve-platform` realm (Step 2.2)

Or use the `local` profile to skip Keycloak entirely.

---

### Backend fails to start — `Connection refused` to Redis

Valkey container is not running.

```bash
docker compose restart valkey
```

---

### `BUILD FAILURE` — `Cannot find symbol` or `package does not exist`

A module is missing from the build. Run from the platform root:

```bash
cd path\to\worksuite\platform
mvn clean install -DskipTests
```

---

### `BUILD FAILURE` — `Duplicate bean definition`

Two beans of the same type are registered. Most likely `SecurityFilterChain`.

Check that `app/src/main/java/com/platform/config/SecurityConfig.java` does NOT define a `SecurityFilterChain` bean — that is already defined in `core-security`.

---

### Frontend — `npm install` fails

Clear the cache and retry:

```bash
npm cache clean --force
npm install
```

---

### Frontend — API calls return 401 Unauthorized

1. Make sure the backend is running on port 8081
2. Make sure `frontend/.env` has: `REACT_APP_API_BASE_URL=http://localhost:8081/api/v1`
3. Make sure you are logged in (Keycloak token is valid)

---

### Flyway migration error on startup

If you see `Migration checksum mismatch` or `already applied`:

```bash
# Connect to PostgreSQL and clean flyway history
docker exec -it company_postgres psql -U vveadmin -d vvesolutions_db -c "DELETE FROM flyway_schema_history;"
```

Then restart the backend.

---

### RabbitMQ connection refused

```bash
docker compose restart rabbitmq
docker compose logs rabbitmq
```

Wait 30 seconds after restart before starting the backend.

---

## Database Access (Direct)

Connect to PostgreSQL directly using any SQL client (DBeaver, TablePlus, pgAdmin):

| Field | Value |
|---|---|
| Host | localhost |
| Port | 5433 |
| Database | vvesolutions_db |
| Username | vveadmin |
| Password | vve@admin123 |

Or via terminal:

```bash
docker exec -it company_postgres psql -U vveadmin -d vvesolutions_db
```

---

## Adding More Docker Services (Optional)

Start MinIO (file storage):
```bash
docker compose up -d minio
```
Dashboard: http://localhost:9001 — admin / admin123456

Start MongoDB (chat module):
```bash
docker compose up -d mongodb
```

Start OpenSearch (search feature):
```bash
docker compose up -d opensearch
```
Note: OpenSearch uses ~1GB RAM. Start it only when needed.
