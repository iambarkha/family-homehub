package com.aab.homehub.nutrition;

import com.aab.homehub.nutrition.dto.NutritionLogRequest;
import com.aab.homehub.nutrition.dto.NutritionSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;

    @PostMapping
    public ResponseEntity<NutritionSummaryResponse.NutritionLogResponse> logDayNutrition(@Valid @RequestBody NutritionLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nutritionService.logDayNutrition(request));
    }

    // HISTORY — defaults to last 30 days
    @GetMapping("/history")
    public ResponseEntity<List<NutritionSummaryResponse.NutritionLogResponse>> getNutritionHistory(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(nutritionService.getNutritionHistory(days));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNutrition(@PathVariable UUID id) {
        nutritionService.deleteNutrition(id);
        return ResponseEntity.noContent().build();
    }

    // WEEKLY SUMMARY with threshold flags
    @GetMapping("/summary")
    public ResponseEntity<NutritionSummaryResponse> getWeeklySummary() {
        return ResponseEntity.ok(nutritionService.getWeeklySummary());
    }

}
