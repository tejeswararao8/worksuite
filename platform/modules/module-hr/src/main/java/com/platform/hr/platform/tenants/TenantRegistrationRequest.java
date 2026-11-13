package com.platform.hr.platform.tenants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for creating a new tenant registration.
 * The tenant is created in PENDING_REVIEW status — never auto-activated.
 */
@Getter
@Setter
@NoArgsConstructor
public class TenantRegistrationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 100)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase alphanumeric with hyphens only")
    private String slug;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;
}
