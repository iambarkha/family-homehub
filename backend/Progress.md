# Household Manager — Complete Build Guide
# ==========================================
# All decisions:
#  - 1 Spring Boot service (modular monolith)
#  - H2 for local dev · PostgreSQL on Railway prod
#  - React (Vite + Tailwind) on Vercel
#  - Feature-first package structure
#  - Kafka: Docker Compose locally · Upstash in prod
#  - Spring AI: all features listed below
#
# MODULES:
#   auth · family · meal · shopping · todo · reminder · pantry · nutrition · ai · notification · config
#
# KAFKA TOPICS:
#   meal.planned · reminder.triggered · ai.requested · todo.completed
#   nutrition.alert · pantry.low
#
# AI FEATURES:
#   1. Weekly meal suggestions (history + cuisine preference)
#   2. Protein & calorie calculation per meal plan
#   3. Nutrition alert when intake drops below threshold
#   4. Auto-generate shopping list from meal plan
#   5. Pantry depletion prediction ("oil running low")
#   6. Oil & sugar consumption monitoring
#   7. Household chat assistant


# ══════════════════════════════════════════
# SECTION 1 — PROJECT STRUCTURE
# ══════════════════════════════════════════

# family-home/
# ├── backend/          ← Spring Boot (1 JAR · 1 Railway deploy)
# │   ├── docker-compose.yml        local Kafka
# │   ├── Dockerfile
# │   └── src/main/java/com/family/household/
# │       ├── auth/
# │       ├── family/
# │       ├── meal/
# │       ├── shopping/
# │       ├── todo/
# │       ├── reminder/
# │       ├── pantry/               ← NEW: tracks stock levels
# │       ├── nutrition/            ← NEW: calories + protein per plan
# │       ├── ai/
# │       ├── notification/
# │       └── config/
# └── frontend/         ← React + Vite + Tailwind (Vercel)


# ══════════════════════════════════════════
# SECTION 2 — PACKAGE STRUCTURE (feature-first)
# ══════════════════════════════════════════

