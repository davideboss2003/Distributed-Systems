# 🚀 GHID COMPLET CRUD - Device Microservice în Postman

## 📋 PREGĂTIRE

### 1️⃣ Obține Token ADMIN

**POST** `http://localhost:8083/api/auth/login`

**Headers:**
- `Content-Type: application/json`

**Body (raw JSON):**
```json
{
  "email": "admin@energy.ro",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlZmJiZjQ5MS0wODBkLTgwNjAtODBhMC04MGQwMDAwMDAwMDAiLCJ1c2VybmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzI5ODYxMjE2LCJleHAiOjE3Mjk5NDc2MTZ9.xyz...",
  "type": "Bearer",
  "userId": "efbbf491-080d-8060-80a0-80d000000000",
  "username": "admin",
  "role": "ADMIN"
}
```

**⚠️ COPIAZĂ TOKEN-UL!** Îl vei folosi la toate request-urile.

---

## 📊 TOATE OPERAȚIILE CRUD

### ✅ 1. CREATE DEVICE (doar ADMIN)

**POST** `http://localhost:8082/api/devices`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer TOKEN_ADMIN_AICI` ⚠️ **OBLIGATORIU!**

**Body (raw JSON):**
```json
{
  "description": "Smart TV Samsung 55 inch",
  "address": "Strada Florilor nr. 10, Cluj-Napoca",
  "maximumHourlyConsumption": 0.15,
  "userId": "efbbf491-080d-8060-80a0-80d000000000"
}
```

**Response (201 Created):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "description": "Smart TV Samsung 55 inch",
  "address": "Strada Florilor nr. 10, Cluj-Napoca",
  "maximumHourlyConsumption": 0.15,
  "userId": "efbbf491-080d-8060-80a0-80d000000000",
  "username": "admin",
  "userEmail": "admin@energy.ro",
  "createdAt": "2025-10-25T15:30:00",
  "updatedAt": "2025-10-25T15:30:00"
}
```

**✅ Observă:** Acum response-ul include și `username` și `userEmail`!

---

### 📖 2. GET ALL DEVICES (doar ADMIN)

**GET** `http://localhost:8082/api/devices`

**Headers:**
- `Authorization: Bearer TOKEN_ADMIN_AICI`

**Response (200 OK):**
```json
[
  {
    "id": "d1111111-1111-1111-1111-111111111111",
    "description": "Smart Refrigerator LG InstaView",
    "address": "123 Carrot Lane, Bunny Town",
    "maximumHourlyConsumption": 1.5,
    "userId": "11111111-1111-1111-1111-111111111111",
    "username": "bugsbunny",
    "userEmail": "bugs@looney.com",
    "createdAt": "2025-10-18T14:47:29",
    "updatedAt": "2025-10-18T14:47:29"
  },
  {
    "id": "d2222222-2222-2222-2222-222222222221",
    "description": "Air Conditioner Daikin",
    "address": "456 Duck Pond Ave",
    "maximumHourlyConsumption": 3.5,
    "userId": "22222222-2222-2222-2222-222222222222",
    "username": "daffyduck",
    "userEmail": "daffy@looney.com",
    "createdAt": "2025-10-18T14:47:29",
    "updatedAt": "2025-10-18T14:47:29"
  }
]
```

---

### 🔍 3. GET DEVICE BY ID (ADMIN sau CLIENT)

**GET** `http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111111`

**Headers:**
- `Authorization: Bearer TOKEN_AICI`

**Response (200 OK):**
```json
{
  "id": "d1111111-1111-1111-1111-111111111111",
  "description": "Smart Refrigerator LG InstaView",
  "address": "123 Carrot Lane, Bunny Town",
  "maximumHourlyConsumption": 1.5,
  "userId": "11111111-1111-1111-1111-111111111111",
  "username": "bugsbunny",
  "userEmail": "bugs@looney.com",
  "createdAt": "2025-10-18T14:47:29",
  "updatedAt": "2025-10-18T14:47:29"
}
```

**Response la ID inexistent (404 Not Found):**
```json
{
  "timestamp": "2025-10-25T15:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Device not found with id: 00000000-0000-0000-0000-000000000000"
}
```

---

### 👤 4. GET DEVICES BY USER ID (ADMIN sau CLIENT propriu)

**GET** `http://localhost:8082/api/devices/user/11111111-1111-1111-1111-111111111111`

**Headers:**
- `Authorization: Bearer TOKEN_ADMIN_AICI`

