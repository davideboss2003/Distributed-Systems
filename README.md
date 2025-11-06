# Energy Management System - Distributed Systems Assignment

## Project Overview

This is a distributed Energy Management System built with microservices architecture that allows authenticated users to access, monitor, and manage smart energy metering devices. The system implements role-based access control with two user types: Administrators and Clients.

![Deployment Architecture](Deployment%20diagram.png)

### Key Features

- **Role-Based Access Control**: Admin and Client roles with different permissions
- **Microservices Architecture**: Loosely coupled services for User Management, Device Management, and Authentication
- **Reverse Proxy**: Traefik API Gateway for request routing and load balancing
- **Containerized Deployment**: Full Docker-based deployment with docker-compose
- **RESTful APIs**: All microservices expose REST endpoints
- **JWT Authentication**: Secure token-based authentication with 24-hour expiration
- **Database Persistence**: Separate MySQL databases for each service
- **Auto-Sync**: Automatic user synchronization between services
- **Modern Frontend**: Next.js-based SPA with responsive design and TypeScript

---

## System Architecture

### Components

1. **Frontend Application** (Next.js 15 + TypeScript)
   - Browser-based single-page application
   - Admin dashboard for CRUD operations on users and devices
   - Client dashboard for viewing assigned devices
   - Role-based UI rendering
   - Built with App Router and React 19

2. **Traefik Reverse Proxy & API Gateway**
   - Entry point for all HTTP requests (Port 80)
   - Routes requests to appropriate microservices
   - Provides load balancing and service discovery
   - Dashboard for monitoring (Port 8080)

3. **Authentication Service** (Spring Boot - Port 8083)
   - Handles user login and JWT token generation
   - Validates JWT tokens
   - Manages authentication credentials with BCrypt hashing
   - User registration (ADMIN only)

4. **User Management Microservice** (Spring Boot - Port 8081)
   - CRUD operations for user accounts
   - Stores user profile data (username, email, full name, role, password)
   - Automatically syncs user data (id, username) to Device Service via REST API
   - Password encryption with BCrypt before storage
   - Triggers sync on CREATE, UPDATE, and DELETE operations

5. **Device Management Microservice** (Spring Boot - Port 8082)
   - CRUD operations for devices
   - Device-to-user assignment
   - Stores device data (description, address, maximum hourly consumption)
   - Maintains local copy of users (id, username) for device assignment
   - Receives user sync from User Management Service via `/api/sync/users` endpoint
   - Can also fetch user data from User Service when assigning devices

6. **MySQL Databases**
   - Separate database instances for each service:
     - `energy_auth_db` (Port 3309): Authentication credentials
     - `energy_users_db` (Port 3307): User profiles
     - `energy_devices_db` (Port 3308): Devices and synced users

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
│                   Dashboard: :8080                              │
└────────────┬────────────────┬────────────────┬──────────────────┘
             │                │                │
      /api/auth        /api/users       /api/devices
             │                │                │
             ▼                ▼                ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  Auth Service   │  │  User Service   │  │ Device Service  │
│   Port: 8083    │  │   Port: 8081    │  │   Port: 8082    │
│  Spring Boot    │  │  Spring Boot    │  │  Spring Boot    │
└────────┬────────┘  └────────┬────────┘  └────────┬────────┘
         │                    │                    │
         │                    │ Sync API           │
         │                    └────────────────────┤
         │                                         │
         ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  mysql-auth     │  │  mysql-users    │  │ mysql-devices   │
│   Port: 3309    │  │   Port: 3307    │  │   Port: 3308    │
│ energy_auth_db  │  │ energy_users_db │  │energy_devices_db│
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

---

## Technologies Used

### Backend
- **Java 17** with **Spring Boot 3.5.6**
- **Spring Data JPA** for ORM
- **Spring Security** for authentication and authorization
- **JWT (JSON Web Tokens)** for stateless authentication
- **MySQL 8.0** for data persistence
- **Maven** for dependency management
- **Hibernate** for JPA implementation
- **RestTemplate** for inter-service communication
- **Lombok** for reducing boilerplate code
- **ModelMapper** for DTO mapping
- **SpringDoc OpenAPI** for API documentation

