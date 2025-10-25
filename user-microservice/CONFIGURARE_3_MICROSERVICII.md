# 🏗️ CONFIGURARE COMPLETĂ - 3 Microservicii cu Docker

## 📊 ARHITECTURA SISTEMULUI

```
Energy Management System
├── User Microservice      → Port 8081, MySQL 3307
├── Device Microservice    → Port 8082, MySQL 3308
└── Auth Microservice      → Port 8083, MySQL 3309
```

---

## 🎯 PORTURI ALOCATE

| Microserviciu | App Port | MySQL Port Extern | MySQL Port Intern | Bază de Date |
|---------------|----------|-------------------|-------------------|--------------|
| **Users**     | 8081     | 3307              | 3306              | energy_users_db |
| **Devices**   | 8082     | 3308              | 3306              | energy_devices_db |
| **Auth**      | 8083     | 3309              | 3306              | energy_auth_db |

---

## 📁 STRUCTURA PROIECTULUI

```
C:\Projects\energy-management-system\
│
├── user-microservice\
│   ├── src\
│   ├── pom.xml
│   ├── Dockerfile
│   ├── docker-compose.yml          ← MySQL pe 3307
│   └── .dockerignore
│
├── device-microservice\
│   ├── src\
│   ├── pom.xml
│   ├── Dockerfile
│   ├── docker-compose.yml          ← MySQL pe 3308
│   └── .dockerignore
│
└── auth-microservice\
    ├── src\
    ├── pom.xml
    ├── Dockerfile
    ├── docker-compose.yml          ← MySQL pe 3309
    └── .dockerignore
```

---

## 🔧 CONFIGURARE PENTRU FIECARE MICROSERVICIU

### 1️⃣ USER MICROSERVICE (Port 8081, MySQL 3307)

**Fișier: `user-microservice/docker-compose.yml`** (DEJA CONFIGURAT ✅)

```yaml
version: '3.8'

services:
  mysql-users:
    image: mysql:8.0
    container_name: mysql-users-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: energy_users_db
    ports:
      - "3307:3306"
    volumes:
      - mysql-users-data:/var/lib/mysql
    networks:
      - users-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  user-microservice:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: user-microservice-app
    restart: unless-stopped
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-users:3306/energy_users_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SERVER_PORT: 8081
    depends_on:
      mysql-users:
        condition: service_healthy
    networks:
      - users-network

networks:
  users-network:
    driver: bridge

volumes:
  mysql-users-data:
    driver: local
```

---

### 2️⃣ DEVICE MICROSERVICE (Port 8082, MySQL 3308)

**Fișier: `device-microservice/docker-compose.yml`**

```yaml
version: '3.8'

services:
  mysql-devices:
    image: mysql:8.0
    container_name: mysql-devices-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: energy_devices_db
    ports:
      - "3308:3306"  # Port extern 3308
    volumes:
      - mysql-devices-data:/var/lib/mysql
    networks:
      - devices-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  device-microservice:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: device-microservice-app
    restart: unless-stopped
    ports:
      - "8082:8082"  # Port aplicație 8082
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-devices:3306/energy_devices_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SERVER_PORT: 8082
    depends_on:
      mysql-devices:
        condition: service_healthy
    networks:
      - devices-network

networks:
  devices-network:
    driver: bridge

volumes:
  mysql-devices-data:
    driver: local
```

**Fișier: `device-microservice/Dockerfile`** (ACELAȘI ca la users)

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

**Fișier: `device-microservice/.dockerignore`** (ACELAȘI ca la users)

---

### 3️⃣ AUTH MICROSERVICE (Port 8083, MySQL 3309)

**Fișier: `auth-microservice/docker-compose.yml`**

```yaml
version: '3.8'

services:
  mysql-auth:
    image: mysql:8.0
    container_name: mysql-auth-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: energy_auth_db
    ports:
      - "3309:3306"  # Port extern 3309
    volumes:
      - mysql-auth-data:/var/lib/mysql
    networks:
      - auth-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  auth-microservice:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: auth-microservice-app
    restart: unless-stopped
    ports:
      - "8083:8083"  # Port aplicație 8083
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-auth:3306/energy_auth_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SERVER_PORT: 8083
      JWT_SECRET: your-secret-key-min-256-bits-for-hs256-algorithm-energy-management
      JWT_EXPIRATION: 86400000
    depends_on:
      mysql-auth:
        condition: service_healthy
    networks:
      - auth-network

networks:
  auth-network:
    driver: bridge

volumes:
  mysql-auth-data:
    driver: local
```

**Fișier: `auth-microservice/Dockerfile`** (ACELAȘI ca la users, doar EXPOSE 8083)

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

---

## 🚀 COMENZI PENTRU PORNIRE

### Opțiunea A: Pornește fiecare microserviciu separat

