# 📋 TODO List - Assignment 3 Implementation

## 🎯 Obiective Principale (5p obligatoriu + 3p bonus)

### ✅ OBLIGATORIU (5p):
1. **WebSocket Microservice** - Notificări overconsumption în timp real
2. **Rule-Based Chatbot** - Customer Support cu minimum 10 reguli
3. **README.md** - Documentație completă
4. **Răspunsuri la întrebări** - Load Balancing, Docker Swarm, WebSockets, Security

### 🎁 BONUS:
- **AI-Driven Customer Support** (1p) - Integrare LLM (Gemini/OpenAI)
- **Client-to-Admin Chat** (2p) - Chat bidirectional prin WebSocket

---

## 📊 Progres Overview

| Fază | Descriere | Puncte | Status |
|------|-----------|--------|--------|
| FAZA 1 | WebSocket Microservice + Overconsumption Alerts | 2.5p | ⏳ TODO |
| FAZA 2 | Rule-Based Chatbot (10+ reguli) | 2.5p | ⏳ TODO |
| FAZA 3 | AI-Driven Support (Gemini API) | +1p | ⏳ TODO |
| FAZA 4 | Client-to-Admin Chat | +2p | ⏳ TODO |
| FAZA 5 | Documentation & Deployment | - | ⏳ TODO |

**Total Posibil:** 5p (obligatoriu) + 3p (bonus) = **8p**

---

## ✅ FAZA 1: WebSocket Microservice + Overconsumption Alerts (2-3 ore)

### 🎯 Obiectiv
Creează un microservice WebSocket care trimite notificări în timp real către frontend când un device depășește consumul maxim orar.

### 📦 1.1 Setup WebSocket Microservice (Java Spring Boot)

**Structură proiect:**
```
websocket-microservice/
├── src/main/java/com/energy/websocket/
│   ├── WebSocketApplication.java
│   ├── config/
│   │   ├── WebSocketConfig.java          # Configurare WebSocket
│   │   └── RabbitMQConfig.java           # Configurare RabbitMQ
│   ├── handler/
│   │   └── NotificationWebSocketHandler.java  # Handler WebSocket
│   ├── listener/
│   │   └── OverconsumptionListener.java  # Listener RabbitMQ
│   ├── dto/
│   │   └── NotificationMessage.java      # DTO pentru notificări
│   └── service/
│       └── NotificationService.java      # Service pentru broadcast
├── pom.xml
└── Dockerfile
```

**Checklist:**
- [ ] Creează folder `websocket-microservice/`
- [ ] Creează `pom.xml` cu dependencies:
  - [ ] `spring-boot-starter-websocket`
  - [ ] `spring-boot-starter-amqp` (RabbitMQ)
  - [ ] `spring-boot-starter-web`
  - [ ] `lombok`
- [ ] Creează `application.yml`:
  - [ ] Port: 8085
  - [ ] RabbitMQ connection
  - [ ] WebSocket endpoint: `/ws/notifications`

### 🔧 1.2 Configurare WebSocket

**WebSocketConfig.java:**
- [ ] Implementează `WebSocketConfigurer`
- [ ] Registrează handler la `/ws/notifications`
- [ ] Configurează CORS pentru frontend
- [ ] Permite conexiuni de la `http://localhost:3000`

**NotificationWebSocketHandler.java:**
- [ ] Extinde `TextWebSocketHandler`
- [ ] Metodă `afterConnectionEstablished()` - salvează sesiunea
- [ ] Metodă `afterConnectionClosed()` - șterge sesiunea
- [ ] Metodă `sendNotification()` - trimite mesaj la toate sesiunile active
- [ ] Map<String, WebSocketSession> pentru tracking conexiuni


### 🐰 1.3 Integrare RabbitMQ pentru Overconsumption

**RabbitMQConfig.java:**
- [ ] Declară queue: `overconsumption_queue`
- [ ] Bean `Jackson2JsonMessageConverter`
- [ ] Bean `RabbitTemplate`

**OverconsumptionListener.java:**
- [ ] `@RabbitListener(queues = "overconsumption_queue")`
- [ ] Primește mesaj cu: deviceId, userId, consumption, maxConsumption, timestamp
- [ ] Creează NotificationMessage
- [ ] Apelează NotificationService.broadcast()

**NotificationMessage.java (DTO):**
```java
{
  "type": "OVERCONSUMPTION",
  "deviceId": "...",
  "userId": "...",
  "message": "Device X exceeded max consumption: 25.5 kWh > 20.0 kWh",
  "consumption": 25.5,
  "maxConsumption": 20.0,
  "timestamp": "2025-12-05T14:30:00"
}
```

### 📊 1.4 Modificări în Monitoring Microservice

**Obiectiv:** Detectează overconsumption și publică în RabbitMQ

**ConsumptionAggregationService.java:**
- [ ] După salvare în `HourlyConsumption`, verifică dacă `totalConsumption > device.maxHourlyConsumption`
- [ ] Dacă DA:
  - [ ] Creează `OverconsumptionEvent` (DTO)
  - [ ] Publică în `overconsumption_queue` via RabbitTemplate
  - [ ] Log: "Overconsumption detected for device X"

**OverconsumptionEvent.java (DTO nou):**
- [ ] String deviceId
- [ ] String userId
- [ ] Double consumption
- [ ] Double maxConsumption
- [ ] LocalDateTime timestamp

**Modificări în pom.xml (dacă nu există deja):**
- [ ] Verifică că ai `spring-boot-starter-amqp`

### 🎨 1.5 Frontend - Notificări WebSocket

**Creează `hooks/useWebSocket.ts`:**
- [ ] Custom hook pentru conexiune WebSocket
- [ ] Conectare la `ws://localhost:8085/ws/notifications`
- [ ] Listener pentru mesaje
- [ ] Callback pentru notificări noi
- [ ] Auto-reconnect la disconnect

