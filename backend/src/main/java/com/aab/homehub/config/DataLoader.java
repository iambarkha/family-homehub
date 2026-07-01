package com.aab.homehub.config;

import com.aab.homehub.auth.UserRepository;
import com.aab.homehub.auth.entity.Role;
import com.aab.homehub.auth.entity.User;
import com.aab.homehub.family.FamilyGroup;
import com.aab.homehub.family.FamilyRepository;
import com.aab.homehub.meal.CuisineType;
import com.aab.homehub.meal.MealPlan;
import com.aab.homehub.meal.MealRepository;
import com.aab.homehub.meal.MealSlot;
import com.aab.homehub.nutrition.NutritionLog;
import com.aab.homehub.nutrition.NutritionRepository;
import com.aab.homehub.pantry.PantryItem;
import com.aab.homehub.pantry.PantryRepository;
import com.aab.homehub.reminder.RecurrencePattern;
import com.aab.homehub.reminder.ReminderRepository;
import com.aab.homehub.shopping.ShoppingCategory;
import com.aab.homehub.shopping.ShoppingItem;
import com.aab.homehub.shopping.ShoppingRepository;
import com.aab.homehub.todo.Priority;
import com.aab.homehub.todo.TodoItem;
import com.aab.homehub.reminder.Reminder;
import com.aab.homehub.todo.TodoRepository;
import com.aab.homehub.todo.TodoScope;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final TodoRepository todoRepository;
    private final MealRepository mealRepository;
    private final ShoppingRepository shoppingRepository;
    private final ReminderRepository reminderRepository;
    private final PantryRepository pantryRepository;
    private final NutritionRepository nutritionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Idempotent seed data for local development.
        FamilyGroup demoFamily = familyRepository.findAll().stream()
                .filter(group -> "Demo1 Family".equalsIgnoreCase(group.getName()))
                .findFirst()
                .orElseGet(() -> familyRepository.save(FamilyGroup.builder().name("Demo1 Family").build()));

        FamilyGroup secondFamily = familyRepository.findAll().stream()
                .filter(group -> "Demo2 Family".equalsIgnoreCase(group.getName()))
                .findFirst()
                .orElseGet(() -> familyRepository.save(FamilyGroup.builder().name("Demo2 Family").build()));

        upsertUser("admin@homehub.local", "Admin", "User", Role.ADMIN, demoFamily);
        upsertUser("john.doe@example.com", "John", "Doe", Role.BASIC, demoFamily);
        upsertUser("emma.smith@example.com", "Emma", "Smith", Role.BASIC, secondFamily);

        User admin = userRepository.findByEmail("admin@homehub.local")
                .orElseThrow(() -> new IllegalStateException("Seed admin user missing"));
        User emma = userRepository.findByEmail("emma.smith@example.com")
                .orElseThrow(() -> new IllegalStateException("Seed emma user missing"));

        upsertTodo(admin, demoFamily);
        upsertMeal(admin, demoFamily, LocalDate.now().plusDays(1), MealSlot.DINNER,
                "Veg Pasta", "Whole wheat pasta with vegetables", CuisineType.ITALIAN);
        upsertMeal(admin, demoFamily, LocalDate.now().plusDays(1), MealSlot.LUNCH,
                "Rice Bowl", "Mixed veggie rice bowl", CuisineType.ASIAN);
        upsertMeal(emma, secondFamily, LocalDate.now().plusDays(2), MealSlot.BREAKFAST,
                "Oats Bowl", "Oats with fruits and nuts", CuisineType.OTHER);

        // Shopping seed data
        upsertShoppingItem("Milk", "2 liters", ShoppingCategory.GROCERIES, admin, demoFamily);
        upsertShoppingItem("Bread", "1 loaf", ShoppingCategory.GROCERIES, admin, demoFamily);
        upsertShoppingItem("Eggs", "12 pack", ShoppingCategory.GROCERIES, admin, demoFamily);
        upsertShoppingItem("Dish Soap", "1 bottle", ShoppingCategory.HOUSEHOLD, admin, demoFamily);
        upsertShoppingItem("Shampoo", "1 bottle", ShoppingCategory.COSMETICS, admin, demoFamily);
        upsertShoppingItem("Rice", "5 kg", ShoppingCategory.GROCERIES, emma, secondFamily);
        upsertShoppingItem("Laundry Detergent", "1 pack", ShoppingCategory.HOUSEHOLD, emma, secondFamily);
        upsertShoppingItem("T-Shirt", "2 pcs", ShoppingCategory.CLOTHING, emma, secondFamily);

        // Reminder seed data
        upsertReminder(admin, demoFamily, "Weekly Grocery Run",
                "Time to go grocery shopping", LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
                RecurrencePattern.WEEKLY);
        upsertReminder(admin, demoFamily, "Team Meeting", "Family sync meeting",
                LocalDateTime.now().plusDays(1).withHour(14).withMinute(0), RecurrencePattern.WEEKLY);
        upsertReminder(emma, secondFamily, "Doctor Appointment",
                "Annual health checkup", LocalDateTime.now().plusDays(5).withHour(9).withMinute(0),
                RecurrencePattern.NONE);

        // Pantry seed data
        upsertPantryItem("Milk", 2.0, "litres", 0.5, true, demoFamily);
        upsertPantryItem("Olive Oil", 0.75, "litres", 0.25, true, demoFamily);
        upsertPantryItem("Rice", 3.0, "kg", 1.0, true, demoFamily);
        upsertPantryItem("Sugar", 2.5, "kg", 0.5, true, demoFamily);
        upsertPantryItem("Flour", 1.5, "kg", 0.5, false, demoFamily);
        upsertPantryItem("Butter", 0.5, "kg", 0.2, true, secondFamily);
        upsertPantryItem("Honey", 0.5, "litres", 0.2, false, secondFamily);
        upsertPantryItem("Pasta", 2.0, "kg", 0.5, false, secondFamily);

        // Nutrition seed data (last few days)
        upsertNutritionLog(demoFamily, LocalDate.now().minusDays(1), 1980, 86, 210, 62);
        upsertNutritionLog(demoFamily, LocalDate.now().minusDays(2), 1750, 72, 190, 58);
        upsertNutritionLog(demoFamily, LocalDate.now().minusDays(3), 2210, 94, 245, 70);
        upsertNutritionLog(secondFamily, LocalDate.now().minusDays(1), 1840, 68, 205, 60);
        upsertNutritionLog(secondFamily, LocalDate.now().minusDays(2), 1690, 56, 180, 54);
    }

    private void upsertUser(String email, String firstName, String lastName, Role role, FamilyGroup familyGroup) {
        boolean isNewUser = userRepository.findByEmail(email).isEmpty();
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> User.builder().email(email).build());

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setFamilyGroup(familyGroup);
        if (isNewUser) {
            user.setPassword(passwordEncoder.encode("Password@123"));
        }

        userRepository.save(user);
    }

    private void upsertTodo(User assignedTo, FamilyGroup familyGroup) {
        String title = "Welcome Todo";
        boolean exists = todoRepository.findAll().stream().anyMatch(todo ->
                title.equalsIgnoreCase(todo.getTitle())
                        && todo.getFamilyGroup() != null
                        && familyGroup.getId().equals(todo.getFamilyGroup().getId()));

        if (exists) {
            return;
        }

        TodoItem todoItem = TodoItem.builder()
                .title(title)
                .description("First todo for demo family")
                .priority(Priority.HIGH)
                .scope(TodoScope.WEEKLY)
                .dueDate(LocalDate.now().plusDays(7))
                .assignedTo(assignedTo)
                .familyGroup(familyGroup)
                .build();

        todoRepository.save(todoItem);
    }

    private void upsertMeal(User createdBy, FamilyGroup familyGroup, LocalDate date, MealSlot slot,
                            String mealName, String description, CuisineType cuisineType) {
        boolean exists = mealRepository.findByFamilyGroupIdAndDateAndSlot(familyGroup.getId(), date, slot)
                .stream()
                .anyMatch(meal -> mealName.equalsIgnoreCase(meal.getMealName()));

        if (exists) {
            return;
        }

        MealPlan mealPlan = MealPlan.builder()
                .date(date)
                .slot(slot)
                .mealName(mealName)
                .description(description)
                .cuisineType(cuisineType)
                .familyGroup(familyGroup)
                .createdBy(createdBy)
                .build();

        mealRepository.save(mealPlan);
    }

    private void upsertShoppingItem(String name, String quantity, ShoppingCategory category,
                                    User addedBy, FamilyGroup familyGroup) {
        boolean exists = shoppingRepository
                .findByFamilyGroupIdOrderByCategoryAsc(familyGroup.getId())
                .stream()
                .anyMatch(item -> name.equalsIgnoreCase(item.getName())
                        && item.getCategory() == category);

        if (exists) {
            return;
        }

        ShoppingItem item = ShoppingItem.builder()
                .name(name)
                .quantity(quantity)
                .category(category)
                .addedBy(addedBy)
                .familyGroup(familyGroup)
                .build();

        shoppingRepository.save(item);
    }

    private void upsertReminder(User assignedTo, FamilyGroup familyGroup, String title,
                                String message, LocalDateTime triggerAt, RecurrencePattern recurrencePattern) {
        boolean exists = reminderRepository.findAll().stream()
                .anyMatch(reminder -> title.equalsIgnoreCase(reminder.getTitle())
                        && familyGroup.getId().equals(reminder.getFamilyGroup().getId()));

        if (exists) {
            return;
        }

        Reminder reminder = Reminder.builder()
                .title(title)
                .message(message)
                .triggerAt(triggerAt)
                .recurrencePattern(recurrencePattern)
                .assignedTo(assignedTo)
                .familyGroup(familyGroup)
                .build();

        reminderRepository.save(reminder);
    }

    private void upsertPantryItem(String name, Double currentQuantity, String unit,
                                  Double thresholdQuantity, boolean trackConsumption, FamilyGroup familyGroup) {
        boolean exists = pantryRepository.findByFamilyGroupId(familyGroup.getId())
                .stream()
                .anyMatch(item -> name.equalsIgnoreCase(item.getName()));

        if (exists) {
            return;
        }

        PantryItem pantryItem = PantryItem.builder()
                .name(name)
                .currentQuantity(currentQuantity)
                .unit(unit)
                .thresholdQuantity(thresholdQuantity)
                .trackConsumption(trackConsumption)
                .familyGroup(familyGroup)
                .build();

        pantryRepository.save(pantryItem);
    }

    private void upsertNutritionLog(FamilyGroup familyGroup, LocalDate logDate, Integer calories,
                                    Integer proteinGrams, Integer carbsGrams, Integer fatGrams) {
        boolean exists = nutritionRepository.existsByFamilyGroupIdAndLogDate(familyGroup.getId(), logDate);
        if (exists) {
            return;
        }

        NutritionLog log = NutritionLog.builder()
                .familyGroup(familyGroup)
                .logDate(logDate)
                .totalCalories(calories)
                .totalProteinGrams(proteinGrams)
                .totalCarbsGrams(carbsGrams)
                .totalFatGrams(fatGrams)
                .build();

        nutritionRepository.save(log);
    }
}

