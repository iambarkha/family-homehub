package com.aab.homehub.auth.dto.response;

import com.aab.homehub.auth.entity.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Role role,
        boolean enabled
) {
}

