# Keycloak Setup Guide - VVE Solutions Platform

## Overview

This document covers the complete Keycloak setup for the VVE Solutions Platform.
Keycloak is our Identity and Access Management (IAM) solution that handles:
- User authentication (login/logout)
- SSO (Single Sign-On) across all modules
- JWT token generation and validation
- Role-based access control (RBAC)
- User management (create, update, disable)

---

## Prerequisites

```
Keycloak running → http://localhost:8080
Docker service   → company_keycloak container running

Verify:
docker compose ps
→ company_keycloak   Up   0.0.0.0:8080->8080/tcp
```

---

## Step 1 → Open Keycloak Admin Console

```
Browser → http://localhost:8080
Click   → "Administration Console"

Login:
  Username → admin
  Password → admin123
```

---

## Step 2 → Create Realm

```
1. Top left corner → Click "master" dropdown
2. Click → "Create Realm"
3. Fill:
   Realm name → vve-platform
   Enabled    → ON
4. Click → "Create"
```

### What is a Realm?
```
A Realm is an isolated space for your application.
All users, roles, clients belong to this realm.
We use ONE realm for the entire VVE platform.
```

---

## Step 3 → Create Roles

```
Left menu → Realm roles → Create role

Create all 9 roles below one by one:
1. Click "Create role"
2. Enter Role name
3. Enter Description
4. Click "Save"
5. Repeat for all roles
```

| Role Name | Description | Access Level |
|-----------|-------------|--------------|
| SUPER_ADMIN | Full access to everything | All modules |
| HR_ADMIN | HRMS + Payroll full access | HR + Payroll |
| HR_MANAGER | HRMS read/write | HR only |
| EMPLOYEE | Own profile, leave, tickets | Self only |
| FINANCE_ADMIN | Finance + Payroll access | Finance |
| PROJECT_MANAGER | Projects + Tickets | Projects |
| SUPPORT_AGENT | Support tickets only | Support |
| IT_ADMIN | IT support tickets | IT Support |
| MANAGER | Team management | Team |

---

## Step 4 → Create Client

```
Left menu → Clients → Create client
```

### Step 4.1 - General Settings
```
Client type  → OpenID Connect
Client ID    → vve-platform-app
Name         → VVE Platform App
Description  → VVE Solutions Platform Backend Client
Click → Next
```

### Step 4.2 - Capability Config
```
Client authentication → ON
Authorization         → OFF
Standard flow         → ON (checked)
Direct access grants  → ON (checked)
Click → Next
```

### Step 4.3 - Login Settings
```
Root URL             → http://localhost:8081
Valid redirect URIs  → http://localhost:8081/*
Valid redirect URIs  → http://localhost:3000/*
Web origins          → *
Click → Save
```

---

## Step 5 → Get Client Secret

```
Left menu → Clients → vve-platform-app
Click tab → "Credentials"
Copy      → Client secret value

Save this value — needed in application.yml:
client-secret: <paste-your-secret-here>
```

---

## Step 6 → Add companyId Custom Claim to JWT

```
This is the MOST IMPORTANT step.
Every JWT token must carry companyId
so Spring Boot knows which tenant the user belongs to.
```

### Create Protocol Mapper
```
Left menu → Clients → vve-platform-app
Click tab → "Client scopes"
Click     → "vve-platform-app-dedicated"
Click     → "Add mapper" → "By configuration"
Select    → "User Attribute"

Fill:
  Name             → companyId
  User Attribute   → companyId
  Token Claim Name → companyId
  Claim JSON Type  → String
  Add to ID token        → ON
  Add to access token    → ON
  Add to userinfo        → ON
Click → "Save"
```

### Why companyId in JWT?
```
Every API request carries the JWT token.
Spring Boot reads companyId from the token.
TenantContext stores it for the request duration.
All database queries automatically filter by companyId.
This is how multi-tenant isolation works.
```

---

## Step 7 → Create Test Users

### User 1 - Super Admin

```
Left menu → Users → Create new user

General tab:
  Username       → admin@vvesolutions.com
  Email          → admin@vvesolutions.com
  First name     → Super
  Last name      → Admin
  Email verified → ON
  Enabled        → ON
Click → "Create"
```

```
Credentials tab:
  Click → "Set password"
  Password  → Admin@123
  Temporary → OFF
  Click → "Save" → "Save password"
```

```
Attributes tab:
  Click → "Add attribute"
  Key   → companyId
  Value → 00000000-0000-0000-0000-000000000001
  Click → "Save"
```

```
Role mapping tab:
  Click → "Assign role"
  Search → SUPER_ADMIN
  Check  → SUPER_ADMIN
  Click  → "Assign"
```

---

### User 2 - HR Admin

```
Left menu → Users → Create new user

General tab:
  Username       → hr@vvesolutions.com
  Email          → hr@vvesolutions.com
  First name     → HR
  Last name      → Admin
  Email verified → ON
  Enabled        → ON
Click → "Create"

Credentials tab:
  Password  → Hr@123
  Temporary → OFF

Attributes tab:
  companyId → 00000000-0000-0000-0000-000000000001

Role mapping tab:
  Assign → HR_ADMIN
```

---

### User 3 - Employee

```
Left menu → Users → Create new user

General tab:
  Username       → employee@vvesolutions.com
  Email          → employee@vvesolutions.com
  First name     → Test
  Last name      → Employee
  Email verified → ON
  Enabled        → ON
Click → "Create"

Credentials tab:
  Password  → Emp@123
  Temporary → OFF

Attributes tab:
  companyId → 00000000-0000-0000-0000-000000000001

Role mapping tab:
  Assign → EMPLOYEE
```

