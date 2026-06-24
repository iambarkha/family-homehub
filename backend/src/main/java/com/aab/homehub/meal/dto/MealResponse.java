package com.aab.homehub.meal.dto;

import com.aab.homehub.meal.CuisineType;
import com.aab.homehub.meal.MealSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MealResponse(
        UUID id,
        LocalDate date,
        MealSlot slot,
        String mealName,
        String description,
        CuisineType cuisineType,
        Integer estimatedCalories,
        Integer estimatedProteinGrams,
        LocalDateTime createdAt
) {}