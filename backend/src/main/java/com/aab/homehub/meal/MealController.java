package com.aab.homehub.meal;

import com.aab.homehub.auth.UserRepository;
import com.aab.homehub.family.FamilyRepository;
import com.aab.homehub.meal.dto.MealItemRequest;
import com.aab.homehub.meal.dto.MealRequest;
import com.aab.homehub.meal.dto.MealResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    // ADD multiple dishes to a slot
    @PostMapping
    public ResponseEntity<List<MealResponse>> createMeals(@Valid @RequestBody MealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mealService.createMeals(request));
    }

    // GET week view grouped by date → slot → dishes
    @GetMapping("/week")
    public ResponseEntity<Map<LocalDate, Map<MealSlot, List<MealResponse>>>> getWeekView(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(mealService.getWeekView(startDate));
    }

    // GET meals between two dates
    @GetMapping("/range")
    public ResponseEntity<List<MealResponse>> getMealsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(mealService.getMealsByRange(from, to));
    }

    // UPDATE single dish
    @PutMapping("/{id}")
    public ResponseEntity<MealResponse> updateMeal(
            @PathVariable UUID id,
            @Valid @RequestBody MealItemRequest request) {
        return ResponseEntity.ok(mealService.updateMeal(id, request));
    }

    // DELETE scenario 1 — single dish by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMealById(@PathVariable UUID id) {
        mealService.deleteMealById(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE scenario 2 — clear entire slot
    @DeleteMapping("/slot")
    public ResponseEntity<Void> deleteMealsBySlot(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam MealSlot slot) {
        mealService.deleteMealsBySlot(date, slot);
        return ResponseEntity.noContent().build();
    }
}