**Response (200 OK):**
```json
[
  {
    "id": "d1111111-1111-1111-1111-111111111111",
    "description": "Smart Refrigerator LG InstaView",
    "address": "123 Carrot Lane, Bunny Town",
    "maximumHourlyConsumption": 1.5,
    "userId": "11111111-1111-1111-1111-111111111111",
    "username": "bugsbunny",
    "userEmail": "bugs@looney.com",
    "createdAt": "2025-10-18T14:47:29",
    "updatedAt": "2025-10-18T14:47:29"
  },
  {
    "id": "d1111111-1111-1111-1111-111111111112",
    "description": "Samsung Washing Machine",
    "address": "123 Carrot Lane, Bunny Town",
    "maximumHourlyConsumption": 2.2,
    "userId": "11111111-1111-1111-1111-111111111111",
    "username": "bugsbunny",
    "userEmail": "bugs@looney.com",
    "createdAt": "2025-10-18T14:47:29",
    "updatedAt": "2025-10-18T14:47:29"
  }
]
```

---

### 🔄 5. UPDATE DEVICE (doar ADMIN)

**PUT** `http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111111`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer TOKEN_ADMIN_AICI`

**Body (raw JSON) - Update parțial:**
```json
{
  "description": "Smart Refrigerator LG InstaView - UPDATED",
  "maximumHourlyConsumption": 1.8
}
```

**Response (200 OK):**
```json
{
  "id": "d1111111-1111-1111-1111-111111111111",
  "description": "Smart Refrigerator LG InstaView - UPDATED",
  "address": "123 Carrot Lane, Bunny Town",
  "maximumHourlyConsumption": 1.8,
  "userId": "11111111-1111-1111-1111-111111111111",
  "username": "bugsbunny",
  "userEmail": "bugs@looney.com",
  "createdAt": "2025-10-18T14:47:29",
  "updatedAt": "2025-10-25T15:35:00"
}
```

**✅ Observă:** Doar câmpurile trimise sunt actualizate!

---

### 🔗 6. ASSIGN DEVICE TO USER (doar ADMIN)

**PUT** `http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111111/assign/22222222-2222-2222-2222-222222222222`

**Headers:**
- `Authorization: Bearer TOKEN_ADMIN_AICI`

**Response (200 OK):**
```json
{
  "id": "d1111111-1111-1111-1111-111111111111",
  "description": "Smart Refrigerator LG InstaView - UPDATED",
  "address": "123 Carrot Lane, Bunny Town",
  "maximumHourlyConsumption": 1.8,
  "userId": "22222222-2222-2222-2222-222222222222",
  "username": "daffyduck",
  "userEmail": "daffy@looney.com",
  "createdAt": "2025-10-18T14:47:29",
  "updatedAt": "2025-10-25T15:40:00"
}
```

**✅ Observă:** 
- `userId` s-a schimbat de la `11111111...` la `22222222...`
- `username` și `userEmail` s-au actualizat automat!
- User-ul a fost sincronizat din User Microservice

---

### ❌ 7. DELETE DEVICE (doar ADMIN)

**DELETE** `http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111112`

**Headers:**
- `Authorization: Bearer TOKEN_ADMIN_AICI`

**Response (204 No Content):**
```
(fără body, doar status 204)
```

**Verificare:**
```bash
# Încearcă să obții device-ul șters
GET http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111112
# Ar trebui să primești 404 Not Found
```

---

## ❌ TESTE DE SECURITATE

### ❌ 8. CLIENT încearcă să vadă toate device-urile (403)

**GET** `http://localhost:8082/api/devices`

**Headers:**
- `Authorization: Bearer TOKEN_CLIENT_AICI` ⚠️ **Token de la CLIENT**

**Response (403 Forbidden):**
```json
{
  "timestamp": "2025-10-25T15:45:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/devices"
}
```

---

### ❌ 9. CLIENT încearcă să creeze device (403)

**POST** `http://localhost:8082/api/devices`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer TOKEN_CLIENT_AICI`

**Body:**
```json
{
  "description": "Hacker Device",
  "address": "Evil Street",
  "maximumHourlyConsumption": 999.9
}
```

**Response (403 Forbidden):**
```json
{
  "timestamp": "2025-10-25T15:45:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/devices"
}
```

---

### ❌ 10. Request FĂRĂ token (403)

**GET** `http://localhost:8082/api/devices`

**Headers:**
- ⚠️ **FĂRĂ Authorization header**

**Response (403 Forbidden):**
```json
{
  "timestamp": "2025-10-25T15:45:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/devices"
}
```

---

### ❌ 11. CREATE cu date invalide (400)

**POST** `http://localhost:8082/api/devices`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer TOKEN_ADMIN_AICI`

**Body - description lipsă:**
```json
{
  "address": "Test Address",
  "maximumHourlyConsumption": 5.0
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2025-10-25T15:45:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Description is required"
}
```

**Body - consumption negativ:**
```json
{
  "description": "Test Device",
  "address": "Test Address",
  "maximumHourlyConsumption": -5.0
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2025-10-25T15:45:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Maximum hourly consumption must be positive"
}
```

---

### ❌ 12. ASSIGN la user inexistent (400)

**PUT** `http://localhost:8082/api/devices/d1111111-1111-1111-1111-111111111111/assign/00000000-0000-0000-0000-000000000000`

**Headers:**
- `Authorization: Bearer TOKEN_ADMIN_AICI`

