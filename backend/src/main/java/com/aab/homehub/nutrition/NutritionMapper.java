package com.aab.homehub.nutrition;

import com.aab.homehub.nutrition.dto.NutritionSummaryResponse;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface NutritionMapper {

        NutritionSummaryResponse.NutritionLogResponse toLogResponse(NutritionLog nutritionLog);
        NutritionSummaryResponse toSummaryResponse(NutritionLog nutritionLog);
}