---

## Step 8 → Test Token Generation

### Using curl
```bash
curl -X POST \
  http://localhost:8080/realms/vve-platform/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=vve-platform-app" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=admin@vvesolutions.com" \
  -d "password=Admin@123"
```

### Using Postman
```
Method  → POST
URL     → http://localhost:8080/realms/vve-platform/protocol/openid-connect/token
Body    → x-www-form-urlencoded

Key              Value
grant_type    →  password
client_id     →  vve-platform-app
client_secret →  YOUR_CLIENT_SECRET
username      →  admin@vvesolutions.com
password      →  Admin@123
```

### Expected Response
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5...",
  "token_type": "Bearer",
  "session_state": "uuid",
  "scope": "openid profile email"
}
```

---

## Step 9 → Verify JWT Contains companyId

```
1. Copy the access_token value from Step 8 response
2. Go to → https://jwt.io
3. Paste token in "Encoded" box on the left
4. Check "Payload" section on the right
```

### Expected JWT Payload
```json
{
  "sub": "keycloak-user-uuid",
  "email": "admin@vvesolutions.com",
  "given_name": "Super",
  "family_name": "Admin",
  "companyId": "00000000-0000-0000-0000-000000000001",
  "realm_access": {
    "roles": [
      "SUPER_ADMIN",
      "default-roles-vve-platform"
    ]
  },
  "exp": 1234567890,
  "iat": 1234567890
}
```

```
✅ companyId present → Multi-tenant isolation works
✅ roles present     → RBAC works
✅ sub present       → User identification works
```

---

## Step 10 → Update Spring Boot application.yml

```yaml
# platform/app/src/main/resources/application.yml

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/vve-platform

keycloak:
  auth-server-url: http://localhost:8080
  realm: vve-platform
  admin:
    client-id: admin-cli
    username: admin
    password: admin123
```

---

## Step 11 → Test Spring Boot API with Token

```bash
# Get token first (from Step 8)
TOKEN="eyJhbGciOiJSUzI1NiIsInR5..."

# Call Spring Boot API with token
curl -X GET http://localhost:8081/api/v1/employees \
  -H "Authorization: Bearer $TOKEN"

# Expected → 200 OK
# Without token → 401 Unauthorized
# Wrong role → 403 Forbidden
```

---

## How Roles Work in Spring Boot Controllers

```java
// Only HR_ADMIN and SUPER_ADMIN can list all employees
@GetMapping("/employees")
@PreAuthorize("hasRole('HR_ADMIN') or hasRole('SUPER_ADMIN')")
public ResponseEntity<?> getAllEmployees() { }

// Any authenticated employee can view their own profile
@GetMapping("/my-profile")
@PreAuthorize("hasRole('EMPLOYEE')")
public ResponseEntity<?> getMyProfile() { }

// Only SUPER_ADMIN can delete
@DeleteMapping("/employees/{id}")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public ResponseEntity<?> deleteEmployee(@PathVariable UUID id) { }

// Finance module
@GetMapping("/payroll")
@PreAuthorize("hasRole('FINANCE_ADMIN') or hasRole('HR_ADMIN')")
public ResponseEntity<?> getPayroll() { }
```

---

## How to Add a New Company (Tenant)

```
When a new company registers on the platform:

1. Create company record in PostgreSQL (tenants table)
   → Gets a new UUID as companyId

2. Create users in Keycloak for that company
   → Set companyId attribute = new company UUID
   → Assign appropriate roles

3. All data for that company is automatically
   isolated by companyId in every database query
```

---

## Keycloak URLs Reference

| URL | Purpose |
|-----|---------|
| http://localhost:8080 | Keycloak home |
| http://localhost:8080/admin | Admin console |
| http://localhost:8080/realms/vve-platform | Realm info |
| http://localhost:8080/realms/vve-platform/protocol/openid-connect/token | Get token |
| http://localhost:8080/realms/vve-platform/protocol/openid-connect/userinfo | User info |
| http://localhost:8080/realms/vve-platform/protocol/openid-connect/logout | Logout |
| http://localhost:8080/realms/vve-platform/protocol/openid-connect/certs | Public keys (JWKS) |

---

## Test Users Summary

| Email | Password | Role | companyId |
|-------|----------|------|-----------|
| admin@vvesolutions.com | Admin@123 | SUPER_ADMIN | 00000000-0000-0000-0000-000000000001 |
| hr@vvesolutions.com | Hr@123 | HR_ADMIN | 00000000-0000-0000-0000-000000000001 |
| employee@vvesolutions.com | Emp@123 | EMPLOYEE | 00000000-0000-0000-0000-000000000001 |

---

## Troubleshooting

### Token request fails
```
Error: invalid_client
Fix  : Check client_secret is correct in Step 5
```

### companyId missing from JWT
```
Error: companyId claim not in token
Fix  : Verify Step 6 protocol mapper is saved correctly
       Check user has companyId attribute set in Step 7
```

### 401 Unauthorized from Spring Boot
```
Error: JWT validation failed
Fix  : Check issuer-uri in application.yml matches realm URL
       Verify Keycloak is running on port 8080
```

### 403 Forbidden from Spring Boot
```
Error: Access denied
Fix  : Check user has correct role assigned in Keycloak
       Verify @PreAuthorize annotation has correct role name
```

### Realm not found
```
Error: Realm does not exist
Fix  : Verify realm name is exactly "vve-platform" (lowercase, hyphen)
```

---

*Document created: 2024*
*Keycloak version: Latest*
*Realm: vve-platform*
*Status: Setup guide*
