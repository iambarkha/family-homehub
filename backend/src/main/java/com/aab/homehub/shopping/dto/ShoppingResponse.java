package com.aab.homehub.shopping.dto;

import com.aab.homehub.shopping.ShoppingCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShoppingResponse(
        UUID id,
        String name,
        String quantity,
        ShoppingCategory category,
        boolean purchased,
        boolean aiSuggested,
        String addedByName,
        LocalDateTime createdAt
) {}
