# 🚀 Onboarding Agent

> AI-powered onboarding assistant that explains Java code to developers using Spring Boot, Spring AI, and GPT‑4o.

---

## 🧠 Overview

**Onboarding Agent** helps developers quickly understand unfamiliar Java codebases by generating clear, context-aware explanations tailored to their experience level.

---

## ✨ Features

- Explain Java classes in plain English  
- Adaptive explanations (junior / mid / senior)  
- Multi-turn conversational guidance  
- Context-aware AI responses using GPT‑4o  
- Persistent storage with Cassandra (Astra DB)  
- Searchable guide history  

---

## 🧱 Tech Stack

- Java 21  
- Spring Boot 3.3.x  
- Spring AI + OpenAI GPT‑4o  
- DataStax Astra DB (Cassandra)  
- Maven  
- HTML, CSS, Vanilla JS  

---

## ▶️ Running the App

```bash
mvn spring-boot:run
```

App runs at: http://localhost:8080

---

## 🔌 API Endpoints

### GET /health
Health check endpoint

### POST /api/guide/generate
Start guide generation

### POST /api/guide/reply
Continue conversation

### GET /api/guide/history
Retrieve saved guides

---

## 🧠 How It Works

1. User pastes Java code  
2. AI asks clarifying questions  
3. User responds  
4. AI generates explanation  
5. Guide is stored for reuse  

---

## 📜 License

For learning and experimentation purposes.
