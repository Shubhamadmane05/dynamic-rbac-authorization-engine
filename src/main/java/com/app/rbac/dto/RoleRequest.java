package com.app.rbac.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank(message = "Role name must not be blank")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "Role name must be UPPER_SNAKE_CASE, e.g. MANAGER")
    private String name;
}
