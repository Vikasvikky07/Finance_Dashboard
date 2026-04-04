package com.Zorvyn.Finance_Dashboard.dto;

import com.Zorvyn.Finance_Dashboard.model.UserStatus;
import java.util.Set;

public record UserResponse(
        Long id,
        String name,
        String username,
        String email,
        Set<String> roles,
        UserStatus status
) {
}
