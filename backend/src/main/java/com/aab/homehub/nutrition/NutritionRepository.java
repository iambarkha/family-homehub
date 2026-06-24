package com.aab.homehub.nutrition;

import com.aab.homehub.pantry.PantryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface NutritionRepository extends JpaRepository<NutritionLog, UUID> {

    //List<NutritionLog> getAllLogHistoryByDate(Integer date);

    // used by getNutritionHistory — last N days
    List<NutritionLog> findByFamilyGroupIdAndLogDateAfterOrderByLogDateDesc(
            UUID familyGroupId, LocalDate after);

    // used by weekly summary
    List<NutritionLog> findByFamilyGroupIdAndLogDateBetweenOrderByLogDateDesc(
            UUID familyGroupId, LocalDate from, LocalDate to);

    // used by scheduler
    List<NutritionLog> findByFamilyGroupIdAndLogDateAfter(
            UUID familyGroupId, LocalDate after);

    // duplicate check — one log per family per day
    boolean existsByFamilyGroupIdAndLogDate(UUID familyGroupId, LocalDate logDate);
}