**Modificări în `app/dashboard/monitoring/page.tsx`:**
- [ ] Import `useWebSocket` hook
- [ ] Afișează toast notification când primește mesaj
- [ ] Folosește `sonner` (deja instalat) pentru toast
- [ ] Badge roșu pentru overconsumption

**Exemplu notificare:**
```
🚨 Overconsumption Alert!
Device "Laptop Ana" exceeded limit:
25.5 kWh > 20.0 kWh
```


### 🐳 1.6 Docker & Deployment

**Dockerfile pentru WebSocket MS:**
- [ ] Creează `websocket-microservice/Dockerfile`
- [ ] Multi-stage build (Maven + JRE)
- [ ] Expune port 8085

**docker-compose.yml:**
- [ ] Adaugă service `websocket-microservice`:
  - [ ] Port: 8085:8085
  - [ ] Depends on: rabbitmq
  - [ ] Environment: RABBITMQ_HOST, RABBITMQ_PORT
  - [ ] Traefik labels pentru `/ws/*`
  - [ ] Network: energy-network

**RabbitMQ Queue:**
- [ ] Queue-ul `overconsumption_queue` se creează automat la pornire
- [ ] Verifică în RabbitMQ Management UI (http://localhost:15672)

### ✅ 1.7 Testing FAZA 1

**Test 1: WebSocket Connection**
- [ ] Pornește toate serviciile: `docker-compose up -d`
- [ ] Verifică logs: `docker logs websocket-microservice-app`
- [ ] Deschide frontend: http://localhost:3000/dashboard/monitoring
- [ ] Verifică în browser console: "WebSocket connected"

**Test 2: Overconsumption Detection**
- [ ] Creează device cu `maxHourlyConsumption = 5.0` kWh
- [ ] Editează `device-simulator/config.json`:
  - [ ] Setează device_id = ID-ul device-ului creat
  - [ ] `test_mode: true`, `max_measurements: 6`
- [ ] Rulează simulator: `python device-simulator/simulator.py`
- [ ] Așteaptă 60 secunde (6 măsurători × 10s)
- [ ] Verifică că suma > 5.0 kWh (normal ar fi ~15-20 kWh)
- [ ] Verifică notificare în frontend (toast roșu)

**Test 3: RabbitMQ Message Flow**
- [ ] Verifică RabbitMQ UI: http://localhost:15672
- [ ] Queue `overconsumption_queue` trebuie să existe
- [ ] Verifică că mesajul a fost consumat (0 messages)
- [ ] Verifică logs Monitoring MS: "Overconsumption detected"
- [ ] Verifică logs WebSocket MS: "Broadcasting notification"

**Status:** ⏳ TO DO

---

## ✅ FAZA 2: Rule-Based Chatbot - Customer Support (2-3 ore)

### 🎯 Obiectiv
Implementează un chatbot simplu cu minimum 10 reguli care răspunde automat la întrebări frecvente.

### 📦 2.1 Setup Customer Support Microservice (Java Spring Boot)

**Structură proiect:**
```
customer-support-microservice/
├── src/main/java/com/energy/support/
│   ├── CustomerSupportApplication.java
│   ├── controller/
│   │   └── ChatController.java           # REST API pentru chat
│   ├── service/
│   │   ├── RuleBasedChatService.java     # Logica cu reguli
│   │   └── ChatRule.java                 # Interface pentru reguli
│   ├── rules/
│   │   ├── GreetingRule.java
│   │   ├── DeviceInfoRule.java
│   │   ├── ConsumptionRule.java
│   │   └── ... (10+ reguli)
│   └── dto/
│       ├── ChatRequest.java
│       └── ChatResponse.java
├── pom.xml
└── Dockerfile
```

**Checklist:**
- [ ] Creează folder `customer-support-microservice/`
- [ ] Creează `pom.xml` cu dependencies:
  - [ ] `spring-boot-starter-web`
  - [ ] `spring-boot-starter-actuator`
  - [ ] `lombok`
- [ ] Creează `application.yml`:
  - [ ] Port: 8086
  - [ ] Logging level: DEBUG

### 🧠 2.2 Implementare Rule-Based System

**ChatRule.java (Interface):**
```java
public interface ChatRule {
    boolean matches(String message);
    String getResponse(String message);
    int getPriority(); // Pentru ordinea verificării
}
```

**RuleBasedChatService.java:**
- [ ] List<ChatRule> rules (injectate automat)
- [ ] Metodă `processMessage(String message)`:
  - [ ] Iterează prin reguli (sortate după priority)
  - [ ] Dacă `rule.matches(message)` → returnează `rule.getResponse()`
  - [ ] Dacă nicio regulă nu se potrivește → returnează mesaj default
- [ ] Metodă `getAllRules()` - pentru debugging


### 📜 2.3 Implementare 10+ Reguli (OBLIGATORIU)

**Lista Reguli Minime:**

1. **GreetingRule.java** - Salutări
   - [ ] Keywords: "hello", "hi", "hey", "bună", "salut"
   - [ ] Response: "Hello! How can I help you today?"

2. **DeviceInfoRule.java** - Informații despre device-uri
   - [ ] Keywords: "device", "add device", "create device"
   - [ ] Response: "To add a device, go to Devices page and click 'Add Device'..."

3. **ConsumptionRule.java** - Întrebări despre consum
   - [ ] Keywords: "consumption", "energy", "kwh", "consum"
   - [ ] Response: "You can view your energy consumption in the Monitoring page..."

4. **OverconsumptionRule.java** - Alertă overconsumption
   - [ ] Keywords: "overconsumption", "exceeded", "alert", "notification"
   - [ ] Response: "Overconsumption alerts are sent when a device exceeds its max hourly limit..."

5. **AccountRule.java** - Informații cont
   - [ ] Keywords: "account", "profile", "password", "email"
   - [ ] Response: "You can manage your account in the Profile section..."

6. **BillingRule.java** - Facturare
   - [ ] Keywords: "bill", "payment", "invoice", "cost"
   - [ ] Response: "Billing information is calculated based on your hourly consumption..."

7. **TechnicalSupportRule.java** - Suport tehnic
   - [ ] Keywords: "error", "bug", "not working", "problem"
   - [ ] Response: "For technical issues, please contact support@energy.ro..."

8. **DataExportRule.java** - Export date
   - [ ] Keywords: "export", "download", "csv", "report"
   - [ ] Response: "You can export your consumption data from the Monitoring page..."

9. **MaxConsumptionRule.java** - Limită consum
   - [ ] Keywords: "max consumption", "limit", "threshold"
   - [ ] Response: "You can set max hourly consumption when creating/editing a device..."

10. **HelpRule.java** - Ajutor general
    - [ ] Keywords: "help", "how to", "guide", "tutorial"
    - [ ] Response: "Here are the main features: Devices, Monitoring, Users..."

11. **FarewellRule.java** - La revedere
    - [ ] Keywords: "bye", "goodbye", "thanks", "thank you"
    - [ ] Response: "You're welcome! Have a great day!"

**Implementare fiecare regulă:**
- [ ] Creează clasă care implementează `ChatRule`
- [ ] Adaugă `@Component` pentru auto-detection
- [ ] Implementează `matches()` cu case-insensitive matching
- [ ] Implementează `getResponse()` cu mesaj relevant
- [ ] Setează `priority` (1-10, mai mic = prioritate mai mare)


### 🌐 2.4 REST API pentru Chat

**ChatController.java:**
- [ ] `POST /api/chat/message` - Trimite mesaj
  - [ ] Request: `{ "message": "How to add a device?" }`
  - [ ] Response: `{ "response": "To add a device...", "isRuleBased": true }`
- [ ] `GET /api/chat/rules` - Lista reguli (pentru debugging)
  - [ ] Response: `[{ "name": "GreetingRule", "priority": 1 }, ...]`

**ChatRequest.java (DTO):**
- [ ] String message
- [ ] String userId (opțional, pentru tracking)

**ChatResponse.java (DTO):**
- [ ] String response
- [ ] Boolean isRuleBased (true dacă a folosit regulă, false dacă AI)
- [ ] String ruleName (opțional, pentru debugging)
- [ ] LocalDateTime timestamp

### 🎨 2.5 Frontend - Chat Interface

**Creează `app/dashboard/chat/page.tsx`:**
- [ ] Chat UI cu:
  - [ ] Lista mesaje (user + bot)
  - [ ] Input pentru mesaj nou
  - [ ] Buton "Send"
  - [ ] Auto-scroll la mesaj nou
- [ ] Stilizare:
  - [ ] Mesaje user: dreapta, albastru
  - [ ] Mesaje bot: stânga, gri
  - [ ] Badge "Rule-based" sau "AI" pentru fiecare răspuns

**Componente UI (folosește shadcn/ui):**
- [ ] `Card` pentru container chat
- [ ] `ScrollArea` pentru lista mesaje
- [ ] `Input` + `Button` pentru trimitere mesaj
- [ ] `Badge` pentru tip răspuns

**API Integration:**
- [ ] Funcție `sendMessage(message: string)`:
  - [ ] POST la `/api/chat/message`
  - [ ] Adaugă răspuns în lista mesaje
  - [ ] Afișează loading state

### 🐳 2.6 Docker & Deployment

**Dockerfile pentru Customer Support MS:**
- [ ] Creează `customer-support-microservice/Dockerfile`
- [ ] Multi-stage build
- [ ] Expune port 8086

**docker-compose.yml:**
- [ ] Adaugă service `customer-support-microservice`:
  - [ ] Port: 8086:8086
  - [ ] Network: energy-network
  - [ ] Traefik labels pentru `/api/chat`


### ✅ 2.7 Testing FAZA 2

**Test 1: Rule Matching**
- [ ] Pornește Customer Support MS: `docker-compose up -d customer-support-microservice`
- [ ] Test API cu curl:
  ```bash
  curl -X POST http://localhost/api/chat/message \
    -H "Content-Type: application/json" \
    -d '{"message": "hello"}'
  ```
- [ ] Verifică răspuns: "Hello! How can I help you today?"

**Test 2: Toate Regulile**
- [ ] Testează fiecare regulă (minimum 10):
  - [ ] "hello" → Greeting
  - [ ] "how to add device" → Device Info
  - [ ] "consumption" → Consumption Info
  - [ ] "overconsumption alert" → Overconsumption Info
  - [ ] "account" → Account Info
  - [ ] "billing" → Billing Info
  - [ ] "error" → Technical Support
  - [ ] "export data" → Data Export
  - [ ] "max consumption" → Max Consumption
  - [ ] "help" → Help
  - [ ] "bye" → Farewell

**Test 3: Frontend Chat**
- [ ] Deschide http://localhost:3000/dashboard/chat
- [ ] Trimite mesaj: "hello"
- [ ] Verifică răspuns bot apare instant
- [ ] Verifică badge "Rule-based"
- [ ] Testează 5-6 întrebări diferite

**Test 4: No Match Scenario**
- [ ] Trimite mesaj random: "asdfghjkl"
- [ ] Verifică răspuns default: "I'm sorry, I didn't understand..."

**Status:** ⏳ TO DO

---

## 🎁 FAZA 3: AI-Driven Customer Support (BONUS +1p) (2-3 ore)

### 🎯 Obiectiv
Integrează un LLM API (Google Gemini) pentru a răspunde la întrebări când nicio regulă nu se potrivește.

### 🔑 3.1 Setup Google Gemini API

**Obține API Key:**
- [ ] Mergi la https://makersuite.google.com/app/apikey
- [ ] Creează API key gratuit
- [ ] Salvează în `application.yml` sau environment variable

**Adaugă dependency în pom.xml:**
- [ ] Adaugă `spring-boot-starter-webflux` (pentru WebClient)
- [ ] SAU folosește `RestTemplate` (deja inclus)

### 🤖 3.2 Implementare AI Service

**GeminiService.java:**
- [ ] Metodă `generateResponse(String userMessage)`:
  - [ ] Creează prompt:
    ```
    You are a helpful customer support assistant for an Energy Management System.
    User question: {userMessage}
    Provide a concise, helpful response.
    ```
  - [ ] Apelează Gemini API:
    - [ ] Endpoint: `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent`
    - [ ] Header: `x-goog-api-key: YOUR_API_KEY`
    - [ ] Body: `{ "contents": [{ "parts": [{ "text": "..." }] }] }`
  - [ ] Parse răspuns JSON
  - [ ] Returnează text generat

**Exemplu Request Gemini:**
```json
{
  "contents": [{
    "parts": [{
      "text": "You are a helpful customer support assistant for an Energy Management System. User question: How can I reduce my energy consumption? Provide a concise, helpful response."
    }]
  }]
}
```

**Exemplu Response Gemini:**
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "To reduce energy consumption, you can: 1) Monitor your devices regularly..."
      }]
    }
  }]
}
```


### 🔄 3.3 Integrare AI în RuleBasedChatService

**Modificări în RuleBasedChatService.java:**
- [ ] Inject `GeminiService`
- [ ] Modifică `processMessage()`:
  ```java
  public ChatResponse processMessage(String message) {
      // 1. Încearcă regulile
      for (ChatRule rule : rules) {
          if (rule.matches(message)) {
              return new ChatResponse(
                  rule.getResponse(message),
                  true,  // isRuleBased
                  rule.getClass().getSimpleName()
              );
          }
      }
      
      // 2. Dacă nicio regulă nu se potrivește, folosește AI
      try {
          String aiResponse = geminiService.generateResponse(message);
          return new ChatResponse(
              aiResponse,
              false,  // isRuleBased = false (AI)
              "GeminiAI"
          );
      } catch (Exception e) {
          log.error("AI service failed", e);
          return new ChatResponse(
              "I'm sorry, I couldn't process your request. Please try again.",
              false,
              "Error"
          );
      }
  }
  ```

### ⚙️ 3.4 Configurare

**application.yml:**
```yaml
gemini:
  api-key: ${GEMINI_API_KEY:your-api-key-here}
  api-url: https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
  timeout: 10000  # 10 seconds
