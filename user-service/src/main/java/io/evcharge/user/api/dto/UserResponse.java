package io.evcharge.user.api.dto;

import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Set<String> roles,
        boolean enabled
) {}
