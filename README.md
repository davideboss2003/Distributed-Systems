# Energy Management System - Distributed Systems Assignment

## Project Overview

This is a distributed Energy Management System built with microservices architecture. The system allows authenticated users to manage smart energy metering devices, monitor real-time energy consumption, receive overconsumption alerts via WebSocket, and interact with an AI-powered customer support chatbot.

![Deployment Architecture - Assignment 3](deployment%20diagram%20Assigment3.png)

### Key Features

- **Role-Based Access Control**: Admin and Client roles with different permissions
- **Microservices Architecture**: Loosely coupled services for User, Device, Authentication, Monitoring, WebSocket, and Customer Support
- **Real-Time Monitoring**: Live energy consumption tracking with data aggregation
- **WebSocket Notifications**: Real-time overconsumption alerts pushed to clients
- **AI Customer Support**: Intelligent chatbot with rule-based and Google Gemini AI responses
- **Live Admin Chat**: Real-time bidirectional chat between clients and administrators
- **Message-Oriented Middleware**: RabbitMQ for asynchronous communication between services
- **Device Simulation**: Python simulator for generating realistic sensor data
- **Reverse Proxy**: Traefik API Gateway for request routing
- **Containerized Deployment**: Full Docker-based deployment
- **RESTful APIs**: All microservices expose REST endpoints
- **JWT Authentication**: Secure token-based authentication
- **Modern Frontend**: Next.js with TypeScript, real-time charts, and Wall-E mascot

---

## System Architecture

### Components

1. **Frontend Application** (Next.js 15 + TypeScript)
   - Admin dashboard for managing users and devices
   - Client dashboard for viewing assigned devices
   - Real-time energy consumption monitoring with interactive charts
   - WebSocket-based notification system for overconsumption alerts
   - AI Customer Support chat interface with Wall-E mascot
   - Admin chat interface for real-time client communication
   - Date-based filtering and visualization

2. **Traefik Reverse Proxy & API Gateway**
   - Entry point for all HTTP requests (Port 80)
   - Routes requests to appropriate microservices
   - Load balancing and service discovery

3. **Authentication Service** (Spring Boot - Port 8083)
   - User login and JWT token generation
   - Token validation
   - BCrypt password hashing

4. **User Management Microservice** (Spring Boot - Port 8081)
   - CRUD operations for user accounts
   - Publishes user events to RabbitMQ sync queue
   - Password encryption with BCrypt

5. **Device Management Microservice** (Spring Boot - Port 8082)
   - CRUD operations for devices
   - Device-to-user assignment
   - Publishes device events to RabbitMQ sync queue

6. **Monitoring Microservice** (Spring Boot - Port 8084)
   - Receives device and user sync events from RabbitMQ
   - Collects real-time sensor data from devices
   - Aggregates measurements into hourly consumption records
   - Detects overconsumption and publishes alerts to RabbitMQ
   - Provides REST API for consumption data queries
   - Stores data in separate MySQL database

7. **WebSocket Microservice** (Spring Boot - Port 8085) **[NEW - Assignment 3]**
   - Real-time WebSocket connections for notifications
   - Overconsumption alert delivery to connected clients
   - Live chat functionality between clients and admins
   - RabbitMQ listener for event-driven notifications
   - Session management for connected users

8. **Customer Support Microservice** (Spring Boot - Port 8086) **[NEW - Assignment 3]**
   - AI-powered chatbot with Google Gemini integration
   - Rule-based response system for common queries
   - Hybrid approach: rules first, AI fallback
   - Response type badges (Rule-Based vs AI)
   - Energy management domain expertise

9. **Device Simulator** (Python)
   - Simulates real IoT devices sending sensor data
   - Generates realistic energy consumption patterns
   - Publishes measurements to RabbitMQ every 10 seconds
   - Configurable device ID and measurement intervals

10. **RabbitMQ Message Broker** (Port 5672, Management: 15672)
    - `sync_queue`: Synchronizes users and devices across microservices
    - `sensor_data_queue`: Receives real-time measurements from devices
    - `overconsumption.queue`: Overconsumption alert events **[NEW]**
    - `chat.messages.queue`: Real-time chat messages **[NEW]**
    - Ensures reliable message delivery between services