# com.family.household/
# │
# ├── auth/
# │   ├── AuthController.java        /api/auth/register · /api/auth/login
# │   ├── AuthService.java
# │   ├── User.java                  @Entity
# │   ├── UserRepository.java
# │   ├── Role.java                  enum ADMIN, MEMBER
# │   └── dto/  RegisterRequest · LoginRequest · AuthResponse
# │
# ├── family/
# │   ├── FamilyGroup.java           @Entity
# │   ├── FamilyRepository.java
# │   └── FamilyService.java
# │
# ├── meal/
# │   ├── MealController.java        /api/meals
# │   ├── MealService.java
# │   ├── MealPlan.java              @Entity
# │   ├── MealSlot.java              enum BREAKFAST, LUNCH, DINNER, SNACK
# │   ├── CuisineType.java           enum INDIAN, ITALIAN, MEXICAN, ASIAN, MEDITERRANEAN, OTHER
# │   ├── MealRepository.java
# │   ├── MealEventProducer.java     → meal.planned
# │   └── dto/  MealPlanRequest · MealPlanResponse
# │
# ├── shopping/
# │   ├── ShoppingController.java    /api/shopping
# │   ├── ShoppingService.java
# │   ├── ShoppingItem.java          @Entity
# │   ├── ShoppingCategory.java      enum GROCERIES, CLOTHING, COSMETICS, HOUSEHOLD, OTHER
# │   ├── ShoppingRepository.java
# │   ├── MealPlannedConsumer.java   consumes meal.planned → auto-add ingredients
# │   ├── PantryLowConsumer.java     consumes pantry.low → auto-add to shopping
# │   └── dto/  ShoppingRequest · ShoppingResponse
# │
# ├── todo/
# │   ├── TodoController.java        /api/todos
# │   ├── TodoService.java
# │   ├── TodoItem.java              @Entity
# │   ├── Priority.java              enum HIGH, MEDIUM, LOW
# │   ├── TodoScope.java             enum WEEKLY, MONTHLY, LONGTERM
# │   ├── TodoRepository.java
# │   ├── TodoEventProducer.java     → todo.completed
# │   └── dto/  TodoRequest · TodoResponse
# │
# ├── reminder/
# │   ├── ReminderController.java    /api/reminders
# │   ├── ReminderService.java       @Scheduled poller
# │   ├── Reminder.java              @Entity
# │   ├── ReminderRepository.java
# │   ├── ReminderEventProducer.java → reminder.triggered
# │   └── dto/  ReminderRequest · ReminderResponse
# │
# ├── pantry/                        ← tracks what you have at home
# │   ├── PantryController.java      /api/pantry
# │   ├── PantryService.java
# │   ├── PantryItem.java            @Entity  (name, quantity, unit, lastRestockedAt)
# │   ├── PantryRepository.java
# │   ├── PantryEventProducer.java   → pantry.low (when quantity drops below threshold)
# │   ├── PantryScheduler.java       @Scheduled — daily depletion check
# │   └── dto/  PantryItemRequest · PantryItemResponse
# │
# ├── nutrition/                     ← calories + protein tracking
# │   ├── NutritionController.java   /api/nutrition
# │   ├── NutritionService.java
# │   ├── NutritionLog.java          @Entity  (date, familyGroupId, totalCalories, totalProtein)
# │   ├── NutritionRepository.java
# │   ├── NutritionScheduler.java    @Scheduled — weekly avg check → alert if below threshold
# │   ├── NutritionEventProducer.java → nutrition.alert
# │   └── dto/  NutritionSummary · NutritionAlert
# │
# ├── ai/
# │   ├── AiController.java          /api/ai/*
# │   ├── AiService.java             all AI features (Spring AI ChatClient)
# │   ├── AiRequestConsumer.java     consumes ai.requested (async AI calls)
# │   ├── TodoCompletedConsumer.java consumes todo.completed → weekly summary
# │   └── dto/  AiRequestedEvent · ChatRequest · ChatResponse
# │
# ├── notification/
# │   ├── ReminderTriggeredConsumer.java  consumes reminder.triggered → email
# │   ├── NutritionAlertConsumer.java     consumes nutrition.alert → email/push
# │   └── PantryLowNotifier.java          consumes pantry.low → email/push
# │
# └── config/
#     ├── SecurityConfig.java
#     ├── JwtTokenProvider.java
#     ├── JwtAuthFilter.java
#     ├── OpenAiConfig.java
#     ├── KafkaTopicConfig.java
#     ├── BaseEntity.java
#     └── GlobalExceptionHandler.java


# ══════════════════════════════════════════
# SECTION 3 — pom.xml DEPENDENCIES
# ══════════════════════════════════════════

```xml
<dependencies>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-mail</artifactId></dependency>

  <!-- H2 for local dev -->
  <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>
  <!-- PostgreSQL for prod -->
  <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
  <!-- Flyway -->
  <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>

  <!-- JWT -->
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.3</version></dependency>
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.3</version><scope>runtime</scope></dependency>
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.3</version><scope>runtime</scope></dependency>

  <!-- Spring AI -->
  <dependency><groupId>org.springframework.ai</groupId><artifactId>spring-ai-openai-spring-boot-starter</artifactId></dependency>

  <!-- Kafka -->
  <dependency><groupId>org.springframework.kafka</groupId><artifactId>spring-kafka</artifactId></dependency>

  <!-- Utilities -->
  <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
</dependencies>
```


# ══════════════════════════════════════════
# SECTION 4 — KEY ENTITIES
# ══════════════════════════════════════════

## MealPlan.java
```java
@Entity @Table(name = "meal_plans")
@Data @NoArgsConstructor @Builder
public class MealPlan extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private LocalDate date;
    @Enumerated(EnumType.STRING) private MealSlot slot;   // BREAKFAST,LUNCH,DINNER,SNACK
    @Column(nullable = false) private String mealName;
    private String description;
    @Enumerated(EnumType.STRING) private CuisineType cuisine; // INDIAN,ITALIAN,MEXICAN...
    private Integer estimatedCalories;   // filled by AI after plan saved
    private Integer estimatedProteinGrams;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id") private FamilyGroup familyGroup;
}
```

