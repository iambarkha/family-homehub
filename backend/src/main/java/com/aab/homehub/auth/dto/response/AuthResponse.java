package com.aab.homehub.auth.dto.response;



public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String token, long expiresIn, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}



