# Energy Management System - Distributed Systems Assignment

## Project Overview

This is a distributed Energy Management System built with microservices architecture. The system allows authenticated users to manage smart energy metering devices and monitor their real-time energy consumption.

![Deployment Architecture - Assignment 2](deployment%20diagram%20assig2.png)

### Key Features

- **Role-Based Access Control**: Admin and Client roles with different permissions
- **Microservices Architecture**: Loosely coupled services for User, Device, Authentication, and Monitoring
- **Real-Time Monitoring**: Live energy consumption tracking with data aggregation
- **Message-Oriented Middleware**: RabbitMQ for asynchronous communication between services
- **Device Simulation**: Python simulator for generating realistic sensor data
- **Reverse Proxy**: Traefik API Gateway for request routing
- **Containerized Deployment**: Full Docker-based deployment
- **RESTful APIs**: All microservices expose REST endpoints
- **JWT Authentication**: Secure token-based authentication
- **Modern Frontend**: Next.js with TypeScript and real-time charts

---

## System Architecture

### Components

1. **Frontend Application** (Next.js 15 + TypeScript)
   - Admin dashboard for managing users and devices
   - Client dashboard for viewing assigned devices
   - Real-time energy consumption monitoring with interactive charts
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

6. **Monitoring Microservice** (Spring Boot - Port 8084) **[NEW - Assignment 2]**
   - Receives device and user sync events from RabbitMQ
   - Collects real-time sensor data from devices
   - Aggregates measurements into hourly consumption records
   - Provides REST API for consumption data queries
   - Stores data in separate MySQL database

7. **Device Simulator** (Python) **[NEW - Assignment 2]**
   - Simulates real IoT devices sending sensor data
   - Generates realistic energy consumption patterns
   - Publishes measurements to RabbitMQ every 10 seconds
   - Configurable device ID and measurement intervals

8. **RabbitMQ Message Broker** (Port 5672, Management: 15672) **[NEW - Assignment 2]**
   - `sync_queue`: Synchronizes users and devices across microservices
   - `sensor_data_queue`: Receives real-time measurements from devices
   - Ensures reliable message delivery between services

9. **MySQL Databases**
   - `energy_auth_db` (Port 3309): Authentication credentials
   - `energy_users_db` (Port 3307): User profiles
   - `energy_devices_db` (Port 3308): Devices
   - `energy_monitoring_db` (Port 3310): Consumption data **[NEW]**

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Browser                          │
│                      http://localhost:3000                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Traefik Reverse Proxy                        │
│                      http://localhost                           │
└────┬────────┬────────┬────────┬────────────────────────────────┘
     │        │        │        │
     │        │        │        └─────────────────┐
     │        │        │                          │
/api/auth  /api/users  /api/devices  /api/monitoring
     │        │        │                          │
     ▼        ▼        ▼                          ▼
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌──────────────────┐
│  Auth   │ │  User   │ │ Device  │ │   Monitoring     │
│ Service │ │ Service │ │ Service │ │   Microservice   │
│  :8083  │ │  :8081  │ │  :8082  │ │      :8084       │
└────┬────┘ └────┬────┘ └────┬────┘ └────────┬─────────┘
     │           │           │                │
     │           │           │                │
     │      ┌────┴───────────┴────────────────┤
     │      │         RabbitMQ Broker         │
     │      │  sync_queue | sensor_data_queue │
     │      │            :5672                 │
     │      └─────────────────────────┬────────┘
     │                                │
     │                                │ Sensor Data
     │                                │
     │                    ┌───────────▼──────────┐
     │                    │  Device Simulator    │
     │                    │      (Python)        │
     │                    └──────────────────────┘
     │
     ▼           ▼           ▼           ▼
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────┐
│mysql-auth mysql-users mysql-devices mysql-monitoring
│  :3309  │ │  :3307  │ │  :3308  │ │    :3310    │
└─────────┘ └─────────┘ └─────────┘ └─────────────┘
```

---

## Assignment 2: Real-Time Monitoring System

### Overview

Assignment 2 extends the system with real-time energy monitoring capabilities using message-oriented middleware (RabbitMQ).

### Key Components

#### 1. Monitoring Microservice (Java Spring Boot)

**Purpose**: Collects and aggregates energy consumption data from IoT devices.

**Features**:
- Listens to RabbitMQ queues for device sync and sensor data
- Maintains local copies of users and devices
- Aggregates 6 measurements into hourly consumption records
- Provides REST API for querying consumption data
- Stores data in separate MySQL database

**Main Classes**:
- `SyncQueueListener.java`: Receives USER_CREATED and DEVICE_CREATED events
- `SensorDataListener.java`: Receives real-time sensor measurements
- `ConsumptionAggregationService.java`: Aggregates measurements into hourly records
- `MonitoringController.java`: REST API for consumption queries

**Database Schema**:
```sql
-- Users table (synced from User Microservice)
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP
);

-- Devices table (synced from Device Microservice)
CREATE TABLE devices (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    description VARCHAR(255),
    address VARCHAR(255),
    max_hourly_consumption DOUBLE,
    created_at TIMESTAMP
);

-- Hourly consumption aggregated data
CREATE TABLE hourly_consumption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(36),
    timestamp DATETIME,
    consumption_kwh FLOAT,
    measurement_count INT,
    created_at DATETIME
);
```

#### 2. Device Simulator (Python)

**Purpose**: Simulates IoT devices sending real-time energy measurements.

**Features**:
- Generates realistic consumption patterns based on time of day
- Sends measurements to RabbitMQ every 10 seconds (configurable)
- Auto-stops after 6 measurements (configurable)
- Easy configuration via JSON file

**Configuration** (`device-simulator/config.json`):
```json
{
  "device_id": "84c776f5-7ab7-4cf1-9233-b5ca6a14abad",
  "rabbitmq_host": "localhost",
  "rabbitmq_port": 5672,
  "rabbitmq_user": "guest",
  "rabbitmq_pass": "guest",
  "interval_seconds": 10,
  "test_mode": true,
  "max_measurements": 6
}
```

**Usage**:
```bash
cd device-simulator
python simulator.py
```

**Output Example**:
```
======================================================================
 Device Data Simulator Started
======================================================================
Device ID: 84c776f5-7ab7-4cf1-9233-b5ca6a14abad
RabbitMQ: localhost:5672
Queue: sensor_data_queue
  TEST MODE: Sending every 10 seconds
  AUTO-STOP: Will stop after 6 measurements
======================================================================

[1] 2025-11-27 10:14:34 - Device: 84c776f5... - Value: 2.68 kWh
    Waiting 10 seconds until next measurement...

[2] 2025-11-27 10:14:44 - Device: 84c776f5... - Value: 2.45 kWh
    Waiting 10 seconds until next measurement...

...

[6] 2025-11-27 10:15:24 - Device: 84c776f5... - Value: 2.88 kWh

======================================================================
 ✓ Reached maximum measurements limit: 6
 Total measurements sent: 6
 Simulator stopped automatically
======================================================================
```

#### 3. RabbitMQ Message Queues

**sync_queue**: Synchronizes entities across microservices
- User Microservice publishes USER_CREATED events
- Device Microservice publishes DEVICE_CREATED events
- Monitoring Microservice consumes both events

**sensor_data_queue**: Real-time sensor measurements
- Device Simulator publishes measurements
- Monitoring Microservice consumes and aggregates data

**Message Format**:
```json
// Sensor Data Message
{
  "timestamp": "2025-11-27T10:14:34.230235",
  "device_id": "84c776f5-7ab7-4cf1-9233-b5ca6a14abad",
  "measurement_value": 2.68
}

// Sync Event Message
{
  "event": "DEVICE_CREATED",
  "deviceId": "84c776f5-7ab7-4cf1-9233-b5ca6a14abad",
  "userId": "84c776f5-7ab7-4cf1-9233-b5ca6a14abad",
  "description": "laptop Ana",
  "address": "Str Universitatii 1",
  "maxHourlyConsumption": 120.0,
  "timestamp": 1764148231000
}
```

### Data Flow

1. **Device Creation**:
   ```
   Admin creates device in Frontend
   → Device Microservice saves to DB
   → Publishes DEVICE_CREATED to sync_queue
   → Monitoring Microservice receives event
   → Saves device to local DB
   ```

2. **Sensor Data Collection**:
   ```
   Device Simulator generates measurement
   → Publishes to sensor_data_queue
   → Monitoring Microservice receives measurement
   → Adds to aggregation buffer
   → After 6 measurements: calculates sum
   → Saves to hourly_consumption table
   ```

3. **Data Visualization**:
   ```
   User selects device and date in Frontend
   → Frontend calls /api/monitoring/devices/{id}/consumption
   → Monitoring Microservice queries hourly_consumption
   → Returns aggregated data
   → Frontend displays chart
   ```

### Monitoring API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/monitoring/health` | Health check |
| GET | `/api/monitoring/users` | Get all synced users |
| GET | `/api/monitoring/devices` | Get all synced devices |
| GET | `/api/monitoring/devices/{deviceId}/consumption` | Get consumption data |

