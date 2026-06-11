package com.aab.homehub.pantry;

import com.aab.homehub.family.FamilyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PantryScheduler {

    private final FamilyRepository familyRepo;
    private final PantryService pantryService;

    // runs every day at 7am
    @Scheduled(cron = "0 0 7 * * *")
    public void checkPantryLevels() {
        log.info("Running daily pantry depletion check");

        familyRepo.findAll().forEach(family -> {
            try {
                List<PantryItem> lowItems = pantryService
                        .getLowStockItemsForScheduler(family.getId());

                if (!lowItems.isEmpty()) {
                    log.warn("Low stock alert for family {}: {} items below threshold",
                            family.getName(), lowItems.size());

                    lowItems.forEach(item ->
                            log.warn("  LOW: {} — {}/{} {}",
                                    item.getName(),
                                    item.getCurrentQuantity(),
                                    item.getThresholdQuantity(),
                                    item.getUnit())
                    );
                    // Kafka phase: publish pantry.low event here
                }
            } catch (Exception e) {
                log.error("Pantry check failed for family {}: {}",
                        family.getName(), e.getMessage());
            }
        });
    }
}
