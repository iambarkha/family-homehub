package com.aab.homehub.meal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MealRepository extends JpaRepository<MealPlan, UUID> {

    // get all meals for a week view
    List<MealPlan> findByFamilyGroupIdAndDateBetweenOrderByDateAscSlotAsc(
            UUID familyGroupId, LocalDate from, LocalDate to);

    // get meals for a specific slot — used by week view grouping
    List<MealPlan> findByFamilyGroupIdAndDateAndSlot(
            UUID familyGroupId, LocalDate date, MealSlot slot);

    // delete entire slot - delete
    void deleteByFamilyGroupIdAndDateAndSlot(
            UUID familyGroupId, LocalDate date, MealSlot slot);

    // get meals after a date — used by AI (4 week history)
    List<MealPlan> findByFamilyGroupIdAndDateAfter(
            UUID familyGroupId, LocalDate after);

    // get meals in range — used by nutrition + AI shopping suggestion
    List<MealPlan> findByFamilyGroupIdAndDateBetween(
            UUID familyGroupId, LocalDate from, LocalDate to);
}