**Example Request**:
```bash
curl "http://localhost/api/monitoring/devices/84c776f5-7ab7-4cf1-9233-b5ca6a14abad/consumption?startDate=2025-11-27T00:00:00&endDate=2025-11-27T23:59:59"
```

**Example Response**:
```json
[
  {
    "measurementCount": 6,
    "consumption": 14.85,
    "timestamp": "2025-11-27T10:00:00"
  }
]
```

### Frontend Monitoring Page

**Features**:
- Device selection dropdown
- Date picker calendar
- Chart type selection (Line/Bar)
- Real-time statistics:
  - Total Consumption (kWh)
  - Average Consumption per hour
  - Peak Consumption
- Interactive charts with Recharts library

**Location**: `frontend/app/dashboard/monitoring/page.tsx`

---

## Technologies Used

### Backend
- **Java 17** with **Spring Boot 3.5.6**
- **Spring Data JPA** for ORM
- **Spring Security** for authentication
- **Spring AMQP** for RabbitMQ integration **[NEW]**
- **JWT** for stateless authentication
- **MySQL 8.0** for data persistence
- **RabbitMQ 3.13** for message brokering **[NEW]**
- **Maven** for dependency management

### Frontend
- **Next.js 15** - React framework
- **React 19** - UI library
- **TypeScript 5** - Type safety
- **Tailwind CSS 4** - Styling
- **Recharts** - Data visualization **[NEW]**
- **date-fns** - Date manipulation **[NEW]**
- **SWR** - Data fetching

### Device Simulator
- **Python 3.x** **[NEW]**
- **pika** - RabbitMQ client library **[NEW]**

### Infrastructure
- **Docker** & **Docker Compose**
- **Traefik v2.10** - API Gateway
- **RabbitMQ 3.13** - Message Broker **[NEW]**

---

## Prerequisites

- **Docker Desktop** (version 20.10+)
- **Docker Compose** (version 2.0+)
- **Python 3.x** (for device simulator)
- **8GB RAM minimum**
- **Ports Available**: 80, 3000, 3307-3310, 5672, 8080-8084, 15672

---

## Quick Start Guide

### Step 1: Clone and Build

```bash
git clone <repository-url>
cd energy-management-system
docker-compose build
```

### Step 2: Start All Services

```bash
docker-compose up -d
```

Wait 30 seconds for all services to start.

### Step 3: Initialize Admin User

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

### Step 4: Access the Application

Open browser: **http://localhost:3000**

Login with:
- **Email**: `admin@energy.ro`
- **Password**: `admin123`

### Step 5: Test Monitoring System

1. **Create a User**:
   - Go to Users → Add User
   - Fill in details (Name, Email, Password, Role: CLIENT)
   - Click Create

2. **Create a Device**:
   - Go to Devices → Add Device
   - Fill in: Description, Address, Max Consumption
   - Assign to the user you created
   - Click Create

3. **Run Device Simulator**:
   ```bash
   cd device-simulator
   # Edit config.json and put your device ID
   python simulator.py
   ```

4. **View Consumption Data**:
   - Go to Monitoring page
   - Select your device from dropdown
   - Select today's date
   - Click "Load Data"
   - See the chart with consumption data!

---

## Testing the Monitoring System

### Complete Test Flow

**Step 1: Create User**
```bash
# Via Frontend or API
POST http://localhost/api/users
{
  "username": "testuser",
  "password": "test123",
  "email": "test@example.com",
  "fullName": "Test User",
  "role": "CLIENT"
}
```

**Step 2: Create Device**
```bash
POST http://localhost/api/devices
{
  "description": "Test Device",
  "address": "Test Address",
  "maximumHourlyConsumption": 100.0,
  "userId": "<user-id-from-step-1>"
}
```

**Step 3: Verify Sync in Monitoring**
```bash
# Check device synced
docker exec -it mysql-monitoring-db mysql -u root -proot energy_monitoring_db \
  -e "SELECT * FROM devices WHERE description='Test Device';"
```

**Step 4: Configure Simulator**
Edit `device-simulator/config.json`:
```json
{
  "device_id": "<device-id-from-step-2>",
  "interval_seconds": 10,
  "max_measurements": 6
}
```

**Step 5: Run Simulator**
```bash
cd device-simulator
python simulator.py
```