### Frontend
- **Next.js 15** - React framework with App Router
- **React 19** - Modern UI library
- **TypeScript 5** - Type-safe JavaScript
- **Tailwind CSS 4** - Utility-first styling
- **Radix UI** - Accessible component primitives
- **Framer Motion** - Animation library
- **SWR** - Data fetching and caching
- **Recharts** - Data visualization

### Infrastructure
- **Docker** & **Docker Compose** for containerization
- **Traefik v2.10** as reverse proxy and API gateway
- **Docker Networks** for service isolation
- **MySQL 8.0** for database containers

---

## Prerequisites

Before running this project, ensure you have the following installed:

- **Docker Desktop** (version 20.10 or higher)
  - Download from: https://www.docker.com/products/docker-desktop
- **Docker Compose** (version 2.0 or higher)
  - Included with Docker Desktop
- **Git** (for cloning the repository)
- **8GB RAM minimum** (recommended for running all containers)
- **Ports Available**: 80, 3000, 3307, 3308, 3309, 8080, 8081, 8082, 8083

### Optional (for local development without Docker)
- **Java JDK 17**
- **Node.js 18+** and **npm**
- **Maven 3.8+**
- **MySQL 8.0**

### Verify Installation

```bash
docker --version
docker-compose --version
```

---

## Build and Deployment Instructions

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd energy-management-system
```

### Step 2: Build All Services

Build all Docker images (this may take 5-10 minutes on first run):

```bash
docker-compose build
```

### Step 3: Start the System

Start all services in detached mode:

```bash
docker-compose up -d
```

### Step 4: Verify Services are Running

Check that all containers are running:

```bash
docker-compose ps
```

You should see 9 containers running:
- `traefik` - Reverse proxy
- `energy-frontend-app` - Next.js application
- `auth-microservice-app` - Authentication service
- `user-microservice-app` - User management service
- `device-microservice-app` - Device management service
- `mysql-auth-db` - Auth database
- `mysql-users-db` - User database
- `mysql-devices-db` - Device database

### Step 5: Initialize Admin User

Create the initial admin user using the provided script:

**Windows (CMD):**
```cmd
cd auth-microservice
docker-init-admin.bat
```

**Windows (PowerShell):**
```powershell
cd auth-microservice
.\docker-init-admin.ps1
```

**Note**: If the script fails, you can manually create the admin user:

```bash
docker exec -i mysql-auth-db mysql -u root -proot energy_auth_db -e "INSERT INTO auth_users (id, username, email, password_hash, role, created_at, updated_at) VALUES (UUID(), 'admin', 'admin@energy.ro', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW(), NOW()) ON DUPLICATE KEY UPDATE username=username;"
```

This creates an admin user with:
- **Email**: `admin@energy.ro`
- **Password**: `admin123` (BCrypt hashed)

### Step 6: Access the Application

Open your browser and navigate to:

```
http://localhost:3000
```

Or access the API gateway directly:

```
http://localhost
```

---

## Default User Accounts

### Administrator Account
- **Email**: `admin@energy.ro`
- **Password**: `admin123`
- **Role**: ADMIN
- **Permissions**: Full CRUD on users and devices, device assignment

### Creating Additional Users

Additional users can be created through:
1. The admin interface (after logging in as admin)
2. The API endpoint `/api/auth/register` (requires ADMIN token)

---

## User Guide

### For Administrators

1. **Login** with admin credentials at `http://localhost:3000/dashboard/login`

2. **User Management**:
   - Navigate to "Users" from the sidebar
   - Click "+ Create User" to add new users
   - Fill in: Username, Password, Email, Full Name, Role (ADMIN/CLIENT)
   - Edit or Delete existing users from the table
   - Changes automatically sync to Auth and Device services

3. **Device Management**:
   - Navigate to "Devices" from the sidebar
   - Click "+ Create Device" to add new devices
   - Fill in: Description, Address, Maximum Hourly Consumption
   - Assign devices to users using the user dropdown
   - Edit or Delete existing devices

4. **Device Assignment**:
   - Select a user from the dropdown when creating/editing a device
   - Device will be linked to that user's account
   - User can view the device in their dashboard

### For Clients

1. **Login** with client credentials at `http://localhost:3000/dashboard/login`

2. **View Devices**:
   - Automatically redirected to "My Devices" page
   - See all devices assigned to your account
   - View device details: description, address, max consumption
   - Read-only access (no edit/delete permissions)

