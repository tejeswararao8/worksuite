package com.platform.hr.platform.employees;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleChangeRequest {

    @NotBlank
    private String roleName;
}
