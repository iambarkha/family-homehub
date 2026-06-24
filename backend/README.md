# family-homehub
A homehub application to track household details

A personal household management app for families.
Built with Spring Boot · React · Kafka · Spring AI.

## Features
- Weekly meal planner with cuisine preferences
- Todo list (weekly / monthly / long-term) with priority
- Shopping list grouped by category (groceries, clothing, cosmetics…)
- Pantry stock tracker with depletion alerts
- Nutrition tracking (calories, protein) with AI alerts
- Reminders with email notifications
- AI assistant — meal suggestions, shopping auto-generate, chat

## Tech stack
| Layer | Technology |
|---|---|
| Backend | Java 17 · Spring Boot · Spring AI · Spring Kafka |
| Frontend | React · Vite · Tailwind CSS |
| Database | PostgreSQL (prod) · H2 (dev) · Flyway |
| Messaging | Kafka — Docker locally · Upstash in prod |
| Hosting | Railway (backend) · Vercel (frontend) |

## Project structure
- `backend/` — Spring Boot modular monolith
- `frontend/` — React + Vite
- `docs/ARCHITECTURE.md` — full architecture, entities, Kafka setup

## Running locally
\`\`\`bash
# Start Kafka
cd backend && docker-compose up -d

# Start backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Start frontend
cd frontend && npm run dev
\`\`\`