3. **View Profile**:
   - Navigate to "Profile" from the sidebar
   - View your account information
   - See username, email, full name, and role

---

## API Endpoints

### Authentication Service (Port 8083)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | Login and get JWT token | No |
| POST | `/api/auth/register` | Register new user | Yes (ADMIN) |
| GET | `/api/auth/validate` | Validate JWT token | No |

**Login Request Example:**
```json
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@energy.ro",
  "password": "admin123"
}
```

**Login Response Example:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "admin",
  "role": "ADMIN"
}
```

### User Management Service (Port 8081)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/users` | Get all users | Yes | ADMIN |
| GET | `/api/users/{id}` | Get user by ID | Yes | Any |
| GET | `/api/users/username/{username}` | Get user by username | Yes | ADMIN |
| POST | `/api/users` | Create new user | Yes | ADMIN |
| PUT | `/api/users/{id}` | Update user | Yes | ADMIN |
| DELETE | `/api/users/{id}` | Delete user | Yes | ADMIN |

**Create User Request Example:**
```json
POST /api/users
Headers: 
  Authorization: Bearer <admin-token>
  Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "email": "user@example.com",
  "fullName": "John Doe",
  "role": "CLIENT"
}
```

**Response:**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "username": "newuser",
  "email": "user@example.com",
  "fullName": "John Doe",
  "role": "CLIENT",
  "createdAt": "2025-11-06T10:30:00",
  "updatedAt": "2025-11-06T10:30:00"
}
```

### Device Management Service (Port 8082)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/devices` | Get all devices | Yes | ADMIN |
| GET | `/api/devices/{id}` | Get device by ID | Yes | Any |
| GET | `/api/devices/user/{userId}` | Get devices for user | Yes | Any |
| POST | `/api/devices` | Create new device | Yes | ADMIN |
| PUT | `/api/devices/{id}` | Update device | Yes | ADMIN |
| DELETE | `/api/devices/{id}` | Delete device | Yes | ADMIN |
| POST | `/api/sync/users` | Receive user sync from User Service | No | Internal |
| DELETE | `/api/sync/users/{userId}` | Receive user deletion from User Service | No | Internal |

**Create Device Request Example:**
```json
POST /api/devices
Headers:
  Authorization: Bearer <admin-token>
  Content-Type: application/json

{
  "description": "Smart Meter 001",
  "address": "192.168.1.100",
  "maximumHourlyConsumption": 5000.0,
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "description": "Smart Meter 001",
  "address": "192.168.1.100",
  "maximumHourlyConsumption": 5000.0,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "admin",
  "userEmail": "admin@energy.ro",
  "createdAt": "2025-11-06T10:35:00",
  "updatedAt": "2025-11-06T10:35:00"
}
```

---

## Project Structure