**Response (400 Bad Request):**
```json
{
  "timestamp": "2025-10-25T15:45:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User not found with id: 00000000-0000-0000-0000-000000000000"
}
```

---

## 📊 MATRICE DE AUTORIZARE

| Endpoint | Method | ADMIN | CLIENT | Fără Token |
|----------|--------|-------|--------|------------|
| `/api/devices` | GET | ✅ | ❌ | ❌ |
| `/api/devices/{id}` | GET | ✅ | ✅ | ❌ |
| `/api/devices/user/{userId}` | GET | ✅ | ✅ (doar propriul userId) | ❌ |
| `/api/devices` | POST | ✅ | ❌ | ❌ |
| `/api/devices/{id}` | PUT | ✅ | ❌ | ❌ |
| `/api/devices/{id}` | DELETE | ✅ | ❌ | ❌ |
| `/api/devices/{id}/assign/{userId}` | PUT | ✅ | ❌ | ❌ |

---

## 🎯 SCENARII COMPLETE DE TESTARE

### Scenariu 1: ADMIN gestionează device-uri

1. **Login ca ADMIN**
   ```
   POST http://localhost:8083/api/auth/login
   Body: {"email": "admin@energy.ro", "password": "admin123"}
   ```

2. **Vezi toate device-urile**
   ```
   GET http://localhost:8082/api/devices
   Header: Authorization: Bearer TOKEN
   ```

3. **Creează device nou**
   ```
   POST http://localhost:8082/api/devices
   Header: Authorization: Bearer TOKEN
   Body: {
     "description": "New Device",
     "address": "Test Street 123",
     "maximumHourlyConsumption": 2.5,
     "userId": "efbbf491-080d-8060-80a0-80d000000000"
   }
   ```

4. **Actualizează device**
   ```
   PUT http://localhost:8082/api/devices/{id}
   Header: Authorization: Bearer TOKEN
   Body: {"maximumHourlyConsumption": 3.0}
   ```

5. **Transferă device la alt user**
   ```
   PUT http://localhost:8082/api/devices/{deviceId}/assign/{newUserId}
   Header: Authorization: Bearer TOKEN
   ```

6. **Șterge device**
   ```
   DELETE http://localhost:8082/api/devices/{id}
   Header: Authorization: Bearer TOKEN
   ```

---

### Scenariu 2: CLIENT accesează propriile device-uri

1. **Login ca CLIENT**
   ```
   POST http://localhost:8083/api/auth/login
   Body: {"email": "client1@energy.ro", "password": "client123"}
   ```

2. **Încearcă să vadă toate device-urile** ❌
   ```
   GET http://localhost:8082/api/devices
   Header: Authorization: Bearer TOKEN_CLIENT
   Result: 403 Forbidden
   ```

3. **Vezi propriile device-uri** ✅
   ```
   GET http://localhost:8082/api/devices/user/{ownUserId}
   Header: Authorization: Bearer TOKEN_CLIENT
   Result: 200 OK
   ```

4. **Vezi un device specific** ✅
   ```
   GET http://localhost:8082/api/devices/{id}
   Header: Authorization: Bearer TOKEN_CLIENT
   Result: 200 OK
   ```

5. **Încearcă să creeze device** ❌
   ```
   POST http://localhost:8082/api/devices
   Header: Authorization: Bearer TOKEN_CLIENT
   Result: 403 Forbidden
   ```

---

## 🆕 NOI FUNCȚIONALITĂȚI (cu tabela User locală)

### Ce s-a schimbat?

1. **Response-urile includ acum informații despre user:**
   ```json
   {
     "userId": "11111111-1111-1111-1111-111111111111",
     "username": "bugsbunny",      ← NOU!
     "userEmail": "bugs@looney.com" ← NOU!
   }
   ```

2. **Sincronizare automată:**
   - Când creezi/actualizezi/asignezi device cu userId, user-ul se sincronizează automat din User Microservice
   - Datele user-ului sunt stocate local în Device Microservice

3. **Queries mai rapide:**
   - Nu mai face REST call la User Microservice pentru fiecare device
   - Datele sunt disponibile imediat din baza locală

---

## ✅ CHECKLIST TESTARE COMPLETĂ

- [ ] Login ADMIN funcționează
- [ ] Token ADMIN este valid
- [ ] CREATE device cu userId valid (sincronizare user)
- [ ] CREATE device fără userId (device neasignat)
- [ ] GET all devices (vezi username și email)
- [ ] GET device by ID
- [ ] GET devices by user ID
- [ ] UPDATE device (parțial)
- [ ] ASSIGN device la alt user (sincronizare user nou)
- [ ] DELETE device
- [ ] CLIENT nu poate accesa endpoint-uri ADMIN (403)
- [ ] Request fără token dă 403
- [ ] Validări funcționează (date invalide → 400)
- [ ] User inexistent la assign → 400

---

**Succes la testare! 🎉**
