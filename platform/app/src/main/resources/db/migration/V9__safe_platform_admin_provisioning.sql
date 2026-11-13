-- V9: Safe platform admin membership provisioning
--
-- REPLACES the automatic privileged insert from V8.
--
-- V8 inserted an ACTIVE platform membership using a hardcoded Keycloak user ID
-- (181872b4-0418-4687-8550-23d296a144b1). This is unsafe because:
--   1. It grants production platform access without explicit operator authorisation.
--   2. The Keycloak user ID is environment-specific and may differ across deployments.
--   3. It is not auditable — no record of who approved the grant.
--
-- STEP 1: Revoke the V8 auto-grant.
--   Remove the membership inserted by V8 for the hardcoded user ID.
--   If V8 was never applied (fresh environment), this is a no-op.
--
DELETE FROM user_tenant_memberships
WHERE keycloak_user_id = '181872b4-0418-4687-8550-23d296a144b1'
  AND tenant_id        = '00000000-0000-0000-0000-000000000001'
  AND role_name        = 'PLATFORM_SUPER_ADMIN';

-- STEP 2: Create an operator-driven provisioning function.
--   To grant platform access to a user, an authorised operator must call:
--
--     SELECT provision_platform_admin('<keycloak-user-id>', '<role-name>');
--
--   Where:
--     <keycloak-user-id>  is the Keycloak user UUID (JWT sub claim)
--     <role-name>         is one of: PLATFORM_SUPER_ADMIN, PLATFORM_ADMIN,
--                                    PLATFORM_SUPPORT, PLATFORM_IMPLEMENTATION
--
--   This call must be:
--     - Executed by a DBA or authorised deployment engineer.
--     - Recorded in the deployment runbook with the approver's name and date.
--     - Verified by checking: SELECT * FROM user_tenant_memberships
--                             WHERE tenant_id = '00000000-0000-0000-0000-000000000001';
--
--   Example (run manually after deployment, NOT in this migration):
--     SELECT provision_platform_admin(
--         '<your-keycloak-user-uuid>',
--         'PLATFORM_SUPER_ADMIN'
--     );

CREATE OR REPLACE FUNCTION provision_platform_admin(
    p_keycloak_user_id VARCHAR(36),
    p_role_name        VARCHAR(100)
)
RETURNS TEXT
LANGUAGE plpgsql
AS $$
DECLARE
    v_platform_tenant UUID := '00000000-0000-0000-0000-000000000001';
    v_valid_roles     TEXT[] := ARRAY[
        'PLATFORM_SUPER_ADMIN',
        'PLATFORM_ADMIN',
        'PLATFORM_SUPPORT',
        'PLATFORM_IMPLEMENTATION'
    ];
BEGIN
    -- Validate role name
    IF NOT (p_role_name = ANY(v_valid_roles)) THEN
        RAISE EXCEPTION 'Invalid platform role: %. Must be one of: %',
            p_role_name, array_to_string(v_valid_roles, ', ');
    END IF;

    -- Validate user ID format
    IF p_keycloak_user_id IS NULL OR length(trim(p_keycloak_user_id)) <> 36 THEN
        RAISE EXCEPTION 'Invalid Keycloak user ID: must be a 36-character UUID string';
    END IF;

    -- Insert or update membership
    INSERT INTO user_tenant_memberships
        (id, keycloak_user_id, tenant_id, role_name, membership_status, joined_at, created_at, updated_at)
    VALUES (
        gen_random_uuid(),
        p_keycloak_user_id,
        v_platform_tenant,
        p_role_name,
        'ACTIVE',
        NOW(),
        NOW(),
        NOW()
    )
    ON CONFLICT (keycloak_user_id, tenant_id)
    DO UPDATE SET
        role_name         = EXCLUDED.role_name,
        membership_status = 'ACTIVE',
        updated_at        = NOW();

    RETURN format('Platform membership provisioned: user=%s role=%s status=ACTIVE',
                  p_keycloak_user_id, p_role_name);
END;
$$;

-- STEP 3: Grant execute only to the application DB user (read-only for app, explicit for ops).
--   Replace 'vveadmin' with your actual application DB username if different.
REVOKE ALL ON FUNCTION provision_platform_admin(VARCHAR, VARCHAR) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION provision_platform_admin(VARCHAR, VARCHAR) TO vveadmin;

-- ─── DEPLOYMENT RUNBOOK ───────────────────────────────────────────────────────
-- After running this migration, the platform has NO active platform admin.
-- Before the application can serve platform API requests, an operator MUST run:
--
--   1. Find the Keycloak user ID for the platform admin:
--      GET http://localhost:8080/admin/realms/vve-platform/users?email=<admin-email>
--      (requires Keycloak admin credentials — never stored in source)
--
--   2. Connect to the database and run:
--      SELECT provision_platform_admin('<keycloak-uuid>', 'PLATFORM_SUPER_ADMIN');
--
--   3. Verify:
--      SELECT keycloak_user_id, role_name, membership_status
--      FROM user_tenant_memberships
--      WHERE tenant_id = '00000000-0000-0000-0000-000000000001';
--
--   4. Record in deployment log: date, operator name, Keycloak user ID provisioned.
-- ─────────────────────────────────────────────────────────────────────────────