```
energy-management-system/
├── auth-microservice/              # Authentication Service
│   └── src/main/java/ro/tuc/ds2025/auth/
│       ├── config/                 # Security configuration
│       ├── controller/             # REST controllers
│       │   └── AuthController.java
│       ├── dto/                    # Data transfer objects
│       │   ├── LoginRequestDTO.java
│       │   ├── LoginResponseDTO.java
│       │   └── RegisterRequestDTO.java
│       ├── exception/              # Custom exceptions
│       ├── model/                  # JPA entities
│       │   └── AuthUser.java
│       ├── repository/             # Data repositories
│       ├── security/               # JWT utilities & filters
│       │   ├── JwtAuthenticationFilter.java
│       │   └── JwtTokenProvider.java
│       ├── service/                # Business logic
│       │   └── AuthService.java
│       └── AuthMicroserviceApplication.java
│   ├── Dockerfile
│   ├── pom.xml
│   ├── docker-init-admin.bat       # Admin initialization script
│   ├── docker-init-admin.ps1
│   └── Energy_Auth_Postman_Collection.json
│
├── user-microservice/              # User Management Service
│   └── src/main/java/ro/tuc/ds2025/users/
│       ├── config/                 # RestTemplate, CORS, Security
│       │   ├── ModelMapperConfig.java
│       │   ├── RestTemplateConfig.java
│       │   ├── SecurityConfig.java
│       │   └── SwaggerConfig.java
│       ├── controller/             # User controllers
│       │   └── UserController.java
│       ├── dto/                    # Data transfer objects
│       │   ├── CreateUserDTO.java
│       │   ├── UpdateUserDTO.java
│       │   ├── UserDTO.java
│       │   └── UserSyncDTO.java
│       ├── exception/              # Custom exceptions
│       ├── model/                  # User entity
│       │   └── User.java
│       ├── repository/             # User repository
│       ├── security/               # JWT filter & utils
│       │   ├── JwtAuthenticationFilter.java
│       │   └── JwtTokenProvider.java
│       ├── service/                # Business logic & sync
│       │   └── UserService.java
│       └── UserMicroserviceApplication.java
│   ├── Dockerfile
│   ├── pom.xml
│   └── fix_users_table.sql
│
├── device-microservice/            # Device Management Service
│   └── src/main/java/ro/tuc/ds2025/devices/
│       ├── config/                 # Configuration classes
│       │   ├── ModelMapperConfig.java
│       │   ├── RestTemplateConfig.java
│       │   └── SecurityConfig.java
│       ├── controller/             # Device & sync controllers
│       │   ├── DeviceController.java
│       │   └── UserSyncController.java
│       ├── dto/                    # Data transfer objects
│       │   ├── CreateDeviceDTO.java
│       │   ├── DeviceDTO.java
│       │   ├── UpdateDeviceDTO.java
│       │   └── UserSyncDTO.java
│       ├── exception/              # Custom exceptions
│       ├── model/                  # Device & User entities
│       │   ├── Device.java
│       │   └── User.java (synced copy)
│       ├── repository/             # Device & user repositories
│       │   ├── DeviceRepository.java
│       │   └── UserRepository.java
│       ├── security/               # JWT filter & utils
│       │   ├── JwtAuthenticationFilter.java
│       │   └── JwtTokenProvider.java
│       ├── service/                # Device service & sync
│       │   ├── DeviceService.java
│       │   └── UserSyncService.java
│       └── DeviceMicroserviceApplication.java
│   ├── Dockerfile
│   ├── pom.xml
│   └── fix_database_charset.sql
│
├── frontend/                       # Next.js Frontend
│   ├── app/                        # Next.js App Router
│   │   ├── dashboard/              # Protected routes
│   │   │   ├── devices/            # Device management
│   │   │   │   └── page.tsx
│   │   │   ├── login/              # Authentication
│   │   │   │   └── page.tsx
│   │   │   ├── register/           # Registration
│   │   │   │   └── page.tsx
│   │   │   ├── users/              # User management
│   │   │   │   └── page.tsx
│   │   │   └── page.tsx            # Dashboard overview
│   │   ├── globals.css             # Global Tailwind styles
│   │   ├── layout.tsx              # Root layout with AuthProvider
│   │   └── page.tsx                # Home page (redirect logic)
│   ├── components/                 # React components
│   │   ├── ui/                     # UI components (Radix UI)
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── input.tsx
│   │   │   ├── label.tsx
│   │   │   ├── select.tsx
│   │   │   ├── table.tsx
│   │   │   ├── toast.tsx
│   │   │   └── toaster.tsx
│   │   ├── auth-provider.tsx       # Auth context provider
│   │   ├── dashboard-layout.tsx    # Dashboard layout
│   │   └── protected-route.tsx     # Route protection HOC
│   ├── hooks/                      # Custom React hooks
│   │   └── use-toast.ts
│   ├── lib/                        # Utilities
│   │   ├── api.ts                  # API client
│   │   ├── auth.ts                 # JWT utilities
│   │   └── utils.ts                # Helper functions
│   ├── .env.local                  # Environment variables
│   ├── Dockerfile
│   ├── package.json
│   ├── next.config.ts
│   ├── tailwind.config.ts
│   ├── tsconfig.json
│   └── FRONTEND_STRUCTURE.md       # Frontend documentation
│
├── docker-compose.yml              # Docker Compose configuration
├── Deployment diagram.png          # Architecture diagram
└── README.md                       # This file
```

---

## Database Schema

### Authentication Database (`energy_auth_db`)

**Table: auth_users**

| Column | Type | Description |
|--------|------|-------------|
| id | UUID (PK) | Auto-generated user ID |
| username | VARCHAR(50) UNIQUE | Login username |
| email | VARCHAR(255) UNIQUE | User email |
| password_hash | VARCHAR(255) | BCrypt hashed password |
| role | VARCHAR(20) | User role (ADMIN/CLIENT) |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

