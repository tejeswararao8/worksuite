package com.platform.tenancy.membership;

/**
 * Lifecycle states for a user-tenant membership.
 * Only ACTIVE memberships grant access to tenant resources.
 */
public enum MembershipStatus {
    /** Invitation sent — user has not yet accepted. No resource access. */
    PENDING,
    /** Full access to the tenant according to the assigned role. */
    ACTIVE,
    /** Temporarily blocked — user cannot access the tenant. */
    SUSPENDED,
    /** Permanently removed — treated as non-existent for access decisions. */
    REVOKED
}