## TodoItem.java
```java
@Entity @Table(name = "todo_items")
@Data @NoArgsConstructor @Builder
public class TodoItem extends BaseEntity {
        @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
        @Column(nullable = false) private String title;
        private String description;
        @Enumerated(EnumType.STRING) private Priority priority;  // HIGH, MEDIUM, LOW
        @Enumerated(EnumType.STRING) private TodoScope scope;    // WEEKLY, MONTHLY, LONGTERM
        private LocalDate dueDate;
        private boolean completed;
        @ManyToOne(fetch = FetchType.LAZY) private User assignedTo;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "family_group_id") private FamilyGroup familyGroup;
}
```

## ShoppingItem.java
```java
@Entity @Table(name = "shopping_items")
@Data @NoArgsConstructor @Builder
public class ShoppingItem extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String name;
    private String quantity;
    @Enumerated(EnumType.STRING)
    private ShoppingCategory category; // GROCERIES, CLOTHING, COSMETICS, HOUSEHOLD, OTHER
    private boolean purchased;
    private boolean aiSuggested;       // true when added by AI, false when added manually
    private String addedByUserId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id") private FamilyGroup familyGroup;
}
```

## PantryItem.java  (NEW)
```java
@Entity @Table(name = "pantry_items")
@Data @NoArgsConstructor @Builder
public class PantryItem extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String name;        // "Sunflower Oil", "Sugar"
    private Double currentQuantity;                       // 0.5
    private String unit;                                  // "litres", "kg", "pcs"
    private Double thresholdQuantity;                     // alert when below this
    private Double averageWeeklyUsage;                    // AI calculates this over time
    private LocalDate lastRestockedAt;
    private boolean trackConsumption;                     // true for oil, sugar etc.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id") private FamilyGroup familyGroup;
}
```

## NutritionLog.java  (NEW)
```java
@Entity @Table(name = "nutrition_logs")
@Data @NoArgsConstructor @Builder
public class NutritionLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private LocalDate logDate;
    private Integer totalCalories;
    private Integer totalProteinGrams;
    private Integer totalCarbsGrams;
    private Integer totalFatGrams;
    private boolean alertSent;           // prevent duplicate alerts
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id") private FamilyGroup familyGroup;
}
```

