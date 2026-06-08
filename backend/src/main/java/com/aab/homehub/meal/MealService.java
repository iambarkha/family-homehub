package com.aab.homehub.meal;

import com.aab.homehub.auth.entity.User;
import com.aab.homehub.exception.ResourceNotFoundException;
import com.aab.homehub.family.FamilyGroup;
import com.aab.homehub.meal.dto.MealItemRequest;
import com.aab.homehub.meal.dto.MealRequest;
import com.aab.homehub.meal.dto.MealResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MealService {

    private final MealRepository mealRepo;
    private final MealMapper mealMapper;

    // ADD — multiple dishes to one slot in one call
    public List<MealResponse> createMeals(MealRequest request) {
        FamilyGroup familyGroup = getCurrentUserFamily();
        User currentUser = getCurrentUser();

        List<MealPlan> saved = request.meals().stream()
                .map(item -> mealRepo.save(
                        MealPlan.builder()
                                .date(request.date())
                                .slot(request.slot())
                                .mealName(item.mealName())
                                .description(item.description())
                                .cuisineType(item.cuisine())
                                .familyGroup(familyGroup)
                                .createdBy(currentUser)
                                .build()
                ))
                .toList();

        return saved.stream().map(mealMapper::toResponse).toList();
    }

    private User getCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if(currentUser == null) {
            throw new IllegalStateException("Current user is null");
        }
        return currentUser;
    }

    private FamilyGroup getCurrentUserFamily() {

        User currentUser = getCurrentUser();
        FamilyGroup familyGroup = currentUser.getFamilyGroup();
        if (familyGroup == null) {
            throw new IllegalStateException("User does not belong to a family");
        }
        return familyGroup;
    }

    // GET week view — grouped by date then slot
    @Transactional(readOnly = true)
    public Map<LocalDate, Map<MealSlot, List<MealResponse>>> getWeekView(LocalDate startDate) {
        FamilyGroup familyGroup = getCurrentUserFamily();
        LocalDate endDate = startDate.plusDays(6);

        List<MealPlan> meals = mealRepo
                .findByFamilyGroupIdAndDateBetweenOrderByDateAscSlotAsc(
                        familyGroup.getId(), startDate, endDate);

        // group by date → slot → list of dishes
        return meals.stream()
                .map(mealMapper::toResponse)
                .collect(Collectors.groupingBy(
                        MealResponse::date,
                        Collectors.groupingBy(MealResponse::slot)
                ));
    }

    // GET range
    @Transactional(readOnly = true)
    public List<MealResponse> getMealsByRange(LocalDate from, LocalDate to) {
        FamilyGroup familyGroup = getCurrentUserFamily();
        return mealRepo.findByFamilyGroupIdAndDateBetween(
                        familyGroup.getId(), from, to)
                .stream().map(mealMapper::toResponse).toList();
    }

    // UPDATE — single dish by id
    public MealResponse updateMeal(UUID id, MealItemRequest request) {
        MealPlan meal = mealRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found: " + id));

        meal.setMealName(request.mealName());
        meal.setDescription(request.description());
        meal.setCuisineType(request.cuisine());

        return mealMapper.toResponse(mealRepo.save(meal));
    }

    // DELETE - single dish by id
    public void deleteMealById(UUID id) {
        if (!mealRepo.existsById(id)) {
            throw new ResourceNotFoundException("Meal not found: " + id);
        }
        mealRepo.deleteById(id);
    }

    // DELETE - clear entire slot
    public void deleteMealsBySlot(LocalDate date, MealSlot slot) {
        FamilyGroup familyGroup = getCurrentUserFamily();
        mealRepo.deleteByFamilyGroupIdAndDateAndSlot(
                familyGroup.getId(), date, slot);
    }
}