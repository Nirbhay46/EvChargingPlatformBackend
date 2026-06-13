package io.evcharge.user.api.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {
    public static AuthResponse of(String token, long exp, UserResponse u) {
        return new AuthResponse(token, "Bearer", exp, u);
    }
}