**Step 6: Verify Data Aggregation**
```bash
# Check hourly consumption
docker exec -it mysql-monitoring-db mysql -u root -proot energy_monitoring_db \
  -e "SELECT * FROM hourly_consumption ORDER BY timestamp DESC LIMIT 5;"
```

**Step 7: Test API**
```bash
curl "http://localhost/api/monitoring/devices/<device-id>/consumption?startDate=2025-11-27T00:00:00&endDate=2025-11-27T23:59:59"
```

**Step 8: View in Frontend**
- Open http://localhost:3000/dashboard/monitoring
- Select device
- Select date
- Click "Load Data"
- Chart appears!

---

## RabbitMQ Management

### Access RabbitMQ Management UI

**URL**: http://localhost:15672
**Username**: `guest`
**Password**: `guest`

### Monitor Queues

- `sync_queue`: User and device synchronization events
- `sensor_data_queue`: Real-time sensor measurements

### View Messages

1. Go to Queues tab
2. Click on queue name
3. Click "Get Messages"
4. See message content and statistics

---

## Project Structure

```
energy-management-system/
├── auth-microservice/              # Authentication Service
├── user-microservice/              # User Management
├── device-microservice/            # Device Management
├── monitoring-microservice/        # Monitoring Service [NEW]
│   └── src/main/java/com/energy/monitoring/
│       ├── config/                 # RabbitMQ configuration
│       ├── controller/             # REST controllers
│       ├── dto/                    # Data transfer objects
│       ├── entity/                 # JPA entities
│       ├── listener/               # RabbitMQ listeners
│       │   ├── SyncQueueListener.java
│       │   └── SensorDataListener.java
│       ├── repository/             # Data repositories
│       └── service/                # Business logic
│           └── ConsumptionAggregationService.java
├── device-simulator/               # Python Device Simulator [NEW]
│   ├── simulator.py                # Main simulator script
│   ├── config.json                 # Configuration file
│   └── README.md                   # Simulator documentation
├── frontend/                       # Next.js Frontend
│   └── app/dashboard/monitoring/   # Monitoring page [NEW]
│       └── page.tsx
├── docker-compose.yml              # Docker configuration
└── README.md                       # This file
```

---

## Troubleshooting

### RabbitMQ Connection Issues

**Problem**: Services can't connect to RabbitMQ

**Solution**:
```bash
# Check RabbitMQ is running
docker ps | grep rabbitmq

# Check logs
docker logs rabbitmq-broker

# Restart RabbitMQ
docker-compose restart rabbitmq-broker
```

### Simulator Not Sending Data

**Problem**: Simulator runs but no data in monitoring

**Solution**:
1. Check `config.json` has correct device_id
2. Verify device exists in monitoring DB
3. Check RabbitMQ queue has messages
4. Check monitoring microservice logs

```bash
docker logs monitoring-microservice-app
```

### Chart Not Displaying

**Problem**: Frontend shows "No data available"

**Solution**:
1. Check date selected matches data timestamp
2. Verify API returns data:
   ```bash
   curl "http://localhost/api/monitoring/devices/{id}/consumption?startDate=2025-11-27T00:00:00&endDate=2025-11-27T23:59:59"
   ```
3. Check browser console for errors (F12)
4. Refresh page (Ctrl+F5)

### Data Not Aggregating

**Problem**: Measurements received but not aggregated

**Solution**:
```bash
# Check monitoring logs
docker logs monitoring-microservice-app | grep "Aggregated"

# Verify 6 measurements received
docker logs monitoring-microservice-app | grep "buffer size"

# Check database
docker exec -it mysql-monitoring-db mysql -u root -proot energy_monitoring_db \
  -e "SELECT * FROM hourly_consumption;"
```

---

## Stopping the System

```bash
# Stop all services
docker-compose down

# Stop and remove all data
docker-compose down -v

# Restart specific service
docker-compose restart monitoring-microservice
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

---

## Future Enhancements

- [ ] WebSocket for real-time updates
- [ ] Alert notifications for threshold violations
- [ ] Historical data export (CSV, PDF)
- [ ] Advanced analytics and predictions
- [ ] Mobile app
- [ ] Multi-device simulator
- [ ] Kubernetes deployment

---

## Authors

**Technical University of Cluj-Napoca**
**Distributed Systems 2025**

---

## License

This project is part of an academic assignment for the Distributed Systems course.

---

## Acknowledgments

- Spring Boot team
- Next.js team
- RabbitMQ team
- Traefik team
- All open-source contributors
