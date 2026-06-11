package com.aab.homehub.pantry.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PantryResponse(
        UUID id,
        String name,
        Double currentQuantity,
        String unit,
        Double thresholdQuantity,
        Double averageWeeklyUsage,
        LocalDate lastRestockedAt,
        boolean trackConsumption,
        boolean lowStock,          // true when currentQuantity < thresholdQuantity
        LocalDateTime createdAt
) {}