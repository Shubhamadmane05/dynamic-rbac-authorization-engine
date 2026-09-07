package com.app.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {

    @NotBlank(message = "Permission name must not be blank")
    @Pattern(
            regexp = "^[A-Z][A-Z0-9]*(_[A-Z0-9]+)+$",
            message = "Permission name must follow {RESOURCE}_{ACTION}, e.g. REPORT_VIEW"
    )
    private String name;
}

