package com.platform.hr.platform.tenants;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for tenant lifecycle actions (approve, reject, suspend, reactivate).
 * {@code reason} is optional for approve/reactivate, required for reject/suspend.
 */
@Getter
@Setter
@NoArgsConstructor
public class TenantActionRequest {

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}