```

**Environment Variable în docker-compose.yml:**
- [ ] Adaugă în service `customer-support-microservice`:
  ```yaml
  environment:
    GEMINI_API_KEY: your-actual-api-key
  ```

### 🎨 3.5 Frontend - AI Badge

**Modificări în `app/dashboard/chat/page.tsx`:**
- [ ] Afișează badge diferit pentru AI:
  - [ ] Rule-based: Badge verde "Rule"
  - [ ] AI: Badge albastru "AI" cu icon ✨
- [ ] Adaugă loading indicator când așteaptă răspuns AI (mai lent)


### ✅ 3.6 Testing FAZA 3

**Test 1: AI Fallback**
- [ ] Trimite mesaj care NU se potrivește cu nicio regulă:
  ```bash
  curl -X POST http://localhost/api/chat/message \
    -H "Content-Type: application/json" \
    -d '{"message": "What are the best practices for reducing energy waste in smart homes?"}'
  ```
- [ ] Verifică răspuns generat de AI (nu mesaj default)
- [ ] Verifică `isRuleBased: false` în response

**Test 2: Rule Priority**
- [ ] Trimite mesaj care se potrivește cu regulă: "hello"
- [ ] Verifică că folosește regula (nu AI)
- [ ] Verifică `isRuleBased: true`

**Test 3: Frontend AI Badge**
- [ ] Deschide chat în frontend
- [ ] Trimite întrebare complexă: "How can I optimize my energy usage?"
- [ ] Verifică badge "AI" ✨
- [ ] Verifică loading indicator apare

**Test 4: API Key Invalid**
- [ ] Setează API key invalid în environment
- [ ] Trimite mesaj fără match
- [ ] Verifică error handling (mesaj default)

**Status:** ⏳ TO DO (BONUS +1p)

---

## 🎁 FAZA 4: Client-to-Admin Chat via WebSocket (BONUS +2p) (3-4 ore)

### 🎯 Obiectiv
Implementează chat bidirectional în timp real între client și admin folosind WebSocket Microservice.

### 🏗️ 4.1 Arhitectură

**Flow:**
```
Client (Frontend) 
  ↕ WebSocket
