package com.platform.hr.subscription;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the annotated controller method requires the tenant to have
 * the specified module enabled in their subscription plan.
 *
 * <p>The check is enforced by {@link ModuleAccessAspect} before the method executes.
 * The tenant ID is always read from {@code TenantContext} (populated from the JWT
 * {@code companyId} claim) — never from a request parameter.
 *
 * <p>Platform admins (platform pseudo-tenant) bypass this check automatically.
 *
 * <p>Canonical module keys (must match values in subscription_plans.enabled_modules):
 * <ul>
 *   <li>{@code HR} — Employee Management, org structure, documents, assets</li>
 *   <li>{@code PAYROLL} — Payroll processing</li>
 *   <li>{@code RECRUITMENT} — Recruitment and applicant tracking</li>
 *   <li>{@code PERFORMANCE} — Performance management</li>
 *   <li>{@code ATTENDANCE} — Attendance and leave management</li>
 *   <li>{@code LEARNING} — Learning management</li>
 *   <li>{@code ANALYTICS} — Advanced analytics and reports</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnforceModule {
    /** Canonical module key (case-insensitive). */
    String value();
}
