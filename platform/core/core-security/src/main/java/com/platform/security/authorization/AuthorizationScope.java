package com.platform.security.authorization;

/**
 * Defines the data visibility scope for an operation.
 *
 * <p>Scopes are ordered from narrowest to broadest. A user's effective scope
 * is determined by their role — they cannot request a broader scope than their
 * role permits.
 *
 * <pre>
 *   SELF           → own records only
 *   TEAM           → own records + direct reports
 *   DEPARTMENT     → all records in own department
 *   ORGANIZATION   → all records in own tenant
 *   PLATFORM       → all records across all tenants (platform staff only)
 * </pre>
 */
public enum AuthorizationScope {
    SELF,
    TEAM,
    DEPARTMENT,
    ORGANIZATION,
    PLATFORM
}