11. **MySQL Databases**
    - `energy_auth_db` (Port 3309): Authentication credentials
    - `energy_users_db` (Port 3307): User profiles
    - `energy_devices_db` (Port 3308): Devices
    - `energy_monitoring_db` (Port 3310): Consumption data

---

## Assignment 3: WebSocket Notifications & AI Customer Support

### Overview

Assignment 3 extends the system with real-time WebSocket notifications for overconsumption alerts and an AI-powered customer support chatbot with Google Gemini integration.

### Part 1: WebSocket Microservice

**Purpose**: Provides real-time bidirectional communication for notifications and live chat.

**Features**:
- WebSocket endpoint for overconsumption notifications (`/ws/notifications`)
- WebSocket endpoint for live chat (`/ws/chat`)
- RabbitMQ listener for overconsumption events
- Session management for user connections
- Automatic reconnection handling

**WebSocket Endpoints**:
| Endpoint | Purpose |
|----------|---------|
| `ws://localhost:8085/ws/notifications?userId={id}` | Overconsumption alerts |
| `ws://localhost:8085/ws/chat?userId={id}&role={role}` | Live chat |

**Main Classes**:
- `NotificationWebSocketHandler.java`: Handles notification connections
- `ChatWebSocketHandler.java`: Handles chat connections
- `OverconsumptionListener.java`: Listens to RabbitMQ for alerts
- `ChatMessageListener.java`: Listens to RabbitMQ for chat messages

### Part 2: Customer Support Microservice

**Purpose**: AI-powered chatbot for customer support with hybrid rule-based and Gemini AI responses.

**Features**:
- Rule-based responses for common queries (greetings, help, billing, devices)
- Google Gemini AI integration for complex questions
- Response type badges to distinguish AI vs rule-based answers
- Energy management domain expertise
- Fallback mechanism when AI is unavailable

**Rule-Based Patterns**:
| Rule | Triggers | Example Response |
|------|----------|------------------|
| Greeting | "salut", "buna", "hello" | "Bună! Sunt asistentul virtual..." |
| Help | "ajutor", "help", "cum" | "Pot să te ajut cu..." |
| Billing | "factura", "plata", "cost" | "Pentru facturare..." |
| Devices | "dispozitiv", "device" | "Pentru dispozitive..." |
| Consumption | "consum", "energie" | "Pentru monitorizare consum..." |
| Farewell | "pa", "bye", "multumesc" | "Cu plăcere! La revedere!" |

**API Endpoints**:
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chat/message` | Send message to chatbot |
| GET | `/api/chat/health` | Health check |

**Request/Response**:
```json
// Request
POST /api/chat/message
{
  "userId": "user-123",
  "message": "Care este consumul meu?"
}

