package com.aab.homehub.pantry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PantryRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Current quantity is required")
        @PositiveOrZero(message = "Quantity cannot be negative")
        Double currentQuantity,

        @NotBlank(message = "Unit is required")
        String unit,

        @NotNull(message = "Threshold is required")
        @Positive(message = "Threshold must be greater than zero")
        Double thresholdQuantity,

        boolean trackConsumption
) {}