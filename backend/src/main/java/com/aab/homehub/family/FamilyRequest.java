package com.aab.homehub.family;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FamilyRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "creator user id is required")
        String creatorUserId
) {
}