```sql
CREATE TABLE auth_users (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### User Management Database (`energy_users_db`)

**Table: users**

| Column | Type | Description |
|--------|------|-------------|
| id | CHAR(36) (PK) | User unique identifier (UUID) |
| username | VARCHAR(50) | Username |
| password | VARCHAR(255) | BCrypt hashed password |
| email | VARCHAR(255) | User email |
| full_name | VARCHAR(100) | User's full name |
| role | VARCHAR(20) | User role (ADMIN/CLIENT) |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

```sql
CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Device Management Database (`energy_devices_db`)

**Table: users** (synced copy)

| Column | Type | Description |
|--------|------|-------------|
| id | VARCHAR(36) (PK) | User ID (synced from User service) |
| username | VARCHAR(255) | Username |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

**Table: devices**

| Column | Type | Description |
|--------|------|-------------|
| id | CHAR(36) (PK) | Device unique identifier |
| description | VARCHAR(255) | Device description |
| address | VARCHAR(255) | Device IP/location |
| maximum_hourly_consumption | DOUBLE | Maximum energy consumption (Wh) |
| user_id | VARCHAR(36) (FK) | Assigned user UUID (nullable) |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_username (username)
);

CREATE TABLE devices (
    id CHAR(36) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    maximum_hourly_consumption DOUBLE NOT NULL,
    user_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_device_user_id (user_id)
);
```

---

## Docker Configuration

### Services Overview

```yaml
services:
  traefik:          # Reverse Proxy (Port 80, 8080)
  frontend:         # Next.js Application (Port 3000)
  mysql-auth:       # Auth Database (Port 3309)
  mysql-users:      # User Database (Port 3307)
  mysql-devices:    # Device Database (Port 3308)
  auth-microservice:     # Authentication Service (Internal: 8083)
  user-microservice:     # User Management (Internal: 8081)
  device-microservice:   # Device Management (Internal: 8082)
```

### Network Configuration

All services communicate through the `energy-network` Docker network:
- Services can reach each other using service names (e.g., `http://user-microservice:8081`)
- External access is through Traefik on port 80
- Frontend served on port 3000
- Traefik dashboard on port 8080

### Volume Configuration

- `mysql-auth-data`: Persistent storage for auth database
- `mysql-users-data`: Persistent storage for user database
- `mysql-devices-data`: Persistent storage for device database

### Service URLs

| Service | Internal URL | External URL | Description |
|---------|-------------|--------------|-------------|
| Frontend | http://frontend:3000 | http://localhost:3000 | Web interface |
| API Gateway | - | http://localhost | All API endpoints |
| Traefik Dashboard | - | http://localhost:8080 | Proxy monitoring |
| Auth Service | http://auth-microservice:8083 | http://localhost/api/auth | Authentication |
| User Service | http://user-microservice:8081 | http://localhost/api/users | User management |
| Device Service | http://device-microservice:8082 | http://localhost/api/devices | Device management |

---

## Configuration Files

### Environment Variables

**Authentication Service:**
```properties
server.port=8083
spring.datasource.url=jdbc:mysql://mysql-auth:3306/energy_auth_db
spring.datasource.username=root
spring.datasource.password=root
jwt.secret=your-secret-key-min-256-bits-for-hs256-algorithm-energy-management
jwt.expiration=86400000
```

**User Service:**
```properties
server.port=8081
spring.datasource.url=jdbc:mysql://mysql-users:3306/energy_users_db
spring.datasource.username=root
spring.datasource.password=root
jwt.secret=your-secret-key-min-256-bits-for-hs256-algorithm-energy-management
device.microservice.url=http://device-microservice:8082
```

**Device Service:**
```properties
server.port=8082
spring.datasource.url=jdbc:mysql://mysql-devices:3306/energy_devices_db
spring.datasource.username=root
spring.datasource.password=root
jwt.secret=your-secret-key-min-256-bits-for-hs256-algorithm-energy-management
user.microservice.url=http://user-microservice:8081
```

**Frontend:**
```env
NEXT_PUBLIC_API_URL=http://localhost
NODE_ENV=production
```

---

## Testing the System

### Test User Creation and Synchronization

1. **Create a new client user** (as admin):

