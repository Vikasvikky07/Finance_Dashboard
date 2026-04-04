package com.Zorvyn.Finance_Dashboard.dto;

import com.Zorvyn.Finance_Dashboard.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record UpdateUserRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Username is required")
        String username,

        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        String email,

        @NotNull(message = "Roles are required")
        Set<@NotBlank(message = "Role name is required") String> roles,

        @NotNull(message = "Status is required")
        UserStatus status
) {
}
