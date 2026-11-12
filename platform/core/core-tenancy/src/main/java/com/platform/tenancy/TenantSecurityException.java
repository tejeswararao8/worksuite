package com.platform.tenancy;

/**
 * Thrown when a request fails tenant security validation:
 * missing tenant, suspended tenant, inactive tenant, or cross-tenant access.
 *
 * <p>This is intentionally separate from {@link TenantException} (business logic)
 * so that security violations can be mapped to HTTP 403 in the global handler
 * without leaking internal details.
 */
public class TenantSecurityException extends RuntimeException {

    public TenantSecurityException(String message) {
        super(message);
    }
}
