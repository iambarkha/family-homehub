# Homehub — Complete Build Guide
# ==========================================
# Reflects all decisions made:
#  - 1 Spring Boot service (modular monolith)
#  - H2 for local dev, PostgreSQL on Railway for prod
#  - React (Vite) on Vercel
#  - Feature-first package structure
#  - All AI features included


# ──────────────────────────────────────────
# PROJECT STRUCTURE (monorepo)
# ──────────────────────────────────────────

# family-home/
# ├── backend/          ← Spring Boot (1 service, 1 JAR, 1 Railway deploy)
# └── frontend/         ← React + Vite + Tailwind (Vercel deploy)


# ──────────────────────────────────────────
# BACKEND: pom.xml dependencies
# ──────────────────────────────────────────

```xml
<dependencies>
  <!-- Core Web + JPA + Security -->
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-mail</artifactId></dependency>

  <!-- H2 for local dev (no install needed) -->
  <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>

  <!-- PostgreSQL for production (Railway) -->
  <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>

  <!-- Flyway: runs migrations on startup in both environments -->
  <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>

  <!-- JWT -->
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.3</version></dependency>
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.3</version><scope>runtime</scope></dependency>
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.3</version><scope>runtime</scope></dependency>

  <!-- Spring AI (OpenAI) -->
  <dependency><groupId>org.springframework.ai</groupId><artifactId>spring-ai-openai-spring-boot-starter</artifactId></dependency>

  <!-- Utilities -->
  <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
</dependencies>
```


# ──────────────────────────────────────────
# BACKEND: Package structure (feature-first modular monolith)
# ONE Spring Boot app — 6 feature packages inside
# ──────────────────────────────────────────

# src/main/java/com/aab/homehub/
# │
# ├── HomehubApplication.java
# │
# ├── auth/
# │   ├── AuthController.java        /api/auth/register, /api/auth/login
# │   ├── AuthService.java
# │   ├── User.java                  @Entity
# │   ├── UserRepository.java
# │   ├── Role.java                  enum ADMIN, MEMBER
# │   └── dto/
# │       ├── RegisterRequest.java
# │       ├── LoginRequest.java
# │       └── AuthResponse.java      { token, user }
# │
# ├── family/
# │   ├── FamilyGroup.java           @Entity — one per household
# │   ├── FamilyRepository.java
# │   └── FamilyService.java
# │
# ├── meal/
# │   ├── MealController.java        /api/meals
# │   ├── MealService.java
# │   ├── MealPlan.java              @Entity
# │   ├── MealSlot.java              enum BREAKFAST, LUNCH, DINNER, SNACK
# │   ├── MealRepository.java
# │   └── dto/
# │       ├── MealPlanRequest.java
# │       └── MealPlanResponse.java
# │
# ├── todo/
# │   ├── TodoController.java        /api/todos
# │   ├── TodoService.java
# │   ├── TodoItem.java              @Entity
# │   ├── Priority.java              enum HIGH, MEDIUM, LOW
# │   ├── TodoRepository.java
# │   └── dto/
# │       ├── TodoRequest.java
# │       └── TodoResponse.java
# │
# ├── shopping/
# │   ├── ShoppingController.java    /api/shopping
# │   ├── ShoppingService.java
# │   ├── ShoppingItem.java          @Entity
# │   ├── ShoppingCategory.java      enum PRODUCE, DAIRY, MEAT, BAKERY, FROZEN, OTHER
# │   ├── ShoppingRepository.java
# │   └── dto/
# │       ├── ShoppingRequest.java
# │       └── ShoppingResponse.java
# │
# ├── reminder/
# │   ├── ReminderController.java    /api/reminders
# │   ├── ReminderService.java       @Scheduled poller (every 60s)
# │   ├── Reminder.java              @Entity
# │   ├── ReminderRepository.java
# │   └── dto/
# │       ├── ReminderRequest.java
# │       └── ReminderResponse.java
# │
# ├── ai/
# │   ├── AiController.java          /api/ai/*
# │   └── AiService.java             Spring AI ChatClient — all 4 AI features
# │
# └── config/
#     ├── SecurityConfig.java        JWT filter chain, CORS for React
#     ├── JwtTokenProvider.java      generate / validate tokens
#     ├── JwtAuthFilter.java         OncePerRequestFilter
#     ├── OpenAiConfig.java          ChatClient bean
#     ├── BaseEntity.java            createdAt, updatedAt
#     └── GlobalExceptionHandler.java  @RestControllerAdvice


