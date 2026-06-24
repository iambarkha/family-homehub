package com.aab.homehub.nutrition;

import com.aab.homehub.family.FamilyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NutritionScheduler {

    private final FamilyRepository familyRepo;
    private final NutritionRepository nutritionRepo;

    @Value("${app.nutrition.min-daily-calories}")
    private int minDailyCalories;

    @Value("${app.nutrition.min-daily-protein-grams}")
    private int minDailyProteinGrams;

    // runs every Monday at 8am
    @Scheduled(cron = "0 0 8 * * MON")
    public void checkWeeklyNutrition() {
        log.info("Running weekly nutrition check");

        familyRepo.findAll().forEach(family -> {
            try {
                List<NutritionLog> lastWeek = nutritionRepo
                        .findByFamilyGroupIdAndLogDateAfter(
                                family.getId(), LocalDate.now().minusDays(7));

                if (lastWeek.isEmpty()) {
                    log.info("No nutrition logs found for family: {}",
                            family.getName());
                    return;
                }

                double avgCalories = lastWeek.stream()
                        .mapToInt(NutritionLog::getTotalCalories)
                        .average().orElse(0);

                double avgProtein = lastWeek.stream()
                        .mapToInt(NutritionLog::getTotalProteinGrams)
                        .average().orElse(0);

                boolean lowCalories = avgCalories < minDailyCalories;
                boolean lowProtein  = avgProtein  < minDailyProteinGrams;

                if (lowCalories || lowProtein) {
                    log.warn("Nutrition alert for family: {}", family.getName());

                    if (lowCalories) {
                        log.warn("  Low calories — avg: {:.0f} kcal/day " +
                                "(min: {} kcal)", avgCalories, minDailyCalories);
                    }
                    if (lowProtein) {
                        log.warn("  Low protein — avg: {:.0f} g/day " +
                                "(min: {}g)", avgProtein, minDailyProteinGrams);
                    }
                    // Kafka phase: publish nutrition.alert event here
                } else {
                    log.info("Nutrition OK for family {} — " +
                                    "avg {} kcal, {}g protein",
                            family.getName(),
                            Math.round(avgCalories),
                            Math.round(avgProtein));
                }

            } catch (Exception e) {
                log.error("Nutrition check failed for family {}: {}",
                        family.getName(), e.getMessage());
            }
        });
    }
}
