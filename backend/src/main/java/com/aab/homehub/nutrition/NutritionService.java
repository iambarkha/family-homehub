package com.aab.homehub.nutrition;

import com.aab.homehub.auth.entity.User;
import com.aab.homehub.exception.ResourceNotFoundException;
import com.aab.homehub.family.FamilyGroup;
import com.aab.homehub.nutrition.dto.NutritionLogRequest;
import com.aab.homehub.nutrition.dto.NutritionSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NutritionService {

    private final NutritionRepository nutritionRepository;
    private final NutritionMapper nutritionMapper;

    @Value("${app.nutrition.min-daily-calories}")
    private int minDailyCalories;

    @Value("${app.nutrition.min-daily-protein-grams}")
    private int minDailyProteinGrams;

    // LOG a day — POST /api/v1/nutrition
    public NutritionSummaryResponse.NutritionLogResponse logDayNutrition(
            NutritionLogRequest request) {

        FamilyGroup familyGroup = getCurrentUserFamily();

        // prevent duplicate log for same date
        if (nutritionRepository.existsByFamilyGroupIdAndLogDate(
                familyGroup.getId(), request.logDate())) {
            throw new IllegalStateException(
                    "Nutrition already logged for: " + request.logDate());
        }

        NutritionLog log = NutritionLog.builder()
                .logDate(request.logDate())
                .totalCalories(request.totalCalories())
                .totalProteinGrams(request.totalProteinGrams())
                .totalCarbsGrams(request.totalCarbsGrams())
                .totalFatGrams(request.totalFatGrams())
                .familyGroup(familyGroup)
                .build();

        return nutritionMapper.toLogResponse(nutritionRepository.save(log));
    }

    // SUMMARY — GET /api/v1/nutrition/summary
    @Transactional(readOnly = true)
    public NutritionSummaryResponse getWeeklySummary() {
        FamilyGroup familyGroup = getCurrentUserFamily();

        List<NutritionLog> lastWeek = nutritionRepository
                .findByFamilyGroupIdAndLogDateBetweenOrderByLogDateDesc(
                        familyGroup.getId(),
                        LocalDate.now().minusDays(7),
                        LocalDate.now());

        double avgCalories = lastWeek.stream()
                .mapToInt(NutritionLog::getTotalCalories)
                .average().orElse(0);

        double avgProtein = lastWeek.stream()
                .mapToInt(NutritionLog::getTotalProteinGrams)
                .average().orElse(0);

        double avgCarbs = lastWeek.stream()
                .filter(l -> l.getTotalCarbsGrams() != null)
                .mapToInt(NutritionLog::getTotalCarbsGrams)
                .average().orElse(0);

        double avgFat = lastWeek.stream()
                .filter(l -> l.getTotalFatGrams() != null)
                .mapToInt(NutritionLog::getTotalFatGrams)
                .average().orElse(0);

        List<NutritionSummaryResponse.NutritionLogResponse> logs = lastWeek
                .stream().map(nutritionMapper::toLogResponse).toList();

        return new NutritionSummaryResponse(
                avgCalories,
                avgProtein,
                avgCarbs,
                avgFat,
                avgCalories < minDailyCalories,   // belowCalorieThreshold
                avgProtein  < minDailyProteinGrams, // belowProteinThreshold
                minDailyCalories,
                minDailyProteinGrams,
                logs
        );
    }

    // HISTORY — GET /api/v1/nutrition/history?days=30
    @Transactional(readOnly = true)
    public List<NutritionSummaryResponse.NutritionLogResponse> getNutritionHistory(
            int days) {
        FamilyGroup familyGroup = getCurrentUserFamily();

        return nutritionRepository
                .findByFamilyGroupIdAndLogDateAfterOrderByLogDateDesc(
                        familyGroup.getId(), LocalDate.now().minusDays(days))
                .stream()
                .map(nutritionMapper::toLogResponse)
                .toList();
    }

    // DELETE — DELETE /api/v1/nutrition/{id}
    public void deleteNutrition(UUID id) {
        if (!nutritionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Nutrition log not found: " + id);
        }
        nutritionRepository.deleteById(id);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private FamilyGroup getCurrentUserFamily() {
        FamilyGroup familyGroup = getCurrentUser().getFamilyGroup();
        if (familyGroup == null) {
            throw new IllegalStateException("User does not belong to a family");
        }
        return familyGroup;
    }
}