WebSocket Microservice
  ↕ RabbitMQ (chat_queue)
Customer Support Microservice
  ↕ RabbitMQ (chat_queue)
WebSocket Microservice
  ↕ WebSocket
Admin (Frontend)
```

### 🔧 4.2 Modificări WebSocket Microservice

**ChatWebSocketHandler.java (NOU):**
- [ ] Extinde `TextWebSocketHandler`
- [ ] Map<String, WebSocketSession> userSessions (userId → session)
- [ ] Map<String, WebSocketSession> adminSessions (adminId → session)
- [ ] Metodă `afterConnectionEstablished()`:
  - [ ] Extrage userId/adminId din query params
  - [ ] Salvează în map corespunzător
- [ ] Metodă `handleTextMessage()`:
  - [ ] Primește mesaj de la client/admin
  - [ ] Publică în RabbitMQ `chat_queue`
- [ ] Metodă `sendToUser(userId, message)` - trimite la client specific
- [ ] Metodă `sendToAdmin(adminId, message)` - trimite la admin specific

**WebSocketConfig.java:**
- [ ] Adaugă handler pentru `/ws/chat`
- [ ] Permite query params: `?userId=...` sau `?adminId=...`

**RabbitMQConfig.java:**
- [ ] Declară queue: `chat_queue`

**ChatMessageListener.java (NOU):**
- [ ] `@RabbitListener(queues = "chat_queue")`
- [ ] Primește mesaj cu: fromUserId, toUserId, message, timestamp, isAdmin
- [ ] Dacă `isAdmin = false` (mesaj de la client):
  - [ ] Trimite la admin via `ChatWebSocketHandler.sendToAdmin()`
- [ ] Dacă `isAdmin = true` (mesaj de la admin):
  - [ ] Trimite la client via `ChatWebSocketHandler.sendToUser()`


### 💬 4.3 Modificări Customer Support Microservice

**ChatController.java:**
- [ ] `POST /api/chat/send` - Trimite mesaj în chat
  - [ ] Request: `{ "fromUserId": "...", "toUserId": "...", "message": "...", "isAdmin": false }`
  - [ ] Publică în RabbitMQ `chat_queue`
  - [ ] Response: `{ "success": true, "timestamp": "..." }`

- [ ] `GET /api/chat/history/{userId}` - Istoric conversație
  - [ ] Returnează ultimele 50 mesaje pentru userId
  - [ ] Salvate în DB (opțional, sau in-memory)

**ChatMessage.java (Entity - opțional):**
- [ ] Long id
- [ ] String fromUserId
- [ ] String toUserId
- [ ] String message
- [ ] Boolean isAdmin
- [ ] LocalDateTime timestamp

**ChatRepository.java (opțional):**
- [ ] Dacă vrei persistență, creează repository JPA
- [ ] Altfel, folosește in-memory Map

### 🎨 4.4 Frontend - Client Chat Interface

**Modificări în `app/dashboard/chat/page.tsx`:**
- [ ] Adaugă toggle: "Bot Mode" vs "Admin Mode"
- [ ] **Bot Mode** (existent):
  - [ ] Folosește API `/api/chat/message` (rule-based + AI)
  - [ ] Răspuns instant
- [ ] **Admin Mode** (NOU):
  - [ ] Conectare WebSocket: `ws://localhost:8085/ws/chat?userId={currentUserId}`
  - [ ] Listener pentru mesaje de la admin
  - [ ] Buton "Send to Admin"
  - [ ] Afișează "Waiting for admin response..." dacă nu răspunde