# ──────────────────────────────────────────
# DATABASE STRATEGY (KEY DECISION)
# No PostgreSQL install on your laptop — ever
# ──────────────────────────────────────────

# application.yml  (base — shared by all profiles)
```yaml
spring:
  application:
    name: homehub
  profiles:
    active: dev             # overridden to "prod" on Railway via env var
  jpa:
    hibernate:
      ddl-auto: validate    # Flyway owns the schema, not Hibernate
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:dummy-key-for-dev}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
app:
  jwt:
    secret: ${JWT_SECRET:local-dev-secret-change-in-prod}
    expiry-ms: 86400000
  cors:
    allowed-origins:
      - http://localhost:5173
      - https://your-app.vercel.app
```

# application-dev.yml  (local laptop — H2, no install needed)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:householddb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true         # visit http://localhost:8080/h2-console to inspect data
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
  flyway:
    locations: classpath:db/migration,classpath:db/migration-h2
    # h2 subfolder for any H2-specific SQL syntax differences
```

# application-prod.yml  (Railway — PostgreSQL injected automatically)
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}          # Railway injects this — you do nothing
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
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


# ──────────────────────────────────────────
# FLYWAY MIGRATIONS
# src/main/resources/db/migration/
# ──────────────────────────────────────────

# V1__create_users_family.sql
```sql
CREATE TABLE family_groups (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V2__create_meals.sql
```sql
CREATE TABLE meal_plans (
    id VARCHAR(36) PRIMARY KEY,
    date DATE NOT NULL,
    slot VARCHAR(20) NOT NULL,         -- BREAKFAST, LUNCH, DINNER, SNACK
    meal_name VARCHAR(200) NOT NULL,
    description TEXT,
    prep_time_minutes INT,
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V3__create_todos.sql
```sql
CREATE TABLE todo_items (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',   -- HIGH, MEDIUM, LOW
    due_date DATE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_to_id VARCHAR(36) REFERENCES users(id),
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V4__create_shopping.sql
```sql
CREATE TABLE shopping_items (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity VARCHAR(100),
    category VARCHAR(30),              -- PRODUCE, DAIRY, MEAT, BAKERY, FROZEN, OTHER
    purchased BOOLEAN NOT NULL DEFAULT FALSE,
    added_by_user_id VARCHAR(36) REFERENCES users(id),
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

# V5__create_reminders.sql
```sql
CREATE TABLE reminders (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    trigger_at TIMESTAMP NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_pattern VARCHAR(50),
    assigned_to_id VARCHAR(36) REFERENCES users(id),
    family_group_id VARCHAR(36) REFERENCES family_groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```


# ──────────────────────────────────────────
# ENTITY CLASSES
# ──────────────────────────────────────────

# User.java
```java
@Entity @Table(name = "users")
@Data @NoArgsConstructor @Builder
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String name;
    @Column(unique = true, nullable = false) private String email;
    @Column(nullable = false) private String password;  // BCrypt
    @Enumerated(EnumType.STRING) private Role role;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id")
    private FamilyGroup familyGroup;
}
```

# FamilyGroup.java
```java
@Entity @Table(name = "family_groups")
@Data @NoArgsConstructor @Builder
public class FamilyGroup extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String name;
    @OneToMany(mappedBy = "familyGroup", cascade = CascadeType.ALL)
    private List<User> members = new ArrayList<>();
}
```

# MealPlan.java
```java
@Entity @Table(name = "meal_plans")
@Data @NoArgsConstructor @Builder
public class MealPlan extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private LocalDate date;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MealSlot slot;
    @Column(nullable = false) private String mealName;
    private String description;
    private Integer prepTimeMinutes;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id") private FamilyGroup familyGroup;
}
```

# TodoItem.java
```java
@Entity @Table(name = "todo_items")
@Data @NoArgsConstructor @Builder
public class TodoItem extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String title;
    private String description;
    @Enumerated(EnumType.STRING) private Priority priority;
    private LocalDate dueDate;
    private boolean completed;
    @ManyToOne(fetch = FetchType.LAZY) private User assignedTo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id") private FamilyGroup familyGroup;
}
```

# ShoppingItem.java
```java
@Entity @Table(name = "shopping_items")
@Data @NoArgsConstructor @Builder
public class ShoppingItem extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String name;
    private String quantity;
    @Enumerated(EnumType.STRING) private ShoppingCategory category;
    private boolean purchased;
    private String addedByUserId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id") private FamilyGroup familyGroup;
}
```

# Reminder.java
```java
@Entity @Table(name = "reminders")
@Data @NoArgsConstructor @Builder
public class Reminder extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String title;
    private String message;
    @Column(nullable = false) private LocalDateTime triggerAt;
    private boolean sent;
    private boolean recurring;
    private String recurrencePattern;
    @ManyToOne(fetch = FetchType.LAZY) private User assignedTo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id") private FamilyGroup familyGroup;
}
```

# BaseEntity.java
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;
}
```


