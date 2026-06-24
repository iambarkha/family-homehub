package com.aab.homehub.meal.dto;

import com.aab.homehub.meal.CuisineType;
import jakarta.validation.constraints.NotBlank;

public record MealItemRequest(
        
        @NotBlank(message = "Meal name is required")
        String mealName,

        String description,
        CuisineType cuisine      // optional
) {}