## BaseEntity.java
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class BaseEntity {
    @CreatedDate @Column(updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;
}
```


# ══════════════════════════════════════════
# SECTION 5 — AI FEATURES (AiService.java)
# All features use Spring AI ChatClient
# ══════════════════════════════════════════

```java
@Service @RequiredArgsConstructor @Slf4j
public class AiService {

    private final ChatClient chatClient;
    private final MealRepository mealRepo;
    private final TodoRepository todoRepo;
    private final ShoppingRepository shoppingRepo;
    private final PantryRepository pantryRepo;
    private final NutritionRepository nutritionRepo;

    // ─────────────────────────────────────────────
    // AI FEATURE 1: Weekly meal suggestions
    // Observes past meals + cuisineType preference
    // ─────────────────────────────────────────────
    public String suggestWeeklyMeals(String familyGroupId, String cuisinePreference) {
        // fetch last 4 weeks of meals as context
        List<MealPlan> history = mealRepo.findByFamilyGroupIdAndDateAfter(
            familyGroupId, LocalDate.now().minusWeeks(4));

        String mealHistory = history.stream()
            .map(m -> "%s | %s | %s".formatted(m.getDate(), m.getSlot(), m.getMealName()))
            .collect(Collectors.joining("\n"));

        String prompt = """
            You are a family meal planner. Here are the meals this family had in the last 4 weeks:
            %s

            Cuisine preference for this week: %s

            Suggest a 7-day meal plan (breakfast, lunch, dinner) that:
            1. Avoids repeating meals from the last 2 weeks
            2. Matches the cuisineType preference where possible
            3. Is balanced and nutritious
            4. Includes estimated calories and protein per meal

            Respond ONLY with valid JSON:
            {
              "days": [
                {
                  "date": "YYYY-MM-DD",
                  "breakfast": { "name": "...", "calories": 0, "proteinGrams": 0 },
                  "lunch":     { "name": "...", "calories": 0, "proteinGrams": 0 },
                  "dinner":    { "name": "...", "calories": 0, "proteinGrams": 0 }
                }
              ]
            }
            """.formatted(mealHistory, cuisinePreference);

        return chatClient.prompt().user(prompt).call().content();
    }

    // ─────────────────────────────────────────────
    // AI FEATURE 2: Calculate nutrition for a meal plan
    // Called after user saves their meal plan for the week
    // ─────────────────────────────────────────────
    public String calculateNutrition(List<MealPlan> meals) {
        String mealList = meals.stream()
            .map(m -> "- %s: %s".formatted(m.getSlot(), m.getMealName()))
            .collect(Collectors.joining("\n"));

        String prompt = """
            Estimate the nutritional content for these meals (assume average adult serving sizes):
            %s

            Respond ONLY with valid JSON:
            {
              "meals": [
                {
                  "mealName": "...",
                  "slot": "BREAKFAST|LUNCH|DINNER|SNACK",
                  "calories": 0,
                  "proteinGrams": 0,
                  "carbsGrams": 0,
                  "fatGrams": 0
                }
              ],
              "dailyTotal": {
                "calories": 0, "proteinGrams": 0, "carbsGrams": 0, "fatGrams": 0
              }
            }
            """.formatted(mealList);

        return chatClient.prompt().user(prompt).call().content();
    }

    // ─────────────────────────────────────────────
    // AI FEATURE 3: Check if nutrition is below threshold
    // Called by NutritionScheduler weekly
    // Returns null if all is fine, alert message if not
    // ─────────────────────────────────────────────
    public String checkNutritionThresholds(String familyGroupId) {
        List<NutritionLog> lastWeek = nutritionRepo.findByFamilyGroupIdAndLogDateAfter(
            familyGroupId, LocalDate.now().minusDays(7));

        if (lastWeek.isEmpty()) return null;

        double avgCalories = lastWeek.stream()
            .mapToInt(NutritionLog::getTotalCalories).average().orElse(0);
        double avgProtein = lastWeek.stream()
            .mapToInt(NutritionLog::getTotalProteinGrams).average().orElse(0);

        String prompt = """
            A family's average daily nutrition this week:
            - Calories: %.0f kcal/day
            - Protein: %.0f g/day

            Recommended minimums for an average adult: 1800 kcal/day, 50g protein/day.

            If any value is significantly below the recommended minimum, return a friendly alert.
            If everything is fine, return null.

            Respond ONLY with valid JSON:
            {
              "alert": true|false,
              "message": "..." or null,
              "lowCalories": true|false,
              "lowProtein": true|false
            }
            """.formatted(avgCalories, avgProtein);

        return chatClient.prompt().user(prompt).call().content();
    }

    // ─────────────────────────────────────────────
    // AI FEATURE 4: Generate shopping list from meal plan
    // ─────────────────────────────────────────────
    public String suggestShoppingFromMeals(String familyGroupId, List<String> mealNames) {
        // also pass current pantry so AI doesn't suggest what you already have
        List<PantryItem> pantry = pantryRepo.findByFamilyGroupId(familyGroupId);
        String pantryItems = pantry.stream()
            .map(p -> "%s: %.1f %s".formatted(p.getName(), p.getCurrentQuantity(), p.getUnit()))
            .collect(Collectors.joining(", "));

        String prompt = """
            Planned meals this week: %s

            Items already in pantry: %s

            Generate a shopping list of ingredients needed for these meals,
            excluding items already well-stocked in the pantry.

            Respond ONLY with valid JSON:
            {
              "items": [
                { "name": "...", "quantity": "...", "category": "GROCERIES|HOUSEHOLD|OTHER" }
              ]
            }
            """.formatted(String.join(", ", mealNames), pantryItems);

        return chatClient.prompt().user(prompt).call().content();
    }

    // ─────────────────────────────────────────────
    // AI FEATURE 5: Pantry depletion prediction
    // Learns usage patterns over time
    // ─────────────────────────────────────────────
    public String predictPantryDepletion(String familyGroupId) {
        List<PantryItem> trackedItems = pantryRepo
            .findByFamilyGroupIdAndTrackConsumptionTrue(familyGroupId);

        if (trackedItems.isEmpty()) return null;

        String itemList = trackedItems.stream()
            .map(p -> "- %s: %.1f %s remaining, avg weekly use: %.1f %s, last restocked: %s"
                .formatted(p.getName(), p.getCurrentQuantity(), p.getUnit(),
                    p.getAverageWeeklyUsage() != null ? p.getAverageWeeklyUsage() : 0,
                    p.getUnit(), p.getLastRestockedAt()))
            .collect(Collectors.joining("\n"));

        String prompt = """
            These are tracked pantry items with current stock and usage patterns:
            %s

            Based on current stock and average weekly usage, predict:
            1. Which items will run out within the next 7 days
            2. Suggested restock quantity for each

            Respond ONLY with valid JSON:
            {
              "depletingSoon": [
                {
                  "itemName": "...",
                  "daysRemaining": 0,
                  "suggestedRestockQuantity": "...",
                  "urgency": "HIGH|MEDIUM|LOW"
                }
              ]
            }
            """.formatted(itemList);

        return chatClient.prompt().user(prompt).call().content();
    }

    // ─────────────────────────────────────────────
    // AI FEATURE 6: Oil & sugar consumption report
    // Monthly insight on specific health-sensitive items
    // ─────────────────────────────────────────────
    public String analyzeConsumption(String familyGroupId, String itemName) {
        List<PantryItem> history = pantryRepo
            .findByFamilyGroupIdAndNameIgnoreCase(familyGroupId, itemName);

        if (history.isEmpty()) return "No data found for " + itemName;

        PantryItem item = history.get(0);
        String prompt = """
            Household consumption data for %s:
            - Current stock: %.1f %s
            - Average weekly usage: %.1f %s
            - Last restocked: %s

            Provide a brief health-aware consumption analysis:
            1. Is this usage level concerning for a typical family?
            2. What is the recommended weekly limit?
            3. Any simple tips to reduce if over the limit?

            Keep the response friendly and under 100 words.
            """.formatted(
                itemName,
                item.getCurrentQuantity(), item.getUnit(),
                item.getAverageWeeklyUsage() != null ? item.getAverageWeeklyUsage() : 0,
                item.getUnit(),
                item.getLastRestockedAt());

        return chatClient.prompt().user(prompt).call().content();
    }

    // ─────────────────────────────────────────────
    // AI FEATURE 7: General household assistant (chat)
    // Context-aware — knows your current household state
    // ─────────────────────────────────────────────
    public String chat(String familyGroupId, String userMessage) {
        long pendingTodos = todoRepo.countByFamilyGroupIdAndCompletedFalse(familyGroupId);
        long shoppingItems = shoppingRepo.countByFamilyGroupIdAndPurchasedFalse(familyGroupId);
        long pantryLow = pantryRepo.countByFamilyGroupIdAndCurrentQuantityLessThanThreshold(familyGroupId);

        String system = """
            You are a helpful household assistant. Current family status:
            - Pending todos: %d
            - Shopping items needed: %d
            - Pantry items running low: %d
            Be friendly, concise, and practical. Answer questions about meals,
            tasks, shopping, nutrition, and household management.
            """.formatted(pendingTodos, shoppingItems, pantryLow);

        return chatClient.prompt().system(system).user(userMessage).call().content();
    }

    // ─────────────────────────────────────────────
    // AI FEATURE 8: Weekly summary (triggered when all todos done)
    // ─────────────────────────────────────────────
    public String generateWeeklySummary(String familyGroupId, LocalDate weekOf) {
        long completedTodos = todoRepo.countByFamilyGroupIdAndCompletedTrue(familyGroupId);
        List<NutritionLog> nutritionThisWeek = nutritionRepo
            .findByFamilyGroupIdAndLogDateAfter(familyGroupId, weekOf);

        double avgCalories = nutritionThisWeek.stream()
            .mapToInt(NutritionLog::getTotalCalories).average().orElse(0);

        String prompt = """
            Generate a friendly weekly household summary:
            - Todos completed this week: %d
            - Average daily calories: %.0f kcal
            - Week of: %s

            Write 3-4 sentences celebrating progress and giving one tip for next week.
            Be warm and encouraging.
            """.formatted(completedTodos, avgCalories, weekOf);

        return chatClient.prompt().user(prompt).call().content();
    }
}
```


# ══════════════════════════════════════════
# SECTION 6 — SCHEDULERS
# ══════════════════════════════════════════

## NutritionScheduler.java  (weekly nutrition alert)
```java
@Component @RequiredArgsConstructor @Slf4j
public class NutritionScheduler {

    private final FamilyRepository familyRepo;
    private final AiService aiService;
    private final NutritionRepository nutritionRepo;
    private final NutritionEventProducer nutritionEventProducer;

    // runs every Monday morning
    @Scheduled(cron = "0 0 8 * * MON")
    public void checkWeeklyNutrition() {
        familyRepo.findAll().forEach(family -> {
            try {
                String result = aiService.checkNutritionThresholds(family.getId());
                if (result != null) {
                    // parse JSON and publish alert if needed
                    nutritionEventProducer.publishIfAlert(family.getId(), result);
                }
            } catch (Exception e) {
                log.error("Nutrition check failed for family {}: {}", family.getId(), e.getMessage());
            }
        });
    }
}
```

## PantryScheduler.java  (daily depletion check)
```java
@Component @RequiredArgsConstructor @Slf4j
public class PantryScheduler {

    private final FamilyRepository familyRepo;
    private final AiService aiService;
    private final PantryEventProducer pantryEventProducer;

    // runs daily at 7am
    @Scheduled(cron = "0 0 7 * * *")
    public void checkPantryLevels() {
        familyRepo.findAll().forEach(family -> {
            try {
                String result = aiService.predictPantryDepletion(family.getId());
                if (result != null) {
                    pantryEventProducer.publishDepletionAlerts(family.getId(), result);
                }
            } catch (Exception e) {
                log.error("Pantry check failed for family {}: {}", family.getId(), e.getMessage());
            }
        });
    }
}
```


# ══════════════════════════════════════════
# SECTION 7 — KAFKA SETUP
# ══════════════════════════════════════════

## docker-compose.yml  (local dev — no install)
```yaml
services:
  kafka:
    image: confluentinc/cp-kafka:7.6.0
    container_name: household-kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_LOG_RETENTION_HOURS: 24
      CLUSTER_ID: household-kafka-cluster-001
```

## KafkaTopicConfig.java
```java
@Configuration
public class KafkaTopicConfig {
    public static final String MEAL_PLANNED         = "meal.planned";
    public static final String REMINDER_TRIGGERED   = "reminder.triggered";
    public static final String AI_REQUESTED         = "ai.requested";
    public static final String TODO_COMPLETED       = "todo.completed";
    public static final String NUTRITION_ALERT      = "nutrition.alert";
    public static final String PANTRY_LOW           = "pantry.low";

    @Bean public NewTopic mealPlanned()       { return TopicBuilder.name(MEAL_PLANNED).partitions(1).replicas(1).build(); }
    @Bean public NewTopic reminderTriggered() { return TopicBuilder.name(REMINDER_TRIGGERED).partitions(1).replicas(1).build(); }
    @Bean public NewTopic aiRequested()       { return TopicBuilder.name(AI_REQUESTED).partitions(1).replicas(1).build(); }
    @Bean public NewTopic todoCompleted()     { return TopicBuilder.name(TODO_COMPLETED).partitions(1).replicas(1).build(); }
    @Bean public NewTopic nutritionAlert()    { return TopicBuilder.name(NUTRITION_ALERT).partitions(1).replicas(1).build(); }
    @Bean public NewTopic pantryLow()         { return TopicBuilder.name(PANTRY_LOW).partitions(1).replicas(1).build(); }
}
```

## Kafka topic flows
# meal.planned       → producer: MealService         consumer: MealPlannedConsumer (shopping)
# reminder.triggered → producer: ReminderService      consumer: ReminderTriggeredConsumer (notification)
# ai.requested       → producer: any service          consumer: AiRequestConsumer
# todo.completed     → producer: TodoService          consumer: TodoCompletedConsumer (ai summary)
# nutrition.alert    → producer: NutritionScheduler   consumer: NutritionAlertConsumer (notification)
# pantry.low         → producer: PantryScheduler      consumer: PantryLowConsumer (shopping + notification)


# ══════════════════════════════════════════
# SECTION 8 — APPLICATION CONFIG
# ══════════════════════════════════════════

## application.yml  (base)
```yaml
spring:
  application:
    name: household-manager
  profiles:
    active: dev
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:dummy-key}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
app:
  jwt:
    secret: ${JWT_SECRET:local-dev-secret}
    expiry-ms: 86400000
  cors:
    allowed-origins:
      - http://localhost:5173
      - https://your-app.vercel.app
  nutrition:
    min-daily-calories: 1800
    min-daily-protein-grams: 50