# ──────────────────────────────────────────
# AI SERVICE (Spring AI — all 4 features)
# ──────────────────────────────────────────

```java
@Service @RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final MealRepository mealRepo;
    private final TodoRepository todoRepo;
    private final ShoppingRepository shoppingRepo;

    // 1. Weekly meal suggestions
    public String suggestWeeklyMeals(String familyGroupId, String preferences) {
        String prompt = """
            You are a helpful family meal planner.
            Family preferences: %s
            Suggest a 7-day meal plan (breakfast, lunch, dinner) as JSON:
            { "days": [ { "date": "YYYY-MM-DD", "breakfast": "...", "lunch": "...", "dinner": "..." } ] }
            Return ONLY valid JSON.
            """.formatted(preferences);
        return chatClient.prompt().user(prompt).call().content();
    }

    // 2. Auto-suggest shopping from meal plan
    public String suggestShoppingItems(String familyGroupId) {
        List<MealPlan> meals = mealRepo.findByFamilyGroupIdAndDateBetween(
            familyGroupId, LocalDate.now(), LocalDate.now().plusDays(7));
        String mealList = meals.stream()
            .map(m -> m.getSlot() + ": " + m.getMealName())
            .collect(Collectors.joining("\n"));
        String prompt = """
            Based on these planned meals:\n%s
            Generate a shopping list as JSON:
            { "items": [ { "name": "...", "quantity": "...", "category": "PRODUCE|DAIRY|MEAT|BAKERY|FROZEN|OTHER" } ] }
            Return ONLY valid JSON.
            """.formatted(mealList);
        return chatClient.prompt().user(prompt).call().content();
    }

    // 3. Todo priority advisor
    public String adviseTodoPriorities(String familyGroupId) {
        List<TodoItem> todos = todoRepo.findByFamilyGroupIdAndCompletedFalse(familyGroupId);
        String todoList = todos.stream()
            .map(t -> "- %s (due: %s, current: %s)".formatted(t.getTitle(), t.getDueDate(), t.getPriority()))
            .collect(Collectors.joining("\n"));
        String prompt = """
            Household tasks:\n%s
            Re-rank by urgency. Return JSON:
            { "ranked": [ { "id": "...", "title": "...", "suggestedPriority": "HIGH|MEDIUM|LOW", "reason": "..." } ] }
            Return ONLY valid JSON.
            """.formatted(todoList);
        return chatClient.prompt().user(prompt).call().content();
    }

    // 4. General household assistant (chat)
    public String chat(String familyGroupId, String userMessage) {
        long pendingTodos = todoRepo.countByFamilyGroupIdAndCompletedFalse(familyGroupId);
        long shoppingItems = shoppingRepo.countByFamilyGroupIdAndPurchasedFalse(familyGroupId);
        String system = """
            You are a helpful household assistant.
            Pending todos: %d · Shopping items needed: %d
            Be friendly, concise, and practical.
            """.formatted(pendingTodos, shoppingItems);
        return chatClient.prompt().system(system).user(userMessage).call().content();
    }
}
```


# ──────────────────────────────────────────
# REMINDER SCHEDULER
# ──────────────────────────────────────────