```bash
curl -X POST http://localhost/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "username": "testclient",
    "password": "test123",
    "email": "test@example.com",
    "fullName": "Test Client",
    "role": "CLIENT"
  }'
```

2. **Verify user in Auth database**:

```bash
docker exec -it mysql-auth-db mysql -u root -proot energy_auth_db \
  -e "SELECT * FROM auth_users WHERE username='testclient';"
```

3. **Verify user in Device database**:

```bash
docker exec -it mysql-devices-db mysql -u root -proot energy_devices_db \
  -e "SELECT * FROM users WHERE username='testclient';"
```

4. **Login as testclient**:

```bash
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123"
  }'
```

### Test Device Assignment

1. **Login as admin** and get token

2. **Create a device**:

```bash
curl -X POST http://localhost/api/devices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "description": "Smart Meter A",
    "address": "192.168.1.50",
    "maximumHourlyConsumption": 3000.0,
    "userId": "<testclient-user-id>"
  }'
```

3. **Logout and login as testclient**

4. **Verify device appears** in client's dashboard:

```bash
curl http://localhost/api/devices/user/<testclient-user-id> \
  -H "Authorization: Bearer <client-token>"
```

### Using Postman

1. **Import collection**:
   - `auth-microservice/Energy_Auth_Postman_Collection.json`

2. **Set environment variables**:
   - `baseUrl`: `http://localhost`
   - `adminToken`: (automatically set after login)
   - `clientToken`: (automatically set after login)

3. **Test flow**:
   - Login ADMIN → Get token
   - Validate Token ADMIN
   - Register CLIENT (with ADMIN token)
   - Login CLIENT
   - Register ADMIN (with ADMIN token)
   - Test error cases (403 Forbidden)

---

## Stopping the System

### Stop all services:

```bash
docker-compose down
```

### Stop and remove all data (including databases):

```bash
docker-compose down -v
```

### Restart specific service:

```bash
docker-compose restart user-microservice
```

### View logs:

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f user-microservice
```

---

## Troubleshooting

### Issue: Containers won't start

**Solution:**
```bash
# Check Docker is running
docker ps

# View logs for specific service
docker logs auth-microservice-app
docker logs user-microservice-app
docker logs device-microservice-app

# Check all services status
docker-compose ps
```

### Issue: Port already in use

**Solution:**
- Check if port 80 is available: `netstat -an | findstr "80"`
- Stop any other web servers (IIS, Apache, etc.)
- Or modify `docker-compose.yml` to use different ports

### Issue: Database connection errors

**Solution:**
```bash
# Check database health
docker-compose ps mysql-auth mysql-users mysql-devices

# Verify database is ready
docker exec mysql-users-db mysqladmin ping -u root -proot

# Restart database
docker-compose restart mysql-users

# Check database logs
docker logs mysql-users-db
```

### Issue: Frontend shows blank page

**Solution:**
```bash
# Rebuild and restart frontend
docker-compose build frontend
docker-compose up -d frontend

# Clear browser cache (Ctrl+Shift+R)

# Check frontend logs
docker logs energy-frontend-app
```

### Issue: 401 Unauthorized on API requests

**Solution:**
- Ensure you're logged in and have a valid token
- Check token expiration (24 hours)
- Verify Authorization header format: `Bearer <token>`
- Login again to get a fresh token

### Issue: 403 Forbidden on operations

**Solution:**
- Ensure you're logged in as ADMIN for admin operations
- Check browser console for role information
- Logout and login again to refresh token
- Verify role in localStorage: `localStorage.getItem('role')`

### Issue: User creation doesn't sync with auth service

**Solution:**
```bash
# Check if auth service is running
docker ps | grep auth-microservice

# Check logs
docker logs auth-microservice-app
docker logs user-microservice-app

# Verify network connectivity
docker exec user-microservice-app ping auth-microservice

