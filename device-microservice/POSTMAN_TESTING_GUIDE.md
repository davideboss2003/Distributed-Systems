# 🚀 GHID COMPLET - Testare Device Microservice în Postman

## 📋 CUPRINS
1. [Pregătire Inițială](#pasul-1-pregătire-inițială)
2. [Pornire Aplicație](#pasul-2-pornește-aplicația)
3. [Obținere Token JWT](#pasul-3-obține-token-jwt-de-la-auth-microservice)
4. [Testare Endpoints](#pasul-4-testare-endpoints-în-postman)
5. [Troubleshooting](#troubleshooting)

---

## PASUL 1: Pregătire Inițială

### ✅ Verifică că MySQL rulează și baza de date există

```bash
# Verifică dacă MySQL este pornit
mysql -u root -p
```

Dacă nu merge, pornește serviciul:
```bash
net start MySQL80
```

### ✅ Verifică datele existente în baza de date

```sql
-- Conectează-te la MySQL
mysql -u root -p
# Parola: root

-- Selectează baza de date
USE energy_devices_db;

-- Verifică structura tabelei
DESCRIBE devices;

-- Vezi toate device-urile existente
SELECT 
    id,
    description,
    address,
    maximum_hourly_consumption,
    user_id
FROM devices
LIMIT 10;

-- Ieși din MySQL
EXIT;
```

**✅ Baza de date este deja populată cu device-uri pentru diferiți useri!**

---

## PASUL 2: Pornește aplicația

```bash
# Din directorul device-microservice
mvnw spring-boot:run

# SAU dacă ai Maven instalat global
mvn spring-boot:run
```

**Verifică că aplicația pornește pe portul 8082:**

```
Started DeviceMicroserviceApplication in X.XXX seconds (JVM running for Y.YYY)
```

**⚠️ IMPORTANT:** Aplicația trebuie să ruleze pe **port 8082** (verifică în `application.properties`)

---

## PASUL 3: Obține Token JWT de la Auth Microservice

### 🔐 Asigură-te că Auth Microservice rulează

```bash
# Auth microservice trebuie să ruleze pe port 8083
# Verifică în alt terminal
curl http://localhost:8083/actuator/health
```

### 🔐 LOGIN cu ADMIN (pentru a obține token)

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8083/api/auth/login`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**

```json
{
  "email": "admin@energy.ro",
  "password": "admin123"
}
```

**Response așteptat (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJ1c2VybmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzI5NjE2NDAwLCJleHAiOjE3Mjk3MDI4MDB9.xyz...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "admin",
  "role": "ADMIN"
}
```

**⚠️ COPIAZĂ TOKEN-UL!** Îl vei folosi la toate request-urile către Device Microservice.

### 🔐 LOGIN cu CLIENT (opțional, pentru teste)

**Body (raw JSON):**

```json
{
  "email": "client1@energy.ro",
  "password": "client123"
}
```

---

## PASUL 4: Testare Endpoints în Postman

### 📊 TEST 1: GET ALL DEVICES (doar ADMIN)

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8082/api/devices`
- **Headers:**
  - `Authorization: Bearer TOKEN_ADMIN_AICI` ⚠️ **OBLIGATORIU!**

**Response așteptat (200 OK):**

Vei primi o listă cu TOATE device-urile din baza de date (aproximativ 20+ device-uri):

```json
[
  {
    "id": "d1111111-1111-1111-1111-111111111111",
    "description": "Smart Refrigerator LG InstaView",
    "address": "123 Carrot Lane, Bunny Town",
    "maximumHourlyConsumption": 1.5,
    "userId": "11111111-1111-1111-1111-111111111111",
    "createdAt": "2025-10-18T14:47:29",
    "updatedAt": "2025-10-18T14:47:29"
  },
  {
    "id": "d1111111-1111-1111-1111-111111111112",
    "description": "Samsung Washing Machine EcoBubble",
    "address": "123 Carrot Lane, Bunny Town",
    "maximumHourlyConsumption": 2.2,
    "userId": "11111111-1111-1111-1111-111111111111",
    "createdAt": "2025-10-18T14:47:29",
    "updatedAt": "2025-10-18T14:47:29"
  },
  {
    "id": "d2222222-2222-2222-2222-222222222221",
    "description": "Air Conditioner Daikin Split 12000 BTU",
    "address": "456 Duck Pond Ave, Duck City",
    "maximumHourlyConsumption": 3.5,
    "userId": "22222222-2222-2222-2222-222222222222",
    "createdAt": "2025-10-18T14:47:29",
    "updatedAt": "2025-10-18T14:47:29"
  }
  // ... și multe altele
]
```

**✅ Verifică:** Trebuie să vezi toate device-urile din baza ta de date!

---

### 🔍 TEST 2: GET DEVICE BY ID (ADMIN sau CLIENT)

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111111`
- **Headers:**
  - `Authorization: Bearer TOKEN_AICI`

**Response așteptat (200 OK):**

```json
{
  "id": "d1111111-1111-1111-1111-111111111111",
  "description": "Smart Refrigerator LG InstaView",
  "address": "123 Carrot Lane, Bunny Town",
  "maximumHourlyConsumption": 1.5,
  "userId": "11111111-1111-1111-1111-111111111111",
  "createdAt": "2025-10-18T14:47:29",
  "updatedAt": "2025-10-18T14:47:29"
}
```

**✅ Încearcă și cu alte ID-uri din baza ta:**
- `d2222222-2222-2222-2222-222222222221` - Air Conditioner
- `d3333333-3333-3333-3333-333333333331` - Dishwasher Bosch
- `d4444444-4444-4444-4444-444444444441` - LED TV Samsung

**Response la ID inexistent (404 Not Found):**

```json
{
  "timestamp": "2025-10-18T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Device not found with id: 00000000-0000-0000-0000-000000000000",
  "path": "/api/devices/00000000-0000-0000-0000-000000000000"
}
```

---

### 👤 TEST 3: GET DEVICES BY USER ID (ADMIN sau CLIENT propriu)

**Request cu token ADMIN:**
- **Method:** `GET`
- **URL:** `http://localhost:8082/api/devices/user/11111111-1111-1111-1111-111111111111`
- **Headers:**
  - `Authorization: Bearer TOKEN_ADMIN_AICI`

**Response așteptat (200 OK):**

Vei primi device-urile pentru user-ul Bugs Bunny (ADMIN):

```json
[
  {
    "id": "d1111111-1111-1111-1111-111111111111",
    "description": "Smart Refrigerator LG InstaView",
    "address": "123 Carrot Lane, Bunny Town",
    "maximumHourlyConsumption": 1.5,
    "userId": "11111111-1111-1111-1111-111111111111",
    "createdAt": "2025-10-18T14:47:29",
    "updatedAt": "2025-10-18T14:47:29"
  },
  {
    "id": "d1111111-1111-1111-1111-111111111112",
    "description": "Samsung Washing Machine EcoBubble",
    "address": "123 Carrot Lane, Bunny Town",
    "maximumHourlyConsumption": 2.2,
    "userId": "11111111-1111-1111-1111-111111111111",
    "createdAt": "2025-10-18T14:47:29",
    "updatedAt": "2025-10-18T14:47:29"
  }
]
```

**✅ Încearcă și cu alți useri:**
- `22222222-2222-2222-2222-222222222222` - Daffy Duck (3 devices)
- `33333333-3333-3333-3333-333333333333` - Porky Pig (2 devices)
- `77777777-7777-7777-7777-777777777777` - Yosemite Sam (3 devices)

---

### ✅ TEST 4: CREATE DEVICE (doar ADMIN)

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8082/api/devices`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer TOKEN_ADMIN_AICI` ⚠️ **OBLIGATORIU!**
- **Body (raw JSON):**

```json
{
  "description": "Tesla Model 3 Charger",
  "address": "999 Electric Avenue, Tech City",
  "maximumHourlyConsumption": 11.5,
  "userId": "11111111-1111-1111-1111-111111111111"
}
```

**Response așteptat (201 Created):**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "description": "Tesla Model 3 Charger",
  "address": "999 Electric Avenue, Tech City",
  "maximumHourlyConsumption": 11.5,
  "userId": "11111111-1111-1111-1111-111111111111",
  "createdAt": "2025-10-18T15:00:00",
  "updatedAt": "2025-10-18T15:00:00"
}
```

**✅ Poți asigna device-ul oricărui user existent:**
- `11111111-1111-1111-1111-111111111111` - Bugs Bunny (ADMIN)
- `22222222-2222-2222-2222-222222222222` - Daffy Duck
- `33333333-3333-3333-3333-333333333333` - Porky Pig

**⚠️ CREATE fără userId (device neasignat):**

```json
{
  "description": "Unassigned Smart Meter",
  "address": "Storage Room 5",
  "maximumHourlyConsumption": 0.5
}
```

---

### 🔄 TEST 5: UPDATE DEVICE (doar ADMIN)

**Request:**
- **Method:** `PUT`
- **URL:** `http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111111`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer TOKEN_ADMIN_AICI`
- **Body (raw JSON) - Update parțial:**

```json
{
  "description": "Smart Refrigerator LG InstaView - UPDATED",
  "maximumHourlyConsumption": 1.8
}
```

**Response așteptat (200 OK):**

```json
{
  "id": "d1111111-1111-1111-1111-111111111111",
  "description": "Smart Refrigerator LG InstaView - UPDATED",
  "address": "123 Carrot Lane, Bunny Town",
  "maximumHourlyConsumption": 1.8,
  "userId": "11111111-1111-1111-1111-111111111111",
  "createdAt": "2025-10-18T14:47:29",
  "updatedAt": "2025-10-18T15:30:00"
}
```

**✅ Observă:** Doar câmpurile trimise sunt actualizate, restul rămân neschimbate!

---

### 🔗 TEST 6: ASSIGN DEVICE TO USER (doar ADMIN)

**Request - Transferă frigiderul de la Bugs Bunny la Daffy Duck:**
- **Method:** `PUT`
- **URL:** `http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111111/assign/22222222-2222-2222-2222-222222222222`
- **Headers:**
  - `Authorization: Bearer TOKEN_ADMIN_AICI`

**Response așteptat (200 OK):**

```json
{
  "id": "d1111111-1111-1111-1111-111111111111",
  "description": "Smart Refrigerator LG InstaView",
  "address": "123 Carrot Lane, Bunny Town",
  "maximumHourlyConsumption": 1.5,
  "userId": "22222222-2222-2222-2222-222222222222",
  "createdAt": "2025-10-18T14:47:29",
  "updatedAt": "2025-10-18T15:45:00"
}
```

**✅ Verifică:** userId s-a schimbat de la `11111111...` la `22222222...`!

**⚠️ Assign la user inexistent (400 Bad Request):**

```json
{
  "timestamp": "2025-10-18T15:45:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User not found with id: 00000000-0000-0000-0000-000000000000",
  "path": "/api/devices/d1111111-1111-1111-1111-111111111111/assign/00000000-0000-0000-0000-000000000000"
}
```

---

### ❌ TEST 7: DELETE DEVICE (doar ADMIN)

**Request - Șterge Samsung Washing Machine:**
- **Method:** `DELETE`
- **URL:** `http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111112`
- **Headers:**
  - `Authorization: Bearer TOKEN_ADMIN_AICI`

**Response așteptat (204 No Content):**

```
(fără body, doar status 204)
```

**✅ Verificare în MySQL:**

```sql
USE energy_devices_db;

-- Verifică că device-ul a fost șters
SELECT id, description FROM devices WHERE id = 'd1111111-1111-1111-1111-111111111112';
-- Trebuie să returneze 0 rows

-- Verifică câte device-uri mai are Bugs Bunny
SELECT COUNT(*) FROM devices WHERE user_id = '11111111-1111-1111-1111-111111111111';
```

**⚠️ ATENȚIE:** Odată șters, device-ul nu mai poate fi recuperat!

---

### ❌ TEST 8: ACCES INTERZIS - CLIENT încearcă să vadă toate device-urile

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8082/api/devices`
- **Headers:**
  - `Authorization: Bearer TOKEN_CLIENT_AICI` ⚠️ **Token de la CLIENT**

**Response așteptat (403 Forbidden):**

```json
{
  "timestamp": "2024-10-22T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/devices"
}
```

---

### ❌ TEST 9: ACCES INTERZIS - CLIENT încearcă să creeze device

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8082/api/devices`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer TOKEN_CLIENT_AICI`
- **Body (raw JSON):**

```json
{
  "description": "Hacker Device",
  "address": "Evil Street",
  "maximumHourlyConsumption": 999.9
}
```

**Response așteptat (403 Forbidden):**

```json
{
  "timestamp": "2024-10-22T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/devices"
}
```

---

### ❌ TEST 10: ACCES FĂRĂ TOKEN

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8082/api/devices`
- **Headers:**
  - ⚠️ **FĂRĂ Authorization header**

**Response așteptat (403 Forbidden):**

```json
{
  "timestamp": "2024-10-22T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/devices"
}
```

---

### ❌ TEST 11: VALIDARE - Creare device cu date invalide

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8082/api/devices`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer TOKEN_ADMIN_AICI`
- **Body (raw JSON) - description lipsă:**

```json
{
  "address": "Test Address",
  "maximumHourlyConsumption": 5.0
}
```

**Response așteptat (400 Bad Request):**

```json
{
  "timestamp": "2024-10-22T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Description is required",
  "path": "/api/devices"
}
```

**Body (raw JSON) - consumption negativ:**

```json
{
  "description": "Test Device",
  "address": "Test Address",
  "maximumHourlyConsumption": -5.0
}
```

**Response așteptat (400 Bad Request):**

```json
{
  "timestamp": "2024-10-22T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Maximum hourly consumption must be positive",
  "path": "/api/devices"
}
```

---

## 📊 VERIFICARE FINALĂ în MySQL

```sql
USE energy_devices_db;

-- Vezi toate device-urile
SELECT 
    id as device_id,
    description,
    address,
    maximum_hourly_consumption,
    user_id,
    created_at,
    updated_at
FROM devices
ORDER BY created_at DESC;

-- Numără device-urile per user
SELECT 
    user_id,
    COUNT(*) as total_devices,
    SUM(maximum_hourly_consumption) as total_consumption
FROM devices
WHERE user_id IS NOT NULL
GROUP BY user_id;

-- Device-uri neasignate
SELECT 
    id as device_id,
    description,
    address
FROM devices
WHERE user_id IS NULL;

-- Top 5 device-uri cu cel mai mare consum
SELECT 
    id,
    description,
    maximum_hourly_consumption,
    user_id
FROM devices
ORDER BY maximum_hourly_consumption DESC
LIMIT 5;
```

---

## 🐛 TROUBLESHOOTING

### Problema 1: "Access denied for user 'root'@'localhost'"

**Soluție:** Verifică parola în `application.properties`:
```properties
spring.datasource.password=root
```

### Problema 2: "Unknown database 'energy_devices_db'"

**Soluție:** Creează baza de date manual:
```sql
CREATE DATABASE energy_devices_db;
```

### Problema 3: "Port 8082 already in use"

**Soluție:**
```bash
# Windows
netstat -ano | findstr :8082
taskkill /PID <PID_NUMBER> /F
```

### Problema 4: "403 Forbidden" cu token valid

**Cauze posibile:**
1. Token-ul este de la CLIENT, dar endpoint-ul necesită ADMIN
2. Token-ul a expirat (24h)
3. JWT secret diferit între auth-microservice și device-microservice

**Soluție:** Verifică că `jwt.secret` este identic în ambele `application.properties`:
```properties
jwt.secret=your-secret-key-min-256-bits-for-hs256-algorithm-energy-management
```

### Problema 5: "User not found" la assign/create

**Cauză:** User-microservice nu rulează sau user-ul nu există

**Soluție:**
```bash
# Verifică că user-microservice rulează pe port 8081
curl http://localhost:8081/actuator/health

# Verifică că user-ul există
curl -H "Authorization: Bearer TOKEN_ADMIN" http://localhost:8081/api/users/11111111-1111-1111-1111-111111111111
```

### Problema 6: "Could not extract ResultSet" sau erori SQL

**Cauză:** Tabela `devices` nu există sau are structură greșită

**Soluție:** Recreează tabela:
```sql
USE energy_devices_db;
DROP TABLE IF EXISTS devices;

CREATE TABLE devices (
    id BINARY(16) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    maximum_hourly_consumption DOUBLE NOT NULL,
    user_id BINARY(16),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Problema 7: Token JWT invalid sau malformat

**Cauză:** Token copiat greșit sau cu spații

**Soluție:** 
- Asigură-te că token-ul începe cu `eyJ`
- Nu include `Bearer ` în token când îl copiezi
- Header-ul trebuie să fie: `Authorization: Bearer eyJ...`

---

## 📝 MATRICE DE AUTORIZARE

| Endpoint | ADMIN | CLIENT | Fără Token |
|----------|-------|--------|------------|
| GET /api/devices | ✅ | ❌ | ❌ |
| GET /api/devices/{id} | ✅ | ✅ | ❌ |
| GET /api/devices/user/{userId} | ✅ | ✅ (doar propriul userId) | ❌ |
| POST /api/devices | ✅ | ❌ | ❌ |
| PUT /api/devices/{id} | ✅ | ❌ | ❌ |
| DELETE /api/devices/{id} | ✅ | ❌ | ❌ |
| PUT /api/devices/{id}/assign/{userId} | ✅ | ❌ | ❌ |

---

## 📝 VALIDĂRI IMPORTANTE

### CreateDeviceDTO:
- `description`: **OBLIGATORIU**, max 255 caractere
- `address`: **OBLIGATORIU**, max 255 caractere
- `maximumHourlyConsumption`: **OBLIGATORIU**, trebuie să fie pozitiv
- `userId`: **OPȚIONAL**, dar dacă e furnizat, user-ul trebuie să existe

### UpdateDeviceDTO:
- Toate câmpurile sunt **OPȚIONALE**
- Doar câmpurile furnizate vor fi actualizate
- `maximumHourlyConsumption`: dacă e furnizat, trebuie să fie pozitiv
- `userId`: dacă e furnizat, user-ul trebuie să existe

---

## ✅ CHECKLIST FINAL

- [ ] MySQL pornit și accesibil
- [ ] Baza de date `energy_devices_db` creată
- [ ] Tabela `devices` creată cu structura corectă
- [ ] Date de test inserate din `data.sql`
- [ ] Auth Microservice pornit pe port 8083
- [ ] Device Microservice pornit pe port 8082
- [ ] Token ADMIN obținut de la auth-microservice
- [ ] GET all devices funcționează (doar ADMIN)
- [ ] GET device by ID funcționează
- [ ] GET devices by user ID funcționează
- [ ] CREATE device funcționează (doar ADMIN)
- [ ] UPDATE device funcționează (doar ADMIN)
- [ ] ASSIGN device to user funcționează (doar ADMIN)
- [ ] DELETE device funcționează (doar ADMIN)
- [ ] CLIENT nu poate accesa endpoint-uri ADMIN (403)
- [ ] Request fără token dă 403
- [ ] Validările funcționează corect

---

## 🎯 SCENARII DE TESTARE COMPLETE

### Scenariu 1: ADMIN gestionează device-uri

1. Login ca ADMIN → obține token
2. GET all devices → vezi toate device-urile
3. CREATE new device → device creat cu succes
4. UPDATE device → device actualizat
5. ASSIGN device to user → device asignat
6. DELETE device → device șters

### Scenariu 2: CLIENT accesează propriile device-uri

1. Login ca CLIENT → obține token
2. GET all devices → 403 Forbidden ❌
3. GET devices by own userId → vezi propriile device-uri ✅
4. GET device by ID (propriul device) → vezi detalii ✅
5. CREATE device → 403 Forbidden ❌
6. UPDATE device → 403 Forbidden ❌

### Scenariu 3: Validări și erori

1. CREATE device fără description → 400 Bad Request
2. CREATE device cu consumption negativ → 400 Bad Request
3. UPDATE device inexistent → 404 Not Found
4. ASSIGN device la user inexistent → 400 Bad Request
5. DELETE device inexistent → 404 Not Found

---

**Succes! 🎉**

**Microserviciul Device este complet funcțional și securizat cu JWT!**