**useWebSocketChat.ts (Custom Hook):**
- [ ] Conectare la `/ws/chat?userId=...`
- [ ] Metodă `sendMessage(message)`
- [ ] Callback `onMessageReceived(message)`
- [ ] Auto-reconnect


### 👨‍💼 4.5 Frontend - Admin Chat Interface

**Creează `app/dashboard/admin-chat/page.tsx` (NOU):**
- [ ] Disponibil doar pentru ADMIN role
- [ ] Lista utilizatori activi (care au trimis mesaje)
- [ ] Selectează user pentru a vedea conversația
- [ ] Chat interface similar cu client
- [ ] Conectare WebSocket: `ws://localhost:8085/ws/chat?adminId={currentAdminId}`
- [ ] Buton "Send to Client"

**Componente:**
- [ ] Sidebar cu lista useri
- [ ] Chat area pentru conversație selectată
- [ ] Badge pentru mesaje necitite

**Navigation:**
- [ ] Adaugă link în dashboard menu (doar pentru admin):
  - [ ] "Admin Chat" cu icon 💬

### 🔐 4.6 Security & Authentication

**WebSocket Authentication:**
- [ ] Extrage token JWT din query params sau headers
- [ ] Validează token înainte de a accepta conexiunea
- [ ] Asociază userId/adminId cu sesiunea

**Authorization:**
- [ ] Verifică că doar ADMIN poate accesa `/dashboard/admin-chat`
- [ ] Verifică că CLIENT poate trimite doar la admin (nu la alți clienți)

### ✅ 4.7 Testing FAZA 4

**Test 1: Client → Admin**
- [ ] Login ca CLIENT
- [ ] Deschide Chat page
- [ ] Switch la "Admin Mode"
- [ ] Trimite mesaj: "I need help with my device"
- [ ] Verifică în RabbitMQ că mesajul a fost publicat

**Test 2: Admin → Client**
- [ ] Login ca ADMIN (în alt browser/incognito)
- [ ] Deschide Admin Chat page
- [ ] Selectează clientul din listă
- [ ] Trimite răspuns: "How can I help you?"
- [ ] Verifică că clientul primește mesajul instant

**Test 3: Bidirectional Chat**
- [ ] Client trimite: "My device is not working"
- [ ] Admin răspunde: "Can you provide the device ID?"
- [ ] Client răspunde: "Device ID is 123-456"
- [ ] Admin răspunde: "I'll check it for you"
- [ ] Verifică că toate mesajele apar în ordine corectă

**Test 4: Multiple Clients**
- [ ] Deschide 2 clienți diferiți
- [ ] Ambii trimit mesaje la admin
- [ ] Admin răspunde la fiecare separat
- [ ] Verifică că mesajele nu se amestecă

**Status:** ⏳ TO DO (BONUS +2p)

---

## 📝 FAZA 5: Documentation & Deployment (2-3 ore)

### 📄 5.1 README.md Principal

**Actualizează `README.md` în root:**

**Secțiuni de adăugat:**

#### Assignment 3: Real-Time Communication & Customer Support
- [ ] Descriere generală Assignment 3
- [ ] Arhitectură nouă (cu WebSocket MS + Customer Support MS)
- [ ] Tehnologii noi: WebSockets, Google Gemini API

