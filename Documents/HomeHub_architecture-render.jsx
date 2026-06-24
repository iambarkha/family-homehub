import { useState } from "react";

const COLORS = {
  frontend:    { bg: "#1a2744", border: "#3b5bdb", text: "#a5b4fc", accent: "#4f6ef7" },
  gateway:     { bg: "#1a2535", border: "#0ea5e9", text: "#7dd3fc", accent: "#0ea5e9" },
  service:     { bg: "#0f2027", border: "#10b981", text: "#6ee7b7", accent: "#10b981" },
  ai:          { bg: "#1e1430", border: "#a855f7", text: "#d8b4fe", accent: "#a855f7" },
  kafka:       { bg: "#1c1408", border: "#f59e0b", text: "#fcd34d", accent: "#f59e0b" },
  consumer:    { bg: "#1a1508", border: "#d97706", text: "#fde68a", accent: "#d97706" },
  db:          { bg: "#0d1f1a", border: "#059669", text: "#6ee7b7", accent: "#059669" },
  config:      { bg: "#1a1825", border: "#8b5cf6", text: "#c4b5fd", accent: "#8b5cf6" },
  hosting:     { bg: "#1a1010", border: "#ef4444", text: "#fca5a5", accent: "#ef4444" },
};

const DETAILS = {
  frontend: {
    title: "React Frontend",
    host: "Vercel (free)",
    items: ["Vite + React + Tailwind CSS", "React Query for data fetching", "JWT stored in localStorage", "8 pages: Meals · Todo · Shopping · Pantry · Nutrition · Reminders · AI Chat · Dashboard"],
    color: COLORS.frontend,
  },
  gateway: {
    title: "API Gateway layer",
    host: "Inside Spring Boot",
    items: ["JWT validation on every incoming request", "Routes /api/* to correct feature package", "CORS configured for Vercel domain", "Spring Security filter chain"],
    color: COLORS.gateway,
  },
  auth: {
    title: "auth package",
    host: "Spring Boot",
    items: ["POST /api/auth/register", "POST /api/auth/login → returns JWT", "User entity + FamilyGroup entity", "BCrypt password encoding", "Role enum: ADMIN · MEMBER"],
    color: COLORS.service,
  },
  meal: {
    title: "meal package",
    host: "Spring Boot",
    items: ["CRUD /api/meals", "MealPlan entity: date · slot · mealName · cuisine · calories · protein", "MealSlot enum: BREAKFAST LUNCH DINNER SNACK", "CuisineType enum: INDIAN ITALIAN MEXICAN ASIAN etc.", "Publishes → meal.planned Kafka topic on save"],
    color: COLORS.service,
  },
  todo: {
    title: "todo package",
    host: "Spring Boot",
    items: ["CRUD /api/todos", "Priority enum: HIGH · MEDIUM · LOW", "TodoScope enum: WEEKLY · MONTHLY · LONGTERM", "Filter by scope on GET", "Publishes → todo.completed Kafka topic"],
    color: COLORS.service,
  },
  shopping: {
    title: "shopping package",
    host: "Spring Boot",
    items: ["CRUD /api/shopping", "ShoppingCategory: GROCERIES · CLOTHING · COSMETICS · HOUSEHOLD · OTHER", "aiSuggested flag: true when AI added it", "Consumes ← meal.planned → auto-add ingredients", "Consumes ← pantry.low → auto-add depleted items"],
    color: COLORS.service,
  },
  reminder: {
    title: "reminder package",
    host: "Spring Boot",
    items: ["CRUD /api/reminders", "@Scheduled poller every 60 seconds", "Marks reminder as sent after publishing", "Publishes → reminder.triggered Kafka topic", "Supports recurring reminders"],
    color: COLORS.service,
  },
  pantry: {
    title: "pantry package",
    host: "Spring Boot",
    items: ["CRUD /api/pantry", "PantryItem: name · currentQuantity · unit · thresholdQuantity · averageWeeklyUsage", "trackConsumption flag for oil · sugar · tracked items", "PantryScheduler: @Scheduled daily at 7am", "Publishes → pantry.low when stock below threshold"],
    color: COLORS.service,
  },
  nutrition: {
    title: "nutrition package",
    host: "Spring Boot",
    items: ["GET /api/nutrition/summary", "NutritionLog: date · calories · protein · carbs · fat", "NutritionScheduler: @Scheduled every Monday 8am", "Checks weekly avg vs thresholds (1800 kcal · 50g protein)", "Publishes → nutrition.alert if below threshold"],
    color: COLORS.service,
  },
  ai: {
    title: "ai package",
    host: "Spring Boot + OpenAI gpt-4o-mini",
    items: [
      "1: Weekly meal suggestions (4-week history + cuisine)",
      "2: Nutrition calculation per meal plan",
      "3: Nutrition threshold alert check",
      "4: Shopping list from meal plan (pantry-aware)",
      "5: Pantry depletion prediction (days remaining)",
      "6: Oil & sugar consumption health analysis",
      "7: Household chat assistant (live context)",
      "8: Weekly summary when all todos complete",
      "Consumes ← ai.requested · todo.completed",
    ],
    color: COLORS.ai,
  },
  kafka: {
    title: "Kafka Broker",
    host: "Docker Compose locally · Upstash (free) in prod",
    items: [
      "meal.planned → shopping auto-suggests ingredients",
      "reminder.triggered → notification sends email",
      "ai.requested → async AI calls (non-blocking UI)",
      "todo.completed → AI generates weekly summary",
      "nutrition.alert → notification when intake drops",
      "pantry.low → shopping list + notification",
    ],
    color: COLORS.kafka,
  },
  notification: {
    title: "notification package",
    host: "Spring Boot",
    items: ["ReminderTriggeredConsumer → JavaMailSender email", "NutritionAlertConsumer → email when calories/protein low", "PantryLowNotifier → email + auto-add to shopping list", "All consumers are @KafkaListener @Component classes"],
    color: COLORS.consumer,
  },
  database: {
    title: "Database",
    host: "H2 locally · PostgreSQL on Railway (free)",
    items: ["7 tables via Flyway migrations — no manual SQL ever", "V1: users + family_groups", "V2: meal_plans (cuisine + nutrition columns)", "V3: todo_items (scope column)", "V4: shopping_items (ai_suggested flag)", "V5: reminders", "V6: pantry_items", "V7: nutrition_logs"],
    color: COLORS.db,
  },
  config: {
    title: "config package",
    host: "Spring Boot — shared by all packages",
    items: ["SecurityConfig: JWT filter chain + CORS", "KafkaTopicConfig: all 6 topics declared as @Bean", "OpenAiConfig: Spring AI ChatClient bean", "GlobalExceptionHandler: @RestControllerAdvice", "BaseEntity: createdAt + updatedAt audit fields"],
    color: COLORS.config,
  },
  hosting: {
    title: "Hosting — all free tier",
    host: "Zero cost setup",
    items: ["Railway: Spring Boot JAR + PostgreSQL add-on", "Vercel: React static site + SPA rewrites", "Upstash: Kafka free (10k msg/day)", "GitHub Actions: CI/CD auto-deploy on push to main", "Ollama (local dev): free AI, no OpenAI cost during dev"],
    color: COLORS.hosting,
  },
};

