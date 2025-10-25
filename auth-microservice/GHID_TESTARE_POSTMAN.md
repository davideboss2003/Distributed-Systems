# 🚀 GHID COMPLET - Creare Admin și Testare Postman

## PASUL 1: Generează Hash-ul pentru Parolă

### Opțiunea A: Folosind scriptul Java (RECOMANDAT)

1. **Compilează și rulează scriptul:**
```bash
javac -cp "target\classes;%USERPROFILE%\.m2\repository\org\springframework\security\spring-security-crypto\6.4.2\spring-security-crypto-6.4.2.jar" PasswordHashGenerator.java
java -cp ".;target\classes;%USERPROFILE%\.m2\repository\org\springframework\security\spring-security-crypto\6.4.2\spring-security-crypto-6.4.2.jar" PasswordHashGenerator
```

2. **Copiază hash-ul generat** - va arăta ceva de genul:
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

### Opțiunea B: Folosind un generator online
- Mergi pe: https://bcrypt-generator.com/
- Introdu parola: `admin123`
- Rounds: `10`
- Copiază hash-ul generat

---

## PASUL 2: Verifică că MySQL rulează

```bash
# Verifică dacă MySQL este pornit
mysql -u root -p
```

Dacă nu merge, pornește serviciul MySQL din Services (Windows) sau:
```bash
net start MySQL80
```

---

## PASUL 3: Creează primul ADMIN în baza de date

### Conectează-te la MySQL:
```bash
mysql -u root -p
# Introdu parola: root
```

### Rulează comenzile SQL:
```sql
-- Selectează baza de date
USE energy_auth_db;

-- Verifică dacă există deja useri
SELECT * FROM auth_users;

-- Inserează primul ADMIN (înlocuiește HASH_UL_TAU cu hash-ul generat)
INSERT INTO auth_users (id, username, email, password_hash, role, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'admin',
    'admin@energy.ro',
    'HASH_UL_TAU_AICI',
    'ADMIN',
    NOW(),
    NOW()
);

-- Verifică că s-a creat
SELECT id, username, email, role, created_at FROM auth_users;

-- Ieși din MySQL
EXIT;
```

**EXEMPLU COMPLET cu hash real:**
```sql
INSERT INTO auth_users (id, username, email, password_hash, role, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'admin',
    'admin@energy.ro',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    NOW(),
    NOW()
);
```

---

## PASUL 4: Pornește aplicația Spring Boot

```bash
# Din directorul proiectului
mvnw spring-boot:run

# SAU dacă ai Maven instalat global
mvn spring-boot:run
```

**Verifică că aplicația pornește pe portul 8083:**
```
Started AuthMicroserviceApplication in X.XXX seconds
```

---

## PASUL 5: Testare în Postman

### 🔐 TEST 1: LOGIN cu ADMIN

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

**⚠️ IMPORTANT:** Copiază token-ul din response! Îl vei folosi la următoarele request-uri.

---

### ✅ TEST 2: VALIDARE TOKEN

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8083/api/auth/validate?token=TOKEN_TAU_AICI`
- **Params:**
  - `token`: (lipește token-ul de la login)

**Response așteptat (200 OK):**
```json
true
```

---

### 👤 TEST 3: REGISTER CLIENT (cu token ADMIN)

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8083/api/auth/register`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer TOKEN_TAU_AICI` ⚠️ **OBLIGATORIU!**
- **Body (raw JSON):**
```json
{
    "username": "client1",
    "email": "client1@energy.ro",
    "password": "client123",
    "role": "CLIENT"
}
```

**Response așteptat (200 OK):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "client1",
    "role": "CLIENT"
}
```

---

### 🔐 TEST 4: LOGIN cu CLIENT

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8083/api/auth/login`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
    "email": "client1@energy.ro",
    "password": "client123"
}
```

**Response așteptat (200 OK):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "client1",
    "role": "CLIENT"
}
```

---

### ❌ TEST 5: REGISTER fără token (trebuie să dea eroare)

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8083/api/auth/register`
- **Headers:**
  - `Content-Type: application/json`
  - ⚠️ **FĂRĂ Authorization header**
- **Body (raw JSON):**
```json
{
    "username": "hacker",
    "email": "hacker@evil.com",
    "password": "hack123",
    "role": "ADMIN"
}
```

**Response așteptat (403 Forbidden):**
```json
{
    "timestamp": "2024-10-22T10:30:00.000+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/auth/register"
}
```

---

### ❌ TEST 6: REGISTER cu token CLIENT (trebuie să dea eroare)

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8083/api/auth/register`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer TOKEN_CLIENT_AICI` ⚠️ **Token de la client1**
- **Body (raw JSON):**
```json
{
    "username": "client2",
    "email": "client2@energy.ro",
    "password": "client123",
    "role": "CLIENT"
}
```

**Response așteptat (403 Forbidden):**
```json
{
    "timestamp": "2024-10-22T10:30:00.000+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/auth/register"
}
```

---

## 📊 VERIFICARE FINALĂ în MySQL

```sql
USE energy_auth_db;

-- Vezi toți userii creați
SELECT id, username, email, role, created_at 
FROM auth_users 
ORDER BY created_at;

-- Numără userii pe roluri
SELECT role, COUNT(*) as total 
FROM auth_users 
GROUP BY role;
```

---

## 🐛 TROUBLESHOOTING

### Problema 1: "Access denied for user 'root'@'localhost'"
**Soluție:** Verifică parola în `application.properties` (acum e `root`)

### Problema 2: "Unknown database 'energy_auth_db'"
**Soluție:** Baza se creează automat la primul start. Pornește aplicația o dată.

### Problema 3: "Port 8083 already in use"
**Soluție:** 
```bash
# Windows
netstat -ano | findstr :8083
taskkill /PID <PID_NUMBER> /F
```

### Problema 4: "401 Unauthorized" la register
**Soluție:** Verifică că ai pus header-ul `Authorization: Bearer TOKEN`

### Problema 5: "403 Forbidden" la register cu token valid
**Soluție:** Verifică că token-ul este de la un user cu rol ADMIN

### Problema 6: Token expirat
**Soluție:** Token-ul expiră după 24h (86400000ms). Fă login din nou.

---

## 📝 NOTIȚE IMPORTANTE

1. **Token-ul JWT conține:**
   - `userId` (UUID)
   - `username`
   - `role` (ADMIN sau CLIENT)
   - `iat` (issued at)
   - `exp` (expiration - 24h)

2. **Securitate:**
   - `/api/auth/login` - PUBLIC (oricine)
   - `/api/auth/validate` - PUBLIC (oricine)
   - `/api/auth/register` - PROTEJAT (doar ADMIN)

3. **Roluri:**
   - `ADMIN` - poate crea alți useri (ADMIN sau CLIENT)
   - `CLIENT` - user normal, nu poate crea alți useri

4. **Validări:**
   - Username: 3-50 caractere, unic
   - Email: unic
   - Password: minim 6 caractere
   - Role: doar "ADMIN" sau "CLIENT"

---

## ✅ CHECKLIST FINAL

- [ ] MySQL pornit și accesibil
- [ ] Baza de date `energy_auth_db` creată
- [ ] Primul ADMIN inserat în `auth_users`
- [ ] Aplicația Spring Boot pornită pe port 8083
- [ ] Login ADMIN funcționează
- [ ] Token JWT primit și valid
- [ ] Register CLIENT cu token ADMIN funcționează
- [ ] Login CLIENT funcționează
- [ ] Register fără token dă 403
- [ ] Register cu token CLIENT dă 403

---

**Succes! 🎉**
