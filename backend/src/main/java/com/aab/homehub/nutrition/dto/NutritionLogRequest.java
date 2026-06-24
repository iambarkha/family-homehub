package com.aab.homehub.nutrition.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record NutritionLogRequest(

        @NotNull(message = "Log date is required")
        LocalDate logDate,

        @NotNull(message = "Calories is required")
        @Positive(message = "Calories must be greater than zero")
        Integer totalCalories,

        @NotNull(message = "Protein is required")
        @PositiveOrZero(message = "Protein cannot be negative")
        Integer totalProteinGrams,

        @PositiveOrZero(message = "Carbs cannot be negative")
        Integer totalCarbsGrams,

        @PositiveOrZero(message = "Fat cannot be negative")
        Integer totalFatGrams
) {}