function Node({ id, label, sublabel, color, selected, onClick, style = {} }) {
  const isSel = selected === id;
  return (
    <div onClick={() => onClick(id)} style={{
      background: color.bg,
      border: `1.5px solid ${isSel ? color.accent : color.border}`,
      borderRadius: 10, padding: "8px 14px", cursor: "pointer",
      transition: "all 0.18s",
      boxShadow: isSel ? `0 0 0 2px ${color.accent}44, 0 4px 24px ${color.accent}22` : "none",
      transform: isSel ? "scale(1.03)" : "scale(1)",
      userSelect: "none", ...style,
    }}>
      <div style={{ color: color.text, fontWeight: 600, fontSize: 12, letterSpacing: "0.02em" }}>{label}</div>
      {sublabel && <div style={{ color: color.accent, fontSize: 10, marginTop: 2, opacity: 0.85 }}>{sublabel}</div>}
    </div>
  );
}

function Arrow({ color = "#444", label }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", minHeight: 30 }}>
      <div style={{ width: 1.5, flex: 1, background: color, minHeight: 8 }} />
      {label && <div style={{ fontSize: 9, color, padding: "1px 6px", whiteSpace: "nowrap", background: "#080d14" }}>{label}</div>}
      <div style={{ fontSize: 11, color, lineHeight: 1 }}>▼</div>
    </div>
  );
}