# Check RestTemplate configuration in User service
```

### Issue: CORS errors in browser console

**Solution:**
- Verify CORS configuration in each service (`@CrossOrigin(origins = "*")`)
- Check that frontend origin is allowed
- Restart services after configuration changes
- Clear browser cache

### Issue: MySQL charset/collation errors

**Solution:**
```bash
# Run the fix scripts provided in each microservice folder
docker exec -i mysql-devices-db mysql -u root -proot energy_devices_db < device-microservice/fix_database_charset.sql
docker exec -i mysql-users-db mysql -u root -proot energy_users_db < user-microservice/fix_users_table.sql
```

---

## Monitoring and Logs

### View logs for all services:

```bash
docker-compose logs -f
```

### View logs for specific service:

```bash
docker logs -f auth-microservice-app
docker logs -f user-microservice-app
docker logs -f device-microservice-app
docker logs -f energy-frontend-app
```

### Access Traefik Dashboard:

```
http://localhost:8080
```

The dashboard shows:
- Active routers and their rules
- Services status and health
- Middleware configuration
- Real-time request metrics

### Database Access:

```bash
# Connect to auth database
docker exec -it mysql-auth-db mysql -u root -proot energy_auth_db

# Connect to user database
docker exec -it mysql-users-db mysql -u root -proot energy_users_db

# Connect to device database
docker exec -it mysql-devices-db mysql -u root -proot energy_devices_db

# Example queries
SELECT * FROM users;
SELECT * FROM devices;
SELECT * FROM auth_users;
```

### Health Check Endpoints:

- Auth Service: http://localhost:8083/actuator/health
- User Service: http://localhost:8081/actuator/health
- Device Service: http://localhost:8082/actuator/health

---

## API Documentation (Swagger)

Each microservice provides interactive API documentation via Swagger UI:

- **Auth API**: http://localhost:8083/swagger-ui.html
- **User API**: http://localhost:8081/swagger-ui.html
- **Device API**: http://localhost:8082/swagger-ui.html

Access these URLs when services are running to explore and test API endpoints interactively.

---

## User Synchronization Mechanism

This system implements a sophisticated user synchronization strategy between microservices:

### Synchronization Flow

```
User Microservice (CREATE/UPDATE/DELETE User)
         │
         ├─────────────────────────────────────┐
         │                                     │
         ▼                                     ▼
Device Microservice                   (Future: Auth Microservice)
POST /api/sync/users                  (Not yet implemented)
DELETE /api/sync/users/{id}
         │
         ▼
Local User Cache Updated
(id, username only)
```

### How It Works

1. **User Creation in User Service**:
   - User is created in `energy_users_db`
   - Password is BCrypt hashed
   - `UserService.syncUserToDeviceMicroservice()` is called
   - POST request sent to Device Service: `/api/sync/users`
   - Device Service stores minimal user data (id, username) in local cache

2. **User Update in User Service**:
   - User is updated in `energy_users_db`
   - Same sync process as creation
   - Device Service updates its local user cache

3. **User Deletion in User Service**:
   - `UserService.deleteUserFromDeviceMicroservice()` is called first
   - DELETE request sent to Device Service: `/api/sync/users/{userId}`
   - Device Service deletes all devices assigned to that user
   - Device Service removes user from local cache
   - Finally, user is deleted from `energy_users_db`

4. **Device Assignment**:
   - When assigning a device to a user, Device Service can:
     - Use the local cached user data (if already synced)
     - OR fetch user data from User Service via GET `/api/users/{userId}`
   - This dual approach ensures data consistency

### Why This Design?

- **Performance**: Device Service doesn't need to call User Service for every device query
- **Resilience**: Device Service can operate even if User Service is temporarily unavailable
- **Data Consistency**: Automatic sync ensures user data is always up-to-date
- **Loose Coupling**: Services remain independent with clear API contracts

### Sync Endpoints

**Device Service Sync Controller** (`/api/sync/users`):
- `POST /api/sync/users` - Receive user create/update from User Service
- `DELETE /api/sync/users/{userId}` - Receive user deletion from User Service

**User Service Sync Logic** (`UserService.java`):
- `syncUserToDeviceMicroservice()` - Sends user data to Device Service
- `deleteUserFromDeviceMicroservice()` - Notifies Device Service of user deletion

---

## Key Design Decisions

1. **Microservices Communication**: Services communicate via HTTP REST APIs using RestTemplate
2. **Authentication Flow**: JWT tokens generated by Auth Service, validated by each service independently
3. **Database Strategy**: Separate MySQL instances for each service to ensure data isolation
4. **User Sync Strategy**: Push-based synchronization from User Service to Device Service with fallback to pull
5. **Role-Based UI**: Frontend dynamically adjusts based on user role (CLIENT vs ADMIN)
6. **Stateless Authentication**: JWT tokens contain all necessary information, no server-side sessions
7. **API Gateway**: Traefik routes requests based on path prefixes and handles load balancing
8. **Frontend Architecture**: Next.js App Router with TypeScript for type safety and better developer experience
9. **Component Library**: Radix UI primitives with Tailwind CSS for accessible and customizable components
10. **State Management**: React Context for auth state, SWR for server state caching
11. **Error Handling**: Sync failures are logged but don't block main operations (graceful degradation)
12. **Data Minimization**: Device Service only stores essential user data (id, username) to reduce coupling

---

## Security Considerations

1. **JWT Tokens**: Stateless authentication with 24-hour expiration
2. **Password Encryption**: BCrypt hashing with strength 10 for stored passwords
3. **Role-Based Access Control**: Endpoints protected by role checks on both frontend and backend
4. **CORS Configuration**: Configured to allow frontend access from specific origins
5. **Network Isolation**: Services communicate through internal Docker network
6. **Secret Key**: Shared JWT secret key across all services (should be environment variable in production)
7. **Input Validation**: Jakarta Validation annotations on DTOs and entities
8. **SQL Injection Prevention**: JPA/Hibernate parameterized queries
9. **XSS Protection**: React's built-in XSS protection through JSX escaping
10. **HTTPS**: Should be enabled in production with proper SSL/TLS certificates

---

## Development

### Running Individual Services Locally

Each microservice can be run independently for development:

#### Backend Services (Spring Boot)

```bash
# User Microservice
cd user-microservice
./mvnw spring-boot:run