#### WebSocket Microservice
- [ ] Descriere funcționalitate
- [ ] Endpoints:
  - [ ] `/ws/notifications` - Notificări overconsumption
  - [ ] `/ws/chat` - Chat bidirectional (bonus)
- [ ] Integrare cu RabbitMQ
- [ ] Exemple de mesaje

#### Customer Support Microservice
- [ ] Descriere funcționalitate
- [ ] Rule-Based System (10+ reguli)
- [ ] AI-Driven Support (Gemini API) - bonus
- [ ] API Endpoints:
  - [ ] `POST /api/chat/message` - Trimite mesaj
  - [ ] `GET /api/chat/rules` - Lista reguli
  - [ ] `POST /api/chat/send` - Chat admin (bonus)
  - [ ] `GET /api/chat/history/{userId}` - Istoric (bonus)

#### Overconsumption Alerts
- [ ] Flow complet:
  1. Simulator trimite date → Monitoring MS
  2. Monitoring MS detectează overconsumption
  3. Publică în RabbitMQ `overconsumption_queue`
  4. WebSocket MS primește și broadcast la frontend
  5. Frontend afișează notificare toast
- [ ] Screenshot cu notificare

#### Chat System
- [ ] Bot Mode: Rule-based + AI
- [ ] Admin Mode: Chat în timp real (bonus)
- [ ] Screenshot cu chat interface


#### Quick Start Guide (Assignment 3)
- [ ] Prerequisites:
  - [ ] Docker & Docker Compose
  - [ ] Python 3.x (pentru simulator)
  - [ ] Google Gemini API Key (pentru AI support - bonus)
- [ ] Build & Run:
  ```bash
  # Build all services
  docker-compose build
  
  # Start all services
  docker-compose up -d
  
  # Check status
  docker ps
  
  # View logs
  docker logs websocket-microservice-app
  docker logs customer-support-microservice-app
  ```
- [ ] Accesare servicii:
  - [ ] Frontend: http://localhost:3000
  - [ ] WebSocket: ws://localhost:8085/ws/notifications
  - [ ] Chat API: http://localhost/api/chat
  - [ ] RabbitMQ UI: http://localhost:15672

#### Testing Assignment 3
- [ ] **Test 1: Overconsumption Alert**
  1. Creează device cu max 5.0 kWh
  2. Rulează simulator (6 măsurători)
  3. Verifică notificare în frontend
  
- [ ] **Test 2: Rule-Based Chat**
  1. Deschide Chat page
  2. Trimite: "hello"
  3. Verifică răspuns instant
  
- [ ] **Test 3: AI Chat** (bonus)
  1. Trimite întrebare complexă
  2. Verifică răspuns AI
  
- [ ] **Test 4: Admin Chat** (bonus)
  1. Login ca client, trimite mesaj
  2. Login ca admin, răspunde
  3. Verifică chat bidirectional

#### Troubleshooting
- [ ] **WebSocket connection failed**
  - Verifică că WebSocket MS rulează
  - Verifică CORS settings
  - Verifică firewall/antivirus
  
- [ ] **Chat nu răspunde**
  - Verifică Customer Support MS logs
  - Verifică că regulile sunt încărcate
  - Verifică API key Gemini (pentru AI)
  
- [ ] **Notificări nu apar**
  - Verifică RabbitMQ queue `overconsumption_queue`
  - Verifică Monitoring MS detectează overconsumption
  - Verifică WebSocket connection în browser console


### 📐 5.2 UML Deployment Diagram

**Creează `deployment-diagram-assignment3.puml`:**

**Componente de inclus:**
- [ ] **Frontend** (Next.js)
  - [ ] Port 3000
  - [ ] WebSocket connections
  