function DetailPanel({ id, onClose }) {
  const d = DETAILS[id];
  if (!d) return null;
  return (
    <div style={{
      position: "fixed", top: 0, right: 0, bottom: 0, width: 320,
      background: "#0a0f18", borderLeft: `2px solid ${d.color.border}`,
      padding: "20px 18px", overflowY: "auto", zIndex: 100,
      boxShadow: `-8px 0 40px ${d.color.accent}18`,
    }}>
      <button onClick={onClose} style={{
        position: "absolute", top: 14, right: 14, background: "none",
        border: `1px solid ${d.color.border}`, color: d.color.text,
        borderRadius: 6, padding: "3px 10px", cursor: "pointer", fontSize: 11,
      }}>✕</button>
      <div style={{ fontSize: 9, color: d.color.accent, textTransform: "uppercase", letterSpacing: "0.12em", marginBottom: 4 }}>{d.host}</div>
      <div style={{ fontSize: 15, fontWeight: 700, color: d.color.text, marginBottom: 14 }}>{d.title}</div>
      <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
        {d.items.map((item, i) => (
          <div key={i} style={{
            display: "flex", gap: 8, alignItems: "flex-start",
            padding: "7px 10px", background: d.color.bg,
            border: `1px solid ${d.color.border}22`, borderRadius: 7,
          }}>
            <span style={{ color: d.color.accent, marginTop: 1, flexShrink: 0, fontSize: 12 }}>›</span>
            <span style={{ fontSize: 11, color: "#b0bec5", lineHeight: 1.5 }}>{item}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function App() {
  const [selected, setSelected] = useState(null);
  const toggle = (id) => setSelected(s => s === id ? null : id);

  const services = [
    { id: "auth",      label: "auth",      sublabel: "JWT · users · family" },
    { id: "meal",      label: "meal",      sublabel: "plan · cuisine · nutrition" },
    { id: "todo",      label: "todo",      sublabel: "weekly · monthly · longterm" },
    { id: "shopping",  label: "shopping",  sublabel: "groceries · clothing · cosmetics" },
    { id: "reminder",  label: "reminder",  sublabel: "@Scheduled · email" },
    { id: "pantry",    label: "pantry",    sublabel: "stock · oil/sugar tracking" },
    { id: "nutrition", label: "nutrition", sublabel: "calories · protein · alert" },
  ];

  const topics = [
    { label: "meal.planned",       flow: "meal → shopping" },
    { label: "reminder.triggered", flow: "reminder → notification" },
    { label: "ai.requested",       flow: "any → ai" },
    { label: "todo.completed",     flow: "todo → ai summary" },
    { label: "nutrition.alert",    flow: "nutrition → notification" },
    { label: "pantry.low",         flow: "pantry → shopping + notify" },
  ];

  const pr = selected ? "336px" : "20px";

  return (
    <div style={{
      minHeight: "100vh", background: "#080d14",
      fontFamily: "'JetBrains Mono','Fira Code','Courier New',monospace",
      paddingRight: pr, paddingLeft: 20, paddingTop: 20, paddingBottom: 20,
      transition: "padding-right 0.25s", boxSizing: "border-box",
    }}>

      {/* Title */}
      <div style={{ marginBottom: 20, paddingBottom: 12, borderBottom: "1px solid #1a2535" }}>
        <div style={{ fontSize: 18, fontWeight: 700, color: "#e2e8f0", letterSpacing: "-0.02em" }}>
          family-home · architecture
        </div>
        <div style={{ fontSize: 10, color: "#3d5470", marginTop: 3 }}>
          1 Spring Boot JAR · modular monolith · click any block for details
        </div>
      </div>

      {/* FRONTEND */}
      <div style={{ display: "flex", justifyContent: "center", marginBottom: 6 }}>
        <Node id="frontend" label="React Frontend" sublabel="Vercel · Vite + Tailwind · 8 pages"
          color={COLORS.frontend} selected={selected} onClick={toggle}
          style={{ width: 260, textAlign: "center" }} />
      </div>
      <div style={{ display: "flex", justifyContent: "center", marginBottom: 6 }}>
        <Arrow color="#3b5bdb" label="HTTPS REST / JSON" />
      </div>

      {/* GATEWAY */}
      <div style={{ display: "flex", justifyContent: "center", marginBottom: 6 }}>
        <Node id="gateway" label="Spring Boot · API Gateway layer" sublabel="JWT validation · routing · CORS"
          color={COLORS.gateway} selected={selected} onClick={toggle}
          style={{ width: 340, textAlign: "center" }} />
      </div>
      <div style={{ display: "flex", justifyContent: "center", marginBottom: 6 }}>
        <Arrow color="#10b981" label="internal calls" />
      </div>

      {/* SERVICE PACKAGES */}
      <div style={{ fontSize: 8, color: "#2d4a30", textTransform: "uppercase", letterSpacing: "0.14em", textAlign: "center", marginBottom: 6 }}>
        feature packages — inside one JAR
      </div>
      <div style={{ display: "flex", gap: 5, flexWrap: "wrap", justifyContent: "center", marginBottom: 6 }}>
        {services.map(s => (
          <Node key={s.id} id={s.id} label={s.label} sublabel={s.sublabel}
            color={COLORS.service} selected={selected} onClick={toggle}
            style={{ minWidth: 100 }} />
        ))}
      </div>

      {/* AI + NOTIFICATION */}
      <div style={{ display: "flex", gap: 6, justifyContent: "center", marginBottom: 6 }}>
        <Node id="ai" label="ai package" sublabel="8 features · Spring AI · gpt-4o-mini"
          color={COLORS.ai} selected={selected} onClick={toggle}
          style={{ flex: 1, maxWidth: 280, textAlign: "center" }} />
        <Node id="notification" label="notification package" sublabel="Kafka consumers · email · push"
          color={COLORS.consumer} selected={selected} onClick={toggle}
          style={{ flex: 1, maxWidth: 230, textAlign: "center" }} />
      </div>

      <div style={{ display: "flex", justifyContent: "center", marginBottom: 6 }}>
        <Arrow color="#f59e0b" label="publish / consume" />
      </div>

      {/* KAFKA */}
      <div onClick={() => toggle("kafka")} style={{
        background: COLORS.kafka.bg,
        border: `1.5px solid ${selected === "kafka" ? COLORS.kafka.accent : COLORS.kafka.border}`,
        borderRadius: 12, padding: "12px 14px", cursor: "pointer", marginBottom: 6,
        boxShadow: selected === "kafka" ? `0 0 0 2px ${COLORS.kafka.accent}33` : "none",
        transition: "all 0.18s",
      }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
          <span style={{ color: COLORS.kafka.text, fontWeight: 700, fontSize: 12 }}>Kafka broker</span>
          <span style={{ color: COLORS.kafka.accent, fontSize: 9 }}>Docker locally · Upstash in prod (free)</span>
        </div>
        <div style={{ display: "flex", gap: 5, flexWrap: "wrap" }}>
          {topics.map(t => (
            <div key={t.label} style={{
              background: "#211800", border: "1px solid #78350f",
              borderRadius: 6, padding: "4px 9px",
            }}>
              <div style={{ color: "#fcd34d", fontSize: 10, fontWeight: 600 }}>{t.label}</div>
              <div style={{ color: "#92400e", fontSize: 9, marginTop: 1 }}>{t.flow}</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ display: "flex", justifyContent: "center", marginBottom: 6 }}>
        <Arrow color="#059669" label="JPA / JDBC" />
      </div>

      {/* BOTTOM ROW */}
      <div style={{ display: "flex", gap: 6, justifyContent: "center", marginBottom: 16 }}>
        <Node id="database" label="PostgreSQL (prod) · H2 (dev)"
          sublabel="7 tables · Flyway migrations"
          color={COLORS.db} selected={selected} onClick={toggle}
          style={{ flex: 1, maxWidth: 260, textAlign: "center" }} />
        <Node id="config" label="config package"
          sublabel="Security · Kafka topics · OpenAI · Base"
          color={COLORS.config} selected={selected} onClick={toggle}
          style={{ flex: 1, maxWidth: 240, textAlign: "center" }} />
        <Node id="hosting" label="Hosting"
          sublabel="Railway · Vercel · Upstash · all free"
          color={COLORS.hosting} selected={selected} onClick={toggle}
          style={{ flex: 1, maxWidth: 180, textAlign: "center" }} />
      </div>

      {/* LEGEND */}
      <div style={{
        padding: "8px 14px", background: "#0a0f18",
        border: "1px solid #1a2535", borderRadius: 8,
        display: "flex", gap: 16, flexWrap: "wrap", justifyContent: "center",
      }}>
        {[
          [COLORS.frontend.border, "Frontend"],
          [COLORS.service.border,  "Feature packages"],
          [COLORS.ai.border,       "AI"],
          [COLORS.kafka.border,    "Kafka"],
          [COLORS.consumer.border, "Consumers"],
          [COLORS.db.border,       "Database"],
          [COLORS.config.border,   "Config"],
          [COLORS.hosting.border,  "Hosting"],
        ].map(([c, l]) => (
          <div key={l} style={{ display: "flex", alignItems: "center", gap: 5 }}>
            <div style={{ width: 9, height: 9, borderRadius: 2, background: c }} />
            <span style={{ fontSize: 9, color: "#4a6080" }}>{l}</span>
          </div>
        ))}
      </div>

      {selected && <DetailPanel id={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}