```bash
# Terminal 1 - User Microservice
cd user-microservice
docker-compose up --build

# Terminal 2 - Device Microservice
cd device-microservice
docker-compose up --build

# Terminal 3 - Auth Microservice
cd auth-microservice
docker-compose up --build
```

### Opțiunea B: Pornește toate dintr-un singur fișier (RECOMANDAT)

Creează un fișier `docker-compose.yml` în folderul părinte:

**Fișier: `energy-management-system/docker-compose.yml`**

```yaml
version: '3.8'

services:
  # ========== USER MICROSERVICE ==========
  mysql-users:
    image: mysql:8.0
    container_name: mysql-users-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: energy_users_db
    ports:
      - "3307:3306"
    volumes:
      - mysql-users-data:/var/lib/mysql
    networks:
      - energy-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  user-microservice:
    build:
      context: ./user-microservice
      dockerfile: Dockerfile
    container_name: user-microservice-app
    restart: unless-stopped
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-users:3306/energy_users_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SERVER_PORT: 8081
    depends_on:
      mysql-users:
        condition: service_healthy
    networks:
      - energy-network

  # ========== DEVICE MICROSERVICE ==========
  mysql-devices:
    image: mysql:8.0
    container_name: mysql-devices-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: energy_devices_db
    ports:
      - "3308:3306"
    volumes:
      - mysql-devices-data:/var/lib/mysql
    networks:
      - energy-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  device-microservice:
    build:
      context: ./device-microservice
      dockerfile: Dockerfile
    container_name: device-microservice-app
    restart: unless-stopped
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-devices:3306/energy_devices_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SERVER_PORT: 8082
    depends_on:
      mysql-devices:
        condition: service_healthy
    networks:
      - energy-network

  # ========== AUTH MICROSERVICE ==========
  mysql-auth:
    image: mysql:8.0
    container_name: mysql-auth-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: energy_auth_db
    ports:
      - "3309:3306"
    volumes:
      - mysql-auth-data:/var/lib/mysql
    networks:
      - energy-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  auth-microservice:
    build:
      context: ./auth-microservice
      dockerfile: Dockerfile
    container_name: auth-microservice-app
    restart: unless-stopped
    ports:
      - "8083:8083"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-auth:3306/energy_auth_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SERVER_PORT: 8083
      JWT_SECRET: your-secret-key-min-256-bits-for-hs256-algorithm-energy-management
      JWT_EXPIRATION: 86400000
    depends_on:
      mysql-auth:
        condition: service_healthy
    networks:
      - energy-network

networks:
  energy-network:
    driver: bridge

volumes:
  mysql-users-data:
    driver: local
  mysql-devices-data:
    driver: local
  mysql-auth-data:
    driver: local
```

**Apoi rulează:**
```bash
cd energy-management-system
docker-compose up --build
```

---

## 🔍 VERIFICARE ȘI TESTARE

### 1. Verifică containerele în Docker Desktop:

Ar trebui să vezi **6 containere verzi**:
- ✅ mysql-users-db (port 3307)
- ✅ user-microservice-app (port 8081)
- ✅ mysql-devices-db (port 3308)
- ✅ device-microservice-app (port 8082)
- ✅ mysql-auth-db (port 3309)
- ✅ auth-microservice-app (port 8083)

### 2. Testează în Postman:

```bash
# User Microservice
GET http://localhost:8081/actuator/health
GET http://localhost:8081/api/users

# Device Microservice
GET http://localhost:8082/actuator/health
GET http://localhost:8082/api/devices

# Auth Microservice
GET http://localhost:8083/actuator/health
POST http://localhost:8083/api/auth/login
```

### 3. Conectează-te la fiecare MySQL:

```bash
# Users DB (port 3307)
docker exec -it mysql-users-db mysql -u root -proot energy_users_db

# Devices DB (port 3308)
docker exec -it mysql-devices-db mysql -u root -proot energy_devices_db

# Auth DB (port 3309)
docker exec -it mysql-auth-db mysql -u root -proot energy_auth_db
```

---

## 🛑 OPRIRE ȘI CURĂȚARE

```bash
# Oprește toate containerele
docker-compose down

# Oprește și șterge volume-urile (bazele de date)
docker-compose down -v

# Curăță tot (imagini, containere, volume-uri)
docker system prune -a --volumes
```

---

## 📊 REZUMAT PORTURI

| Serviciu | Tip | Port | URL |
|----------|-----|------|-----|
| User App | HTTP | 8081 | http://localhost:8081 |
| User MySQL | MySQL | 3307 | localhost:3307 |
| Device App | HTTP | 8082 | http://localhost:8082 |
| Device MySQL | MySQL | 3308 | localhost:3308 |
| Auth App | HTTP | 8083 | http://localhost:8083 |
| Auth MySQL | MySQL | 3309 | localhost:3309 |

---

**Succes! Acum ai toate cele 3 microservicii configurate corect cu porturi separate! 🚀**