```

## application-dev.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:householddb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2.console.enabled: true
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: household-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.family.household.*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

## application-prod.yml
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  kafka:
    bootstrap-servers: ${KAFKA_BROKER_URL}
    consumer:
      group-id: household-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.family.household.*"
        security.protocol: SASL_SSL
        sasl.mechanism: SCRAM-SHA-256
        sasl.jaas.config: >
          org.apache.kafka.common.security.scram.ScramLoginModule required
          username="${KAFKA_USERNAME}" password="${KAFKA_PASSWORD}";
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        security.protocol: SASL_SSL
        sasl.mechanism: SCRAM-SHA-256
        sasl.jaas.config: >
          org.apache.kafka.common.security.scram.ScramLoginModule required
          username="${KAFKA_USERNAME}" password="${KAFKA_PASSWORD}";
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
logging:
  level:
    root: WARN
    com.family.household: INFO
```


# ══════════════════════════════════════════
# SECTION 9 — FLYWAY MIGRATIONS
# ══════════════════════════════════════════

# V1__create_users_family.sql
```sql
CREATE TABLE family_groups (
    id VARCHAR(36) PRIMARY KEY, name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY, name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V2__create_meals.sql
```sql
CREATE TABLE meal_plans (
    id VARCHAR(36) PRIMARY KEY, date DATE NOT NULL,
    slot VARCHAR(20) NOT NULL, meal_name VARCHAR(200) NOT NULL,
    description TEXT, cuisine VARCHAR(30),
    estimated_calories INT, estimated_protein_grams INT,
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V1_create_todos.sql
```sql
CREATE TABLE todo_items (
    id VARCHAR(36) PRIMARY KEY, title VARCHAR(255) NOT NULL, description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    scope VARCHAR(20) NOT NULL DEFAULT 'WEEKLY',
    due_date DATE, completed BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_to_id VARCHAR(36) REFERENCES users(id),
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V4__create_shopping.sql
```sql
CREATE TABLE shopping_items (
    id VARCHAR(36) PRIMARY KEY, name VARCHAR(255) NOT NULL,
    quantity VARCHAR(100), category VARCHAR(30),
    purchased BOOLEAN NOT NULL DEFAULT FALSE,
    ai_suggested BOOLEAN NOT NULL DEFAULT FALSE,
    added_by_user_id VARCHAR(36),
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V5__create_reminders.sql
```sql
CREATE TABLE reminders (
    id VARCHAR(36) PRIMARY KEY, title VARCHAR(255) NOT NULL,
    message TEXT, trigger_at TIMESTAMP NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    recurring BOOLEAN NOT NULL DEFAULT FALSE, recurrence_pattern VARCHAR(50),
    assigned_to_id VARCHAR(36) REFERENCES users(id),
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V6__create_pantry.sql
```sql
CREATE TABLE pantry_items (
    id VARCHAR(36) PRIMARY KEY, name VARCHAR(200) NOT NULL,
    current_quantity DECIMAL(10,2), unit VARCHAR(30),
    threshold_quantity DECIMAL(10,2),
    average_weekly_usage DECIMAL(10,2),
    last_restocked_at DATE,
    track_consumption BOOLEAN NOT NULL DEFAULT FALSE,
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V7__create_nutrition.sql
```sql
CREATE TABLE nutrition_logs (
    id VARCHAR(36) PRIMARY KEY, log_date DATE NOT NULL,
    total_calories INT, total_protein_grams INT,
    total_carbs_grams INT, total_fat_grams INT,
    alert_sent BOOLEAN NOT NULL DEFAULT FALSE,
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```


# ══════════════════════════════════════════
# SECTION 10 — DEPLOYMENT
# ══════════════════════════════════════════

## Dockerfile (backend root)
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Railway environment variables
# SPRING_PROFILES_ACTIVE = prod
# OPENAI_API_KEY         = sk-...
# JWT_SECRET             = <random 64-char string>
# MAIL_USERNAME          = youremail@gmail.com
# MAIL_PASSWORD          = <gmail app password>
# KAFKA_BROKER_URL       = <from Upstash dashboard>
# KAFKA_USERNAME         = <from Upstash dashboard>
# KAFKA_PASSWORD         = <from Upstash dashboard>
# DATABASE_URL           → injected automatically by Railway PostgreSQL add-on
# DATABASE_USER          → injected automatically
# DATABASE_PASSWORD      → injected automatically

## vercel.json (frontend root)
```json
{ "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }] }
```

## railway.toml (backend root)
```toml
[build]
builder = "DOCKERFILE"
[deploy]
startCommand = "java -jar app.jar"
restartPolicyType = "ON_FAILURE"
```


# ══════════════════════════════════════════
# SECTION 11 — FRONTEND STRUCTURE
# ══════════════════════════════════════════

## Setup
```bash
npm create vite@latest frontend -- --template react
cd frontend
npm install -D tailwindcss postcss autoprefixer && npx tailwindcss init -p
npm install @tanstack/react-query axios react-router-dom react-hot-toast date-fns lucide-react
```

## Pages
# LoginPage · RegisterPage · DashboardPage
# MealsPage           — weekly meal calendar (breakfast → dinner per day)
# TodoPage            — filter by WEEKLY / MONTHLY / LONGTERM · priority sort
# ShoppingPage        — grouped by category (GROCERIES / CLOTHING / COSMETICS...)
# RemindersPage
# PantryPage          — current stock levels · tracked items (oil, sugar)
# NutritionPage       — weekly calories + protein chart · alert history
# AiAssistantPage     — chat interface

## src/api/client.js
```js
import axios from 'axios';
const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});
client.interceptors.request.use(config => {
  const token = localStorage.getItem('jwt');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
client.interceptors.response.use(res => res, err => {
  if (err.response?.status === 401) { localStorage.clear(); window.location.href = '/login'; }
  return Promise.reject(err);
});
export default client;
```


# ══════════════════════════════════════════
# SECTION 12 — BUILD ORDER
# ══════════════════════════════════════════
# 1.  auth/          → register + login + JWT end-to-end
# 2.  family/        → FamilyGroup + link users
# 3.  todo/          → CRUD + Priority enum + TodoScope enum (WEEKLY/MONTHLY/LONGTERM)
# 4.  meal/          → weekly meal plan CRUD + CuisineType enum
# 5.  shopping/      → CRUD grouped by ShoppingCategory
# 6.  reminder/      → @Scheduled poller
# 7.  pantry/        → stock tracking + threshold + trackConsumption flag
# 8.  nutrition/     → NutritionLog entity + NutritionScheduler
# 9.  docker-compose → Kafka locally · create all 6 topics
# 10. kafka events   → wire producers + consumers one topic at a time:
#                      reminder.triggered → simplest (scheduler → email)
#                      meal.planned → shopping auto-suggest
#                      pantry.low → shopping + notification
#                      nutrition.alert → notification
#                      todo.completed → AI summary
#                      ai.requested → async AI calls
# 11. ai/            → AiService with all 8 features
#                      start with meal suggestions + nutrition calc
#                      then pantry depletion + consumption analysis
#                      then chat assistant
# 12. frontend       → React pages module by module
# 13. deploy         → Railway + Vercel + swap to Upstash Kafka

## Completed
- [x] auth
- [x] family
- [x] todo
- [x] meal
- [x] shopping
- [x] reminder
- [x] pantry

## Current
- [ ] nutrition package