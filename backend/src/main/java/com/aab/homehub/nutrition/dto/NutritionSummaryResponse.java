package com.aab.homehub.nutrition.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record NutritionSummaryResponse(

        // weekly averages
        Double avgDailyCalories,
        Double avgDailyProteinGrams,
        Double avgDailyCarbsGrams,
        Double avgDailyFatGrams,

        // threshold flags
        boolean belowCalorieThreshold,   // true when avg < 1800
        boolean belowProteinThreshold,   // true when avg < 50g

        // thresholds for frontend to display
        int minDailyCalories,
        int minDailyProteinGrams,

        // individual logs for the week
        List<NutritionLogResponse> logs
) {
    // inner record for each daily log entry
    public record NutritionLogResponse(
            UUID id,
            LocalDate logDate,
            Integer totalCalories,
            Integer totalProteinGrams,
            Integer totalCarbsGrams,
            Integer totalFatGrams,
            LocalDateTime createdAt
    ) {}
}