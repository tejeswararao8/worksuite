# Infrastructure - Company Platform

All Docker services for the Company Platform.

---

## Folder Structure

```
infrastructure/
├── docker-compose.yml        ← Development services
├── docker-compose.prod.yml   ← Production overrides
├── nginx/
│   └── nginx.conf            ← Reverse proxy config
└── README.md                 ← This file
```

---

## Services Overview

| Service | Port | Dashboard URL | Login |
|---------|------|--------------|-------|
| PostgreSQL | 5433 | No UI | vveadmin / vve@admin123 |
| Valkey | 6379 | No UI | - |
| RabbitMQ | 5672 | http://localhost:15672 | admin / admin123 |
| Keycloak | 8080 | http://localhost:8080 | admin / admin123 |
| MongoDB | 27017 | No UI | - |
| MinIO | 9000 | http://localhost:9001 | admin / admin123456 |
| OpenSearch | 9200 | http://localhost:5601 | - |
| Jitsi | 8443 | http://localhost:8443 | - |

---

## Phase Guide - Start Only What You Need

### Phase 1 - HRMS + Auth
```bash
docker compose up -d postgres valkey rabbitmq keycloak
```
RAM Usage: ~1GB
PostgreSQL → runs in Docker on port 5433

### Phase 2 - Chat Module
```bash
docker compose up -d mongodb
```
RAM Usage: +300MB

### Phase 3 - File Uploads
```bash
docker compose up -d minio
```
RAM Usage: +200MB

### Phase 4 - Search Feature
```bash
docker compose up -d opensearch opensearch-dashboards
```
RAM Usage: +1.3GB (heavy - start alone)

### Phase 5 - Video Calls
```bash
docker compose up -d jitsi
```
RAM Usage: +300MB

---

## Common Commands

### Start specific service
```bash
docker compose up -d postgres
docker compose up -d valkey
docker compose up -d rabbitmq
docker compose up -d keycloak
docker compose up -d mongodb
docker compose up -d minio
docker compose up -d opensearch
docker compose up -d jitsi
```

### Stop specific service
```bash
docker compose stop valkey
docker compose stop keycloak
docker compose stop opensearch
```

### Start multiple services
```bash
docker compose up -d valkey rabbitmq keycloak
```

### Stop all services
```bash
docker compose down
```

### Stop all and remove volumes (WARNING: deletes all data)
```bash
docker compose down -v
```

### See what is running
```bash
docker compose ps
```

### View logs of a service
```bash
docker compose logs -f rabbitmq
docker compose logs -f keycloak
docker compose logs -f valkey
```

### Restart a service
```bash
docker compose restart keycloak
```

### Pull latest images
```bash
docker compose pull
```

---

## RAM Usage Per Service

```
Valkey              →  ~50MB
RabbitMQ            → ~200MB
Keycloak            → ~512MB
MongoDB             → ~300MB
MinIO               → ~200MB
OpenSearch          →   ~1GB
OpenSearch Dashboard→  ~300MB
Jitsi               →  ~300MB
```

---

## Production Deployment

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## Troubleshooting

### Keycloak not starting
```bash
docker compose logs -f keycloak
# Check if port 8080 is already in use
netstat -ano | findstr :8080
```

### RabbitMQ not starting
```bash
docker compose logs -f rabbitmq
# Check if port 5672 is already in use
netstat -ano | findstr :5672
```

### OpenSearch out of memory
```bash
# Add memory limit in docker-compose.yml
environment:
  - OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m
```

### Reset a service data
```bash
# Stop service
docker compose stop mongodb
# Remove its volume
docker volume rm infrastructure_mongo_data
# Start again fresh
docker compose up -d mongodb
```

---

## Spring Boot Connection Strings

Add these to your `application.yml`:

```yaml
spring:
  # PostgreSQL (Docker)
  datasource:
    url: jdbc:postgresql://localhost:5433/vvesolutions_db
    username: vveadmin
    password: vve@admin123

  # MongoDB
  data:
    mongodb:
      uri: mongodb://localhost:27017/company_chat_db

  # Valkey (Redis compatible)
  redis:
    host: localhost
    port: 6379

  # RabbitMQ
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123

  # Keycloak
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/company

# OpenSearch
opensearch:
  host: localhost
  port: 9200

# MinIO
minio:
  endpoint: http://localhost:9000
  access-key: admin
  secret-key: admin123456
  bucket-name: company-files
```