# Device Microservice
cd device-microservice
./mvnw spring-boot:run

# Auth Microservice
cd auth-microservice
./mvnw spring-boot:run
```

**Note**: Update `application.properties` to use `localhost` instead of Docker service names.

#### Frontend

```bash
cd frontend
npm install
npm run dev
```

Access at: http://localhost:3000

### Building Services

```bash
# Build all services
docker-compose build

# Build specific service
docker-compose build user-microservice

# Build without cache
docker-compose build --no-cache
```

### Hot Reload Development

For backend development with hot reload:
1. Use Spring Boot DevTools (already included in dependencies)
2. Run services locally with `./mvnw spring-boot:run`
3. Changes to Java files will trigger automatic restart

For frontend development:
1. Run `npm run dev` in frontend folder
2. Changes to TypeScript/React files will hot reload automatically

---

## Future Enhancements

### Planned Features
- [ ] Real-time energy consumption monitoring with WebSocket
- [ ] Historical data visualization with charts
- [ ] Email notifications for threshold alerts
- [ ] Export data (CSV, PDF)
- [ ] Advanced filtering and sorting
- [ ] Pagination for large datasets
- [ ] Dark mode toggle
- [ ] Multi-language support (i18n)
- [ ] Password reset flow
- [ ] Two-factor authentication (2FA)
- [ ] Audit logging for all operations
- [ ] Device status monitoring (online/offline)
- [ ] Energy consumption predictions with ML

### Technical Improvements
- [ ] Unit tests (JUnit, Mockito)
- [ ] Integration tests
- [ ] E2E tests (Playwright)
- [ ] Performance optimization
- [ ] Caching layer (Redis)
- [ ] Message queue (RabbitMQ/Kafka)
- [ ] Service mesh (Istio)
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Monitoring (Prometheus + Grafana)
- [ ] Distributed tracing (Jaeger)
- [ ] API rate limiting
- [ ] Database migrations (Flyway/Liquibase)

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Java coding conventions for backend
- Use ESLint/Prettier for frontend code
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed

---

## License

This project is part of an academic assignment for the Distributed Systems course at Technical University of Cluj-Napoca (2025).

---

## Authors

- **Technical University of Cluj-Napoca**
- **Distributed Systems 2025**
- **Group ID**: ro.tuc.ds2025

---

## Acknowledgments

- Spring Boot team for the excellent framework
- Next.js team for the modern React framework
- Traefik for the powerful reverse proxy
- Radix UI for accessible component primitives
- All open-source contributors

---

## Contact & Support

For questions or issues:
1. Check the [Troubleshooting](#troubleshooting) section
2. Review the [API Documentation](#api-documentation-swagger)
3. Check Docker logs for error messages
4. Verify all services are running with `docker-compose ps`

---

**Built with ❤️ for efficient energy management**

**Last Updated**: November 6, 2025
