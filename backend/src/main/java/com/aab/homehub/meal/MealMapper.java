package com.aab.homehub.meal;

import com.aab.homehub.meal.dto.MealResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MealMapper {

    MealResponse toResponse(MealPlan mealPlan);

    List<MealResponse> toResponseList(List<MealPlan> mealPlans);
}
