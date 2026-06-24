package com.aab.homehub.meal.dto;

import com.aab.homehub.meal.MealSlot;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record MealRequest(
        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Slot is required")
        MealSlot slot,

        @NotEmpty(message = "At least one meal is required")
        List<MealItemRequest> meals
)
{}