- [ ] **Traefik** (Reverse Proxy)
  - [ ] Port 80
  - [ ] Routes: /api/*, /ws/*
  
- [ ] **Microservices:**
  - [ ] Auth MS (8083)
  - [ ] User MS (8081)
  - [ ] Device MS (8082)
  - [ ] Monitoring MS (8084)
  - [ ] **WebSocket MS (8085)** ← NOU
  - [ ] **Customer Support MS (8086)** ← NOU
  
- [ ] **RabbitMQ** (5672, 15672)
  - [ ] Queues:
    - [ ] sync_queue
    - [ ] sensor_data_queue
    - [ ] **overconsumption_queue** ← NOU
    - [ ] **chat_queue** ← NOU (bonus)
  
- [ ] **MySQL Databases:**
  - [ ] mysql-auth (3309)
  - [ ] mysql-users (3307)
  - [ ] mysql-devices (3308)
  - [ ] mysql-monitoring (3310)
  
- [ ] **Device Simulator** (Python)
  - [ ] Publică în sensor_data_queue
  
- [ ] **External Services:**
  - [ ] Google Gemini API (bonus)

**Săgeți (comunicare):**
- [ ] Frontend ↔ Traefik (HTTP/WebSocket)
- [ ] Traefik → Microservices (HTTP)
- [ ] Microservices ↔ RabbitMQ (AMQP)
- [ ] Microservices → MySQL (JDBC)
- [ ] WebSocket MS ↔ Frontend (WebSocket)
- [ ] Customer Support MS → Gemini API (HTTPS) - bonus
- [ ] Device Simulator → RabbitMQ (AMQP)

**Generare PNG:**
- [ ] Folosește http://www.plantuml.com/plantuml/uml/
- [ ] SAU instalează PlantUML local
- [ ] Generează `deployment-diagram-assignment3.png`
- [ ] Include în README.md


### 📚 5.3 Răspunsuri Întrebări Teoretice

**Creează secțiune în README.md: "Assignment 3 - Theoretical Questions"**

#### 1. Load Balancing
- [ ] **Ce este Load Balancing?**
  - Definiție: Distribuirea traficului între multiple servere
  - Scopuri: Scalabilitate, High Availability, Performance
  
- [ ] **Tipuri de Load Balancing:**
  - Round Robin: Distribuie secvențial
  - Least Connections: Trimite la serverul cu cele mai puține conexiuni
  - IP Hash: Bazat pe IP-ul clientului
  - Weighted: Bazat pe capacitatea serverului
  
- [ ] **Load Balancing în Assignment 3:**
  - Traefik ca reverse proxy
  - Poate scala microserviciile cu Docker Swarm
  - Exemplu: Multiple replici Monitoring MS

#### 2. Docker Swarm
- [ ] **Ce este Docker Swarm?**
  - Orchestration tool pentru Docker containers
  - Permite deployment cu replici
  - Load balancing automat între replici
  
- [ ] **Concepte cheie:**
  - Manager nodes: Controlează cluster-ul
  - Worker nodes: Rulează containerele
  - Services: Definesc aplicațiile
  - Replicas: Număr de instanțe
  
- [ ] **Comenzi Docker Swarm:**
  ```bash
  # Initialize swarm
  docker swarm init
  
  # Deploy stack
  docker stack deploy -c docker-compose.yml energy-stack
  
  # Scale service
  docker service scale energy-stack_monitoring-microservice=3
  
  # List services
  docker service ls
  ```
  
- [ ] **Avantaje:**
  - Auto-healing: Repornește containere căzute
  - Rolling updates: Update fără downtime
  - Load balancing: Distribuie trafic automat


#### 3. Sockets vs WebSockets
- [ ] **Sockets (TCP/IP):**
  - Low-level network communication
  - Bidirectional, persistent connection
  - Folosit pentru: SSH, FTP, HTTP
  
- [ ] **WebSockets:**
  - Protocol peste HTTP (upgrade de la HTTP)
  - Full-duplex communication
  - Persistent connection între browser și server
  - Handshake: HTTP → WebSocket upgrade
  
- [ ] **Diferențe:**
  | Feature | HTTP | WebSocket |
  |---------|------|-----------|
  | Connection | Request-Response | Persistent |
  | Direction | Unidirectional | Bidirectional |
  | Overhead | High (headers) | Low (după handshake) |
  | Use case | REST APIs | Real-time apps |
  
- [ ] **WebSocket în Assignment 3:**
  - Notificări overconsumption în timp real
  - Chat bidirectional client-admin (bonus)
  - Endpoint: `ws://localhost:8085/ws/notifications`

#### 4. Security in Web Applications
- [ ] **Authentication vs Authorization:**
  - Authentication: "Cine ești?" (Login)
  - Authorization: "Ce poți face?" (Permissions)
  
- [ ] **JWT (JSON Web Token):**
  - Stateless authentication
  - Token format: `header.payload.signature`
  - Stocat în localStorage/sessionStorage
  - Trimis în header: `Authorization: Bearer <token>`
  
- [ ] **Security Best Practices:**
  - HTTPS pentru toate comunicările
  - Password hashing (BCrypt)
  - Input validation & sanitization
  - CORS configuration
  - Rate limiting
  - SQL injection prevention (Prepared Statements)
  - XSS prevention (Escape user input)
  
- [ ] **Security în Assignment 3:**
  - JWT pentru autentificare
  - BCrypt pentru parole
  - CORS pentru WebSocket
  - Traefik ca reverse proxy (security layer)
  - Role-based access (ADMIN vs CLIENT)


### 🐳 5.4 Docker Compose Final

**Verifică `docker-compose.yml` conține:**
- [ ] Toate serviciile existente (Assignment 1 & 2)
- [ ] **websocket-microservice** (NOU):
  - [ ] Port: 8085:8085
  - [ ] Depends on: rabbitmq
  - [ ] Environment: RABBITMQ_HOST, RABBITMQ_PORT
  - [ ] Traefik labels: `/ws/*`
  - [ ] Network: energy-network
  
- [ ] **customer-support-microservice** (NOU):
  - [ ] Port: 8086:8086
  - [ ] Depends on: rabbitmq
  - [ ] Environment: RABBITMQ_HOST, GEMINI_API_KEY (bonus)
  - [ ] Traefik labels: `/api/chat/*`
  - [ ] Network: energy-network

**Health checks:**
- [ ] Toate serviciile au healthcheck configurat
- [ ] Verifică că serviciile pornesc în ordine corectă

### 🧹 5.5 Cleanup & Final Checks

**Code Cleanup:**
- [ ] Șterge comentarii inutile
- [ ] Șterge debug prints excesive
- [ ] Șterge fișiere temporare
- [ ] Verifică `.gitignore`:
  - [ ] `target/` (Java)
  - [ ] `node_modules/` (Frontend)
  - [ ] `.env` (API keys)
  - [ ] `*.log`

**Testing Final:**
- [ ] Test complet: `docker-compose down && docker-compose up -d --build`
- [ ] Verifică toate containerele pornesc: `docker ps`
- [ ] Verifică health checks: `docker ps` (healthy status)
- [ ] Test frontend: http://localhost:3000
- [ ] Test toate funcționalitățile:
  - [ ] Login
  - [ ] Create user/device
  - [ ] Monitoring cu grafice
  - [ ] Overconsumption alert
  - [ ] Chat bot (rule-based)
  - [ ] Chat AI (bonus)
  - [ ] Admin chat (bonus)


### 📤 5.6 GitLab Delivery

**Repository Setup:**
- [ ] Verifică branch-ul corect (main/master)
- [ ] Verifică că toate fișierele sunt committed
- [ ] Verifică că `.gitignore` funcționează corect

**Commit Final:**
```bash
git add .
git commit -m "Assignment 3 - WebSockets and Customer Support - Complete implementation"
git push origin main
```

**Share Repository:**
- [ ] Mergi la Settings → Members
- [ ] Invite member: `utcn_dsrl`
- [ ] Role: Reporter (read-only)
- [ ] Verifică că user-ul are acces

**Checklist Final:**
- [ ] README.md complet cu Assignment 3
- [ ] Deployment Diagram (PNG) inclus
- [ ] Toate serviciile funcționează
- [ ] WebSocket notificări funcționează
- [ ] Chat bot cu 10+ reguli funcționează
- [ ] AI support funcționează (bonus)
- [ ] Admin chat funcționează (bonus)
- [ ] Răspunsuri la întrebări teoretice
- [ ] Repository shared cu utcn_dsrl

**Status:** ⏳ TO DO

---

## 📊 Progress Tracking

### Rezumat Faze

| Fază | Descriere | Timp Estimat | Puncte | Status |
|------|-----------|--------------|--------|--------|
| **FAZA 1** | WebSocket MS + Overconsumption Alerts | 2-3 ore | 2.5p | ⏳ TODO |
| **FAZA 2** | Rule-Based Chatbot (10+ reguli) | 2-3 ore | 2.5p | ⏳ TODO |
| **FAZA 3** | AI-Driven Support (Gemini) | 2-3 ore | +1p | ⏳ TODO |
| **FAZA 4** | Client-to-Admin Chat (WebSocket) | 3-4 ore | +2p | ⏳ TODO |
| **FAZA 5** | Documentation & Deployment | 2-3 ore | - | ⏳ TODO |

**Total Timp Estimat:**
- Obligatoriu (FAZA 1 + 2 + 5): **6-9 ore**
- Cu bonus (toate fazele): **11-16 ore**

**Total Puncte Posibile:**
- Obligatoriu: **5p**
- Bonus: **+3p**
- **TOTAL: 8p**

---

## 🎯 Recomandări Implementare

### Ordinea Recomandată:

1. **Start cu FAZA 1** (WebSocket + Overconsumption)
   - Este obligatoriu (2.5p)
   - Fundație pentru FAZA 4 (bonus)
   - Relativ simplu de implementat

2. **Continuă cu FAZA 2** (Rule-Based Chatbot)
   - Este obligatoriu (2.5p)
   - Fundație pentru FAZA 3 (bonus)
   - Poate fi testat independent

3. **Apoi FAZA 3** (AI Support) - BONUS
   - Doar dacă ai timp
   - Ușor de adăugat peste FAZA 2
   - +1p bonus

4. **Apoi FAZA 4** (Admin Chat) - BONUS
   - Cel mai complex
   - Necesită FAZA 1 completă
   - +2p bonus (cel mai valoros)

5. **Final FAZA 5** (Documentation)
   - Obligatoriu
   - Fă-o la final când totul funcționează

### Prioritizare dacă ai timp limitat:

**Minim pentru nota 5 (5p):**
- ✅ FAZA 1: WebSocket + Overconsumption
- ✅ FAZA 2: Rule-Based Chatbot (10 reguli)
- ✅ FAZA 5: README + Deployment Diagram

**Pentru nota maximă (8p):**
- ✅ Toate fazele (1-5)
- ✅ Ambele bonus-uri (AI + Admin Chat)

---

## 📝 Notes & Tips

### Tips Implementare:

1. **WebSocket Testing:**
   - Folosește browser console pentru debugging
   - Verifică Network tab → WS pentru conexiuni
   - Testează reconnect manual (oprește/pornește server)

2. **RabbitMQ:**
   - Verifică mereu Management UI pentru queue-uri
   - Monitorizează mesajele publicate/consumate
   - Folosește `durable=true` pentru persistență

3. **Chat Rules:**
   - Fă regulile case-insensitive
   - Folosește regex pentru matching mai flexibil
   - Testează cu variații de întrebări

4. **AI Integration:**
   - Gemini API are free tier generos
   - Timeout la 10 secunde pentru răspunsuri
   - Fallback la mesaj default dacă API fail

5. **Docker:**
   - Rebuild după modificări: `docker-compose up -d --build`
   - Verifică logs: `docker logs -f <container-name>`
   - Curăță volumes dacă ai probleme: `docker-compose down -v`

### Common Pitfalls:

- ❌ Uiți să adaugi CORS pentru WebSocket
- ❌ Nu verifici că RabbitMQ e pornit înainte de microservicii
- ❌ Hardcodezi API keys în cod (folosește environment variables)
- ❌ Nu testezi reconnect WebSocket
- ❌ Uiți să adaugi Traefik labels pentru noi servicii

---

## 🚀 Quick Start Commands

```bash
# Build toate serviciile
docker-compose build

# Pornește toate serviciile
docker-compose up -d

# Verifică status
docker ps

# Logs pentru debugging
docker logs -f websocket-microservice-app
docker logs -f customer-support-microservice-app
docker logs -f monitoring-microservice-app

# Restart un service
docker-compose restart websocket-microservice

# Oprește toate
docker-compose down

# Curăță tot (inclusiv volumes)
docker-compose down -v

# Rebuild și restart
docker-compose up -d --build
```

---

## ✅ Final Checklist

Înainte de predare, verifică:

- [ ] Toate serviciile pornesc fără erori
- [ ] Frontend accesibil la http://localhost:3000
- [ ] WebSocket notificări funcționează
- [ ] Chat bot răspunde la 10+ întrebări
- [ ] AI support funcționează (dacă ai implementat)
- [ ] Admin chat funcționează (dacă ai implementat)
- [ ] README.md complet și actualizat
- [ ] Deployment diagram generat și inclus
- [ ] Răspunsuri la întrebări teoretice
- [ ] Cod curat (fără comentarii inutile)
- [ ] .gitignore corect configurat
- [ ] Repository shared cu utcn_dsrl
- [ ] Toate commit-urile pushed pe GitLab

---

**Succes la implementare! 🚀**

**Dacă ai întrebări sau probleme, întreabă-mă oricând!**