```java
@Service @RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository reminderRepo;
    private final JavaMailSender mailSender;

    @Scheduled(fixedDelay = 60_000)   // every 60 seconds
    public void sendDueReminders() {
        List<Reminder> due = reminderRepo
            .findBySentFalseAndTriggerAtBefore(LocalDateTime.now());
        for (Reminder r : due) {
            sendEmail(r);
            r.setSent(true);
            reminderRepo.save(r);
        }
    }

    private void sendEmail(Reminder r) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(r.getAssignedTo().getEmail());
        msg.setSubject("Reminder: " + r.getTitle());
        msg.setText(r.getMessage());
        mailSender.send(msg);
    }
}
```


# ──────────────────────────────────────────
# FRONTEND: React + Vite + Tailwind
# ──────────────────────────────────────────

## Setup commands
```bash
npm create vite@latest frontend -- --template react
cd frontend
npm install
npm install -D tailwindcss postcss autoprefixer && npx tailwindcss init -p
npm install @tanstack/react-query axios react-router-dom
npm install react-hot-toast date-fns lucide-react
```

## Folder structure
# src/
# ├── main.jsx
# ├── App.jsx                    routes
# ├── api/
# │   ├── client.js              axios + JWT interceptor
# │   ├── auth.js
# │   ├── meals.js
# │   ├── todos.js
# │   ├── shopping.js
# │   ├── reminders.js
# │   └── ai.js
# ├── hooks/
# │   ├── useAuth.js
# │   ├── useMeals.js
# │   ├── useTodos.js
# │   ├── useShopping.js
# │   └── useReminders.js
# ├── pages/
# │   ├── LoginPage.jsx
# │   ├── RegisterPage.jsx
# │   ├── DashboardPage.jsx
# │   ├── MealsPage.jsx
# │   ├── TodoPage.jsx
# │   ├── ShoppingPage.jsx
# │   ├── RemindersPage.jsx
# │   └── AiAssistantPage.jsx
# └── components/
#     ├── layout/   AppShell, Sidebar, TopBar
#     ├── meals/    WeeklyMealGrid, MealCard, AddMealModal
#     ├── todos/    TodoList, TodoItem, PriorityBadge, AddTodoModal
#     ├── shopping/ ShoppingList, ShoppingItem, AddItemModal
#     ├── reminders/ ReminderList, AddReminderModal
#     └── common/   ProtectedRoute, LoadingSpinner, EmptyState

## src/api/client.js
```js
import axios from 'axios';
const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('jwt');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);
export default client;
```

## .env.local (never commit)
```
VITE_API_URL=http://localhost:8080
```

## .env.production (safe to commit)
```
VITE_API_URL=https://your-app.railway.app
```


# ──────────────────────────────────────────
# DEPLOYMENT
# ──────────────────────────────────────────

## Backend Dockerfile (backend/Dockerfile)
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

## Railway environment variables (set in dashboard)
# SPRING_PROFILES_ACTIVE = prod
# OPENAI_API_KEY          = sk-...
# JWT_SECRET              = <random 64-char string>
# MAIL_USERNAME           = youremail@gmail.com
# MAIL_PASSWORD           = <gmail app password>
# DATABASE_URL            → injected automatically by Railway PostgreSQL add-on

## Vercel environment variables
# VITE_API_URL = https://your-app.railway.app

## vercel.json (frontend root)
```json
{
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
}
```

## railway.toml (backend root)
```toml
[build]
builder = "DOCKERFILE"
[deploy]
startCommand = "java -jar app.jar"
restartPolicyType = "ON_FAILURE"
```

## Local dev tip — test AI for free with Ollama
# Install from ollama.ai, then:
#   ollama pull llama3.2
# Switch pom.xml dependency to spring-ai-ollama-spring-boot-starter
# Add to application-dev.yml:
#   spring.ai.ollama.base-url: http://localhost:11434
#   spring.ai.ollama.chat.options.model: llama3.2


# ──────────────────────────────────────────
# BUILD ORDER (what to code first)
# ──────────────────────────────────────────
# 1. auth/        → Register + Login + JWT working end-to-end
# 2. family/      → FamilyGroup entity, link users to a family
# 3. todo/        → CRUD + priority — simplest feature module
# 4. meal/        → Weekly meal plan CRUD
# 5. shopping/    → Shared list with categories
# 6. reminder/    → @Scheduled email sender
# 7. ai/          → Wire up Spring AI ChatClient, all 4 features
# 8. frontend     → React pages for each module (parallel with backend)