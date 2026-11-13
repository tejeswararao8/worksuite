-- V10: Restrict provision_platform_admin() to DBA role only
--
-- SECURITY FIX for V9.
--
-- V9 granted EXECUTE on provision_platform_admin() to 'vveadmin', which is the
-- application database user. This means the running Spring Boot application could
-- call the function via any SQL execution path (e.g. a future admin endpoint,
-- a SQL injection vulnerability, or a misconfigured JPA native query).
--
-- This migration:
--   1. Revokes EXECUTE from the application user (vveadmin).
--   2. Creates a dedicated 'platform_ops' role that has no login privilege.
--   3. Grants EXECUTE only to 'platform_ops'.
--   4. A human DBA must SET ROLE platform_ops before calling the function.
--      The application user cannot SET ROLE to platform_ops.
--
-- After this migration, provisioning a platform admin requires:
--   psql -U <dba-superuser> -d vvesolutions_db
--   SET ROLE platform_ops;
--   SELECT provision_platform_admin('<keycloak-uuid>', 'PLATFORM_SUPER_ADMIN');
--   RESET ROLE;
--
-- The application user (vveadmin) cannot SET ROLE to platform_ops because
-- it has not been granted that role.

-- Step 1: Revoke from application user
REVOKE EXECUTE ON FUNCTION provision_platform_admin(VARCHAR, VARCHAR) FROM vveadmin;
REVOKE EXECUTE ON FUNCTION provision_platform_admin(VARCHAR, VARCHAR) FROM PUBLIC;

-- Step 2: Create the ops role (no login, no password — humans assume it via SET ROLE)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'platform_ops') THEN
        CREATE ROLE platform_ops NOLOGIN;
    END IF;
END
$$;

-- Step 3: Grant EXECUTE only to the ops role
GRANT EXECUTE ON FUNCTION provision_platform_admin(VARCHAR, VARCHAR) TO platform_ops;

-- Step 4: Ensure vveadmin cannot assume platform_ops
-- (vveadmin is not granted platform_ops, so SET ROLE platform_ops will fail for it)
-- No explicit REVOKE needed — roles are not granted unless explicitly done.

-- ─── UPDATED DEPLOYMENT RUNBOOK ──────────────────────────────────────────────
-- To provision the first platform administrator in any environment:
--
--   1. Find the Keycloak user ID (JWT sub claim) for the admin user:
--      Connect to Keycloak Admin Console or run:
--      GET http://<keycloak-host>:8080/admin/realms/vve-platform/users?email=<admin-email>
--      (requires Keycloak admin credentials — never stored in source)
--
--   2. Connect to the database as a superuser or a user with platform_ops membership:
--      psql -h localhost -p 5433 -U <dba-user> -d vvesolutions_db
--
--   3. Assume the ops role:
--      SET ROLE platform_ops;
--
--   4. Provision the admin:
--      SELECT provision_platform_admin('<keycloak-uuid>', 'PLATFORM_SUPER_ADMIN');
--
--   5. Verify:
--      SELECT keycloak_user_id, role_name, membership_status, joined_at
--      FROM user_tenant_memberships
--      WHERE tenant_id = '00000000-0000-0000-0000-000000000001';
--
--   6. Reset role and disconnect:
--      RESET ROLE;
--      \q
--
--   7. Record in deployment log: date, operator name, Keycloak user ID provisioned,
--      approver name.
--
-- The application user (vveadmin) cannot call provision_platform_admin() directly.
-- ─────────────────────────────────────────────────────────────────────────────
