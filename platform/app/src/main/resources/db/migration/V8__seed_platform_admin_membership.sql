-- V8: Seed platform admin membership
--
-- Creates an ACTIVE UserTenantMembership for the platform super-admin user
-- (admin@company.com, Keycloak ID: 181872b4-0418-4687-8550-23d296a144b1)
-- in the platform pseudo-tenant (00000000-0000-0000-0000-000000000001).
--
-- This membership is required for:
--   1. PlatformDashboardService.platformEmployees count to be accurate.
--   2. Any future code that validates platform membership explicitly.
--
-- The JwtAuthFilter already bypasses membership validation for the platform
-- tenant, so API access is not blocked without this row. This migration
-- makes the data consistent with the security model.
--
-- Safe to re-run: INSERT ... ON CONFLICT DO NOTHING.

INSERT INTO user_tenant_memberships
    (id, keycloak_user_id, tenant_id, role_name, membership_status, joined_at, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    '181872b4-0418-4687-8550-23d296a144b1',
    '00000000-0000-0000-0000-000000000001',
    'PLATFORM_SUPER_ADMIN',
    'ACTIVE',
    NOW(),
    NOW(),
    NOW()
)
ON CONFLICT (keycloak_user_id, tenant_id) DO NOTHING;