// Response
{
  "response": "Pentru a vedea consumul...",
  "responseType": "RULE_BASED",
  "timestamp": "2025-12-07T10:30:00"
}
```

### Part 3: Frontend Integration

**Notification System**:
- Real-time toast notifications for overconsumption
- WebSocket hook with auto-reconnect
- Visual alerts with device information

**Customer Support Chat**:
- Chat interface with Wall-E mascot
- Message bubbles with response type badges
- Typing indicators
- Chat history

**Admin Chat Interface**:
- Real-time client list
- Bidirectional messaging
- Message timestamps
- Connection status indicators

### Data Flow

**Overconsumption Notification Flow**:
```
Device Simulator sends measurement
→ Monitoring Microservice receives data
→ Detects overconsumption (value > maxHourlyConsumption)
→ Publishes to overconsumption.queue
→ WebSocket Microservice receives event
→ Pushes notification to connected client via WebSocket
→ Frontend displays toast notification
```

**Customer Support Chat Flow**:
```
User types message in chat
→ Frontend sends POST to /api/chat/message
→ Customer Support checks rule-based patterns
→ If match: returns rule-based response
→ If no match: sends to Gemini AI
→ Returns response with type badge
→ Frontend displays message with badge
```

**Live Admin Chat Flow**:
```
Client sends message via WebSocket
→ WebSocket Microservice receives message
→ Publishes to chat.messages.queue
→ Admin receives message in real-time
→ Admin responds via WebSocket
→ Client receives response instantly
```

---

## Technologies Used

### Backend
- **Java 17** with **Spring Boot 3.5.6**
- **Spring Data JPA** for ORM
- **Spring Security** for authentication
- **Spring AMQP** for RabbitMQ integration
- **Spring WebSocket** for real-time communication **[NEW]**
- **Google Gemini API** for AI responses **[NEW]**
- **JWT** for stateless authentication
- **MySQL 8.0** for data persistence
- **RabbitMQ 3.13** for message brokering
- **Maven** for dependency management

### Frontend
- **Next.js 15** - React framework
- **React 19** - UI library
- **TypeScript 5** - Type safety
- **Tailwind CSS 4** - Styling
- **Recharts** - Data visualization
- **WebSocket API** - Real-time notifications **[NEW]**
- **date-fns** - Date manipulation
- **SWR** - Data fetching

### Device Simulator
- **Python 3.x**
- **pika** - RabbitMQ client library

### Infrastructure
- **Docker** & **Docker Compose**
- **Traefik v2.10** - API Gateway
- **RabbitMQ 3.13** - Message Broker

---

## Prerequisites

- **Docker Desktop** (version 20.10+)
- **Docker Compose** (version 2.0+)
- **Python 3.x** (for device simulator)
- **Google Gemini API Key** (for AI chatbot)
- **8GB RAM minimum**
- **Ports Available**: 80, 3000, 3307-3310, 5672, 8080-8086, 15672

---

## Quick Start Guide

### Step 1: Clone and Build

```bash
git clone <repository-url>
cd energy-management-system
docker-compose build
```

### Step 2: Configure Environment

Create `.env` file in root directory:
```env
GEMINI_API_KEY=your-gemini-api-key-here
GEMINI_ENABLED=true
```

### Step 3: Start All Services

```bash
docker-compose up -d
```

Wait 30-60 seconds for all services to start.

### Step 4: Initialize Admin User

**Windows (CMD)**:
```cmd
cd auth-microservice
docker-init-admin.bat
```

**Windows (PowerShell)**:
```powershell
cd auth-microservice
.\docker-init-admin.ps1
```

### Step 5: Access the Application

Open browser: **http://localhost:3000**

Login with:
- **Email**: `admin@energy.ro`
- **Password**: `admin123`

### Step 6: Test Features

**Test Overconsumption Notifications**:
1. Create a device with low `maxHourlyConsumption` (e.g., 1.0)
2. Run device simulator with that device ID
3. Watch for toast notifications when consumption exceeds limit

**Test AI Customer Support**:
1. Go to Customer Support page
2. Type "Salut" - get rule-based response
3. Type complex question - get AI response
4. Notice the response type badges

**Test Admin Chat**:
1. Login as admin, go to Admin Chat
2. Open another browser as client
3. Client sends message
4. Admin receives and responds in real-time

---

## Service Ports

| Service | Internal Port | External Port | URL |
|---------|--------------|---------------|-----|
| Frontend | 3000 | 3000 | http://localhost:3000 |
| Traefik | 80 | 80 | http://localhost |
| Traefik Dashboard | 8080 | 8080 | http://localhost:8080 |
| User Service | 8081 | - | /api/users |
| Device Service | 8082 | - | /api/devices |
| Auth Service | 8083 | - | /api/auth |
| Monitoring Service | 8084 | - | /api/monitoring |
| WebSocket Service | 8085 | 8085 | ws://localhost:8085 |
| Customer Support | 8086 | 8086 | /api/chat |
| RabbitMQ | 5672 | 5672 | amqp://localhost |
| RabbitMQ Management | 15672 | 15672 | http://localhost:15672 |
| MySQL Users | 3306 | 3307 | - |
| MySQL Devices | 3306 | 3308 | - |
| MySQL Auth | 3306 | 3309 | - |
| MySQL Monitoring | 3306 | 3310 | - |

---

## Project Structure

```
energy-management-system/
├── auth-microservice/              # Authentication Service
├── user-microservice/              # User Management
├── device-microservice/            # Device Management
├── monitoring-microservice/        # Monitoring Service
│   └── src/main/java/com/energy/monitoring/
│       ├── config/                 # RabbitMQ configuration
│       ├── controller/             # REST controllers
│       ├── dto/                    # Data transfer objects
│       ├── entity/                 # JPA entities
│       ├── listener/               # RabbitMQ listeners
│       ├── repository/             # Data repositories
│       └── service/                # Business logic
├── websocket-microservice/         # WebSocket Service [NEW]
│   └── src/main/java/com/energy/websocket/
│       ├── config/                 # WebSocket & RabbitMQ config
│       ├── dto/                    # Message DTOs
│       ├── handler/                # WebSocket handlers
│       │   ├── NotificationWebSocketHandler.java
│       │   └── ChatWebSocketHandler.java
│       └── listener/               # RabbitMQ listeners
│           ├── OverconsumptionListener.java
│           └── ChatMessageListener.java
├── customer-support-microservice/  # AI Customer Support [NEW]
│   └── src/main/java/com/energy/support/
│       ├── controller/             # REST controllers
│       ├── service/                # Business logic
│       │   ├── GeminiService.java
│       │   └── RuleBasedChatService.java
│       └── rules/                  # Chat rules
│           ├── GreetingRule.java
│           ├── HelpRule.java
│           ├── BillingRule.java
│           └── ...
├── device-simulator/               # Python Device Simulator
│   ├── simulator.py
│   ├── config.json
│   └── README.md
├── frontend/                       # Next.js Frontend
│   ├── app/
│   │   └── dashboard/
│   │       ├── monitoring/         # Consumption charts
│   │       ├── customer-support/   # AI chatbot [NEW]
│   │       └── admin-chat/         # Live chat [NEW]
│   ├── components/
│   │   └── wall-e-mascot.tsx       # Wall-E mascot [NEW]
│   └── hooks/
│       └── use-websocket-chat.ts   # WebSocket hook [NEW]
├── docker-compose.yml              # Docker configuration
├── deployment diagram Assigment3.png # Architecture diagram
└── README.md                       # This file
```

---

## Key Features Implemented

### Assignment 1
✅ Microservices architecture
✅ User management with roles
✅ Device management
✅ JWT authentication
✅ REST APIs
✅ Docker deployment
✅ Frontend dashboard

### Assignment 2
✅ Monitoring microservice
✅ RabbitMQ message broker
✅ Device simulator (Python)
✅ Real-time data collection
✅ Data aggregation (6 measurements → 1 hour)
✅ Consumption visualization
✅ Interactive charts
✅ Date-based filtering

### Assignment 3
✅ WebSocket microservice for real-time communication
✅ Overconsumption notifications via WebSocket
✅ Customer Support microservice with AI
✅ Google Gemini API integration
✅ Rule-based chat responses
✅ Response type badges (AI vs Rule-Based)
✅ Live admin-client chat
✅ Wall-E mascot in chat interface
✅ Auto-reconnect WebSocket handling

---

## Troubleshooting

### WebSocket Connection Issues

**Problem**: WebSocket not connecting

**Solution**:
```bash
# Check WebSocket service is running
docker ps | grep websocket

# Check logs
docker logs websocket-microservice-app

# Test connection
# Open browser console and run:
# new WebSocket('ws://localhost:8085/ws/notifications?userId=test')
```

### Gemini AI Not Responding

**Problem**: AI responses not working

**Solution**:
1. Check `GEMINI_API_KEY` is set in `.env`
2. Verify API key is valid
3. Check customer-support logs:
   ```bash
   docker logs customer-support-microservice-app
   ```
4. System falls back to rule-based responses if AI fails

### Notifications Not Appearing

**Problem**: No overconsumption notifications

**Solution**:
1. Verify device `maxHourlyConsumption` is lower than simulated values
2. Check monitoring service detects overconsumption:
   ```bash
   docker logs monitoring-microservice-app | grep "overconsumption"
   ```
3. Check WebSocket connection in browser console
4. Verify RabbitMQ queue has messages

---

## Stopping the System

```bash
# Stop all services
docker-compose down

# Stop and remove all data
docker-compose down -v

# Restart specific service
docker-compose restart websocket-microservice
docker-compose restart customer-support-microservice
```

---

## Authors

**Technical University of Cluj-Napoca**
**Distributed Systems 2025**

---

## License

This project is part of an academic assignment for the Distributed Systems course.
