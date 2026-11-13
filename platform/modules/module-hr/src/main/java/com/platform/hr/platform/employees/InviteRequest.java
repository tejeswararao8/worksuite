package com.platform.hr.platform.employees;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for inviting a new platform employee.
 */
@Getter
@Setter
@NoArgsConstructor
public class InviteRequest {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    private String roleName;
}
