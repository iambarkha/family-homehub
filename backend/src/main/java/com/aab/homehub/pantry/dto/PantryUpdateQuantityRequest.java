package com.aab.homehub.pantry.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PantryUpdateQuantityRequest(
        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity cannot be negative")
        Double currentQuantity,

        boolean restocked   // true = restocking, false = consuming
) {}