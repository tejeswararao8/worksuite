@echo off
REM ── Database ──────────────────────────────────────────────────────────────
set DB_USERNAME=vveadmin
set DB_PASSWORD=vve@admin123
set DB_NAME=vvesolutions_db
set DB_HOST=localhost
set DB_PORT=5433

REM ── Keycloak ──────────────────────────────────────────────────────────────
set KC_ADMIN_USERNAME=admin
set KC_ADMIN_PASSWORD=admin123

REM ── MinIO ─────────────────────────────────────────────────────────────────
set MINIO_ACCESS_KEY=admin
set MINIO_SECRET_KEY=admin123456

REM ── RabbitMQ ──────────────────────────────────────────────────────────────
set RABBITMQ_USERNAME=admin
set RABBITMQ_PASSWORD=admin123

REM ── Launch ────────────────────────────────────────────────────────────────
mvn spring-boot:run -Dspring-boot.run.profiles=docker -Dspring-boot.run.jvmArguments="-Duser.timezone=Asia/Kolkata"
