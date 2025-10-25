# 🚀 GHID COMPLET - Testare Microserviciu Users în Postman

## 📋 PREZENTARE GENERALĂ

**Microserviciul Users** gestionează utilizatorii din sistemul Energy Management:
- **Port:** 8081
- **Bază de date:** `energy_users_db` (MySQL)
- **Securitate:** DEZACTIVATĂ temporar pentru testare (toate endpoint-urile sunt publice)
- **Roluri:** ADMIN și CLIENT

---

## PASUL 1: Verifică că MySQL rulează

```bash
# Verifică dacă MySQL este pornit
mysql -u root -p
```

Dacă nu merge, pornește serviciul MySQL:
```bash
net start MySQL80
```

---

## PASUL 2: Verifică baza de date și userii existenți

### Conectează-te la MySQL:
```bash
mysql -u root -p
# Introdu parola: root
```

### Verifică datele existente:
```sql
-- Selectează baza de date
USE energy_users_db;

-- Verifică structura tabelului
DESCRIBE users;

-- Vezi toți userii existenți
SELECT id, username, email, role, full_name, created_at 
FROM users 
ORDER BY created_at;

-- Numără userii pe roluri
SELECT role, COUNT(*) as total 
FROM users 
GROUP BY role;
```

**⚠️ IMPORTANT:** Ai deja 10 useri în baza de date:
- **3 ADMIN:** bugs.bunny, porky.pig, yosemite.sam
- **7 CLIENT:** daffy.duck, tweety.bird, sylvester.cat, road.runner, wile.e.coyote, marvin.martian, tasmanian.devil

**Parolele lor sunt hash-uite cu BCrypt** (încep cu `$2a$10$`), deci nu le poți vedea în clar.

```sql
-- Ieși din MySQL
EXIT;
```

---

## PASUL 3: Pornește aplicația Spring Boot

```bash
# Din directorul proiectului
mvnw spring-boot:run

# SAU dacă ai Maven instalat global
mvn spring-boot:run
```

**Verifică că aplicația pornește pe portul 8081:**
```
Started UserMicroserviceApplication in X.XXX seconds
```

---

## PASUL 4: Useri existenți în baza de date

**Ai deja 10 useri Looney Tunes în baza de date:**

### ADMINS (3):
| UUID | Username | Email | Full Name |
|------|----------|-------|-----------|
| `11111111-1111-1111-1111-111111111111` | bugs.bunny | bugs.bunny@looneyTunes.com | Bugs Bunny |
| `33333333-3333-3333-3333-333333333333` | porky.pig | porky.pig@looneyTunes.com | Porky Pig |
| `77777777-7777-7777-7777-777777777777` | yosemite.sam | yosemite.sam@looneyTunes.com | Yosemite Sam |

### CLIENTS (7):
| UUID | Username | Email | Full Name |
|------|----------|-------|-----------|
| `22222222-2222-2222-2222-222222222222` | daffy.duck | daffy.duck@looneyTunes.com | Daffy Duck |
| `44444444-4444-4444-4444-444444444444` | tweety.bird | tweety.bird@looneyTunes.com | Tweety Bird |
| `55555555-5555-5555-5555-555555555555` | sylvester.cat | sylvester.cat@looneyTunes.com | Sylvester Cat |
| `66666666-6666-6666-6666-666666666666` | road.runner | road.runner@looneyTunes.com | Road Runner |
| `88888888-8888-8888-8888-888888888888` | wile.e.coyote | wile.e.coyote@looneyTunes.com | Wile E. Coyote |
| `99999999-9999-9999-9999-999999999999` | marvin.martian | marvin.martian@looneyTunes.com | Marvin the Martian |
| `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` | tasmanian.devil | tasmanian.devil@looneyTunes.com | Tasmanian Devil |

**⚠️ Parolele lor sunt hash-uite cu BCrypt**, deci nu le poți vedea în clar în baza de date.

---

## PASUL 5: Testare în Postman

### 📊 STRUCTURA ENDPOINT-URILOR

| Method | Endpoint | Descriere | Securitate |
|--------|----------|-----------|------------|
| GET | `/api/users` | Lista tuturor utilizatorilor | PUBLIC (temporar) |
| GET | `/api/users/{id}` | Detalii user după UUID | PUBLIC (temporar) |
| GET | `/api/users/username/{username}` | Detalii user după username | PUBLIC (temporar) |
| POST | `/api/users` | Creează user nou | PUBLIC (temporar) |
| PUT | `/api/users/{id}` | Actualizează user | PUBLIC (temporar) |
| DELETE | `/api/users/{id}` | Șterge user | PUBLIC (temporar) |

---

### 📋 TEST 1: LISTĂ TOȚI UTILIZATORII EXISTENȚI

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8081/api/users`
- **Headers:** (nu sunt necesare)

**Response așteptat (200 OK) - vei vedea cei 10 useri existenți:**
```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "username": "bugs.bunny",
    "role": "ADMIN",
    "fullName": "Bugs Bunny",
    "email": "bugs.bunny@looneyTunes.com"
  },
  {
    "id": "22222222-2222-2222-2222-222222222222",
    "username": "daffy.duck",
    "role": "CLIENT",
    "fullName": "Daffy Duck",
    "email": "daffy.duck@looneyTunes.com"
  },
  // ... și restul de 8 useri
]
```

**⚠️ IMPORTANT:** Copiază câteva UUID-uri din response! Le vei folosi pentru testele următoare.

---

### ✅ TEST 2: CREEAZĂ UN USER NOU

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8081/api/users`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "username": "test.admin",
  "password": "admin123456",
  "role": "ADMIN",
  "fullName": "Test Administrator",
  "email": "test.admin@energy.ro"
}
```

**Response așteptat (201 CREATED):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "username": "test.admin",
  "role": "ADMIN",
  "fullName": "Test Administrator",
  "email": "test.admin@energy.ro"
}
```

**⚠️ IMPORTANT:** Copiază `id`-ul din response! Îl vei folosi pentru update și delete.

---

### ✅ TEST 3: CREEAZĂ UN CLIENT NOU

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8081/api/users`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "username": "test.client",
  "password": "client123456",
  "role": "CLIENT",
  "fullName": "Test Client User",
  "email": "test.client@energy.ro"
}
```

**Response așteptat (201 CREATED):**
```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "username": "test.client",
  "role": "CLIENT",
  "fullName": "Test Client User",
  "email": "test.client@energy.ro"
}
```

---

### 🔍 TEST 4: CAUTĂ USER DUPĂ ID (cu user existent)

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8081/api/users/{id}`
  - Înlocuiește `{id}` cu UUID-ul unui user existent
- **Exemplu cu Bugs Bunny:** `http://localhost:8081/api/users/11111111-1111-1111-1111-111111111111`

**Response așteptat (200 OK):**
```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "username": "bugs.bunny",
  "role": "ADMIN",
  "fullName": "Bugs Bunny",
  "email": "bugs.bunny@looneyTunes.com"
}
```

**Alte UUID-uri pe care le poți testa:**
- Daffy Duck: `22222222-2222-2222-2222-222222222222`
- Porky Pig: `33333333-3333-3333-3333-333333333333`
- Tweety Bird: `44444444-4444-4444-4444-444444444444`

---

### 🔍 TEST 5: CAUTĂ USER DUPĂ USERNAME (cu user existent)

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8081/api/users/username/{username}`
  - Înlocuiește `{username}` cu numele utilizatorului
- **Exemplu:** `http://localhost:8081/api/users/username/daffy.duck`

**Response așteptat (200 OK):**
```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "username": "daffy.duck",
  "role": "CLIENT",
  "fullName": "Daffy Duck",
  "email": "daffy.duck@looneyTunes.com"
}
```

**Alte username-uri pe care le poți testa:**
- `bugs.bunny` (ADMIN)
- `porky.pig` (CLIENT)
- `tweety.bird` (CLIENT)
- `sylvester.cat` (CLIENT)
- `road.runner` (CLIENT)
- `yosemite.sam` (ADMIN)
- `wile.e.coyote` (CLIENT)
- `marvin.martian` (CLIENT)
- `tasmanian.devil` (CLIENT)

---

### ✏️ TEST 6: ACTUALIZEAZĂ USER (UPDATE COMPLET)

**Request:**
- **Method:** `PUT`
- **URL:** `http://localhost:8081/api/users/{id}`
  - Înlocuiește `{id}` cu UUID-ul userului pe care vrei să-l modifici
- **Exemplu cu Tweety Bird:** `http://localhost:8081/api/users/44444444-4444-4444-4444-444444444444`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "username": "tweety.bird.updated",
  "password": "newpassword123",
  "role": "CLIENT",
  "fullName": "Tweety Bird - VIP Client",
  "email": "tweety.vip@looneyTunes.com"
}
```

**Response așteptat (200 OK):**
```json
{
  "id": "44444444-4444-4444-4444-444444444444",
  "username": "tweety.bird.updated",
  "role": "CLIENT",
  "fullName": "Tweety Bird - VIP Client",
  "email": "tweety.vip@looneyTunes.com"
}
```

---

### ✏️ TEST 7: ACTUALIZEAZĂ PARȚIAL (doar câmpuri specifice)

**Request:**
- **Method:** `PUT`
- **URL:** `http://localhost:8081/api/users/{id}`
- **Exemplu cu Sylvester Cat:** `http://localhost:8081/api/users/55555555-5555-5555-5555-555555555555`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON) - doar câmpurile pe care vrei să le modifici:**
```json
{
  "fullName": "Sylvester Cat - Premium Member"
}
```

**Response așteptat (200 OK):**
```json
{
  "id": "55555555-5555-5555-5555-555555555555",
  "username": "sylvester.cat",
  "role": "CLIENT",
  "fullName": "Sylvester Cat - Premium Member",
  "email": "sylvester.cat@looneyTunes.com"
}
```

**Observație:** Doar `fullName` s-a modificat, restul câmpurilor rămân neschimbate.

---

### 🗑️ TEST 8: ȘTERGE USER

**Request:**
- **Method:** `DELETE`
- **URL:** `http://localhost:8081/api/users/{id}`
- **Exemplu cu Tasmanian Devil:** `http://localhost:8081/api/users/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa`

**Response așteptat (204 NO CONTENT):**
- Nu returnează body, doar status code 204

**Verificare:** După ștergere, dacă încerci să cauți user-ul șters, vei primi eroare 404:
```bash
GET http://localhost:8081/api/users/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa
# Response: 404 Not Found
```

---

### ❌ TEST 9: VALIDĂRI - Username duplicat

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8081/api/users`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON) - încearcă să creezi user cu username existent:**
```json
{
  "username": "bugs.bunny",
  "password": "test123456",
  "role": "CLIENT",
  "fullName": "Test User",
  "email": "test.unique@energy.ro"
}
```

**Response așteptat (400 BAD REQUEST):**
```json
{
  "timestamp": "2025-10-22T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username already exists: bugs.bunny",
  "path": "/api/users"
}
```

---

### ❌ TEST 10: VALIDĂRI - Email duplicat

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8081/api/users`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON) - încearcă să creezi user cu email existent:**
```json
{
  "username": "unique.username",
  "password": "test123456",
  "role": "CLIENT",
  "fullName": "Test User",
  "email": "daffy.duck@looneyTunes.com"
}
```

**Response așteptat (400 BAD REQUEST):**
```json
{
  "timestamp": "2025-10-22T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists: daffy.duck@looneyTunes.com",
  "path": "/api/users"
}
```

---

### ❌ TEST 11: VALIDĂRI - Parolă prea scurtă

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8081/api/users`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "username": "testuser",
  "password": "123",
  "role": "CLIENT",
  "fullName": "Test User",
  "email": "test@energy.ro"
}
```

**Response așteptat (400 BAD REQUEST):**
```json
{
  "timestamp": "2025-10-22T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Password must be at least 8 characters",
  "path": "/api/users"
}
```

---

### ❌ TEST 12: VALIDĂRI - Rol invalid

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8081/api/users`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "username": "testuser",
  "password": "test123456",
  "role": "SUPERADMIN",
  "fullName": "Test User",
  "email": "test@energy.ro"
}
```

**Response așteptat (400 BAD REQUEST):**
```json
{
  "timestamp": "2025-10-22T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Role must be either ADMIN or CLIENT",
  "path": "/api/users"
}
```

---

### ❌ TEST 13: USER INEXISTENT

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8081/api/users/00000000-0000-0000-0000-000000000000`

**Response așteptat (404 NOT FOUND):**
```json
{
  "timestamp": "2025-10-22T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 00000000-0000-0000-0000-000000000000",
  "path": "/api/users/00000000-0000-0000-0000-000000000000"
}
```

---

### ❌ TEST 14: USERNAME INEXISTENT

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8081/api/users/username/nonexistent.user`

**Response așteptat (404 NOT FOUND):**
```json
{
  "timestamp": "2025-10-22T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with username: nonexistent.user",
  "path": "/api/users/username/nonexistent.user"
}
```

---

## 📊 VERIFICARE FINALĂ în MySQL

```sql
USE energy_users_db;

-- Vezi toți userii existenți (cei 10 Looney Tunes + cei noi creați)
SELECT id, username, email, role, full_name, created_at, updated_at 
FROM users 
ORDER BY created_at;

-- Numără userii pe roluri (ar trebui 3 ADMIN + 7 CLIENT = 10 inițial)
SELECT role, COUNT(*) as total 
FROM users 
GROUP BY role;

-- Verifică hash-urile parolelor (trebuie să înceapă cu $2a$10$)
SELECT username, LEFT(password, 20) as password_hash_preview 
FROM users;

-- Caută un user specific după username
SELECT * FROM users WHERE username = 'bugs.bunny';

-- Caută un user specific după UUID
SELECT * FROM users WHERE id = '11111111-1111-1111-1111-111111111111';

-- Vezi doar ADMIN-ii
SELECT id, username, email, full_name 
FROM users 
WHERE role = 'ADMIN';

-- Vezi doar CLIENT-ii
SELECT id, username, email, full_name 
FROM users 
WHERE role = 'CLIENT';

-- Verifică userii creați recent (ultimele 24 ore)
SELECT id, username, email, role, created_at 
FROM users 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY created_at DESC;

-- Verifică userii modificați recent
SELECT id, username, email, role, updated_at 
FROM users 
WHERE updated_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY updated_at DESC;
```

---

## 🐛 TROUBLESHOOTING

### Problema 1: "Access denied for user 'root'@'localhost'"
**Soluție:** Verifică parola în `application.properties` (acum e `root`)

### Problema 2: "Unknown database 'energy_users_db'"
**Soluție:** Rulează scriptul SQL din PASUL 2 pentru a crea baza de date

### Problema 3: "Port 8081 already in use"
**Soluție:**
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID_NUMBER> /F
```

### Problema 4: "Table 'users' doesn't exist"
**Soluție:** Rulează scriptul SQL din PASUL 2 pentru a crea tabelul

### Problema 5: UUID prea lung pentru coloana id
**Soluție:** Rulează scriptul `fix_users_table.sql`:
```bash
mysql -u root -p < fix_users_table.sql
```

### Problema 6: Aplicația nu pornește
**Soluție:** Verifică că:
- MySQL rulează
- Portul 8081 este liber
- Java 17 este instalat: `java -version`

---

## 📝 NOTIȚE IMPORTANTE

### 1. Securitate (TEMPORAR DEZACTIVATĂ)
- **TOATE endpoint-urile sunt publice** pentru testare
- În producție, vor fi protejate cu JWT:
  - `GET /api/users` - doar ADMIN
  - `GET /api/users/{id}` - ADMIN sau user-ul însuși
  - `POST /api/users` - doar ADMIN
  - `PUT /api/users/{id}` - doar ADMIN
  - `DELETE /api/users/{id}` - doar ADMIN
  - `GET /api/users/username/{username}` - doar ADMIN

### 2. Validări aplicate
- **Username:** 3-50 caractere, unic
- **Password:** minim 8 caractere, hash-uit cu BCrypt
- **Email:** format valid, unic
- **Role:** doar "ADMIN" sau "CLIENT"
- **Full Name:** maxim 100 caractere

### 3. Parolele sunt hash-uite
- Folosește BCrypt cu 10 rounds
- Hash-ul începe cu `$2a$10$`
- Nu poți vedea parola originală în baza de date

### 4. UUID-uri
- Fiecare user are un UUID unic generat automat
- Format: `a1b2c3d4-e5f6-7890-abcd-ef1234567890`
- Folosit pentru identificare în toate operațiunile

### 5. Timestamp-uri
- `created_at` - setat automat la creare
- `updated_at` - actualizat automat la fiecare modificare

---

## ✅ CHECKLIST FINAL

- [ ] MySQL pornit și accesibil
- [ ] Baza de date `energy_users_db` există și conține cei 10 useri Looney Tunes
- [ ] Tabelul `users` are structura corectă (VARCHAR(36) pentru UUID)
- [ ] Aplicația Spring Boot pornită pe port 8081
- [ ] **TEST 1:** Lista celor 10 useri existenți funcționează
- [ ] **TEST 2:** Creat user ADMIN nou cu succes
- [ ] **TEST 3:** Creat user CLIENT nou cu succes
- [ ] **TEST 4:** Căutare după ID funcționează (testat cu Bugs Bunny)
- [ ] **TEST 5:** Căutare după username funcționează (testat cu Daffy Duck)
- [ ] **TEST 6:** Update complet funcționează (testat cu Tweety Bird)
- [ ] **TEST 7:** Update parțial funcționează (testat cu Sylvester Cat)
- [ ] **TEST 8:** Delete user funcționează (testat cu Tasmanian Devil)
- [ ] **TEST 9-12:** Validările funcționează (username/email duplicat, parolă scurtă, rol invalid)
- [ ] **TEST 13-14:** Erorile 404 funcționează (user inexistent, username inexistent)
- [ ] Verificat în MySQL că datele sunt salvate/modificate/șterse corect

---

## 🎯 SCENARII DE TESTARE COMPLETE

### Scenariu 1: Explorare useri existenți
1. **TEST 1:** Listează toți cei 10 useri existenți
2. **TEST 4:** Caută Bugs Bunny după ID (`11111111-1111-1111-1111-111111111111`)
3. **TEST 5:** Caută Daffy Duck după username (`daffy.duck`)
4. Verifică în MySQL că datele corespund

### Scenariu 2: Flux complet CRUD cu useri noi
1. **TEST 2:** Creează un ADMIN nou (`test.admin`)
2. **TEST 3:** Creează un CLIENT nou (`test.client`)
3. **TEST 1:** Listează toți userii (acum ar trebui să fie 12)
4. **TEST 6:** Actualizează Tweety Bird (schimbă fullName și email)
5. **TEST 7:** Actualizează parțial Sylvester Cat (doar fullName)
6. **TEST 8:** Șterge Tasmanian Devil
7. **TEST 1:** Verifică că Tasmanian Devil nu mai apare în listă

### Scenariu 3: Testare validări și erori
1. **TEST 9:** Încearcă să creezi user cu username `bugs.bunny` (duplicat) → 400
2. **TEST 10:** Încearcă să creezi user cu email `daffy.duck@looneyTunes.com` (duplicat) → 400
3. **TEST 11:** Încearcă să creezi user cu parolă `123` (prea scurtă) → 400
4. **TEST 12:** Încearcă să creezi user cu rol `SUPERADMIN` (invalid) → 400
5. **TEST 13:** Caută user cu UUID inexistent → 404
6. **TEST 14:** Caută user cu username inexistent → 404

### Scenariu 4: Update și verificare
1. Alege un CLIENT existent (ex: Road Runner)
2. Actualizează-i email-ul și full name
3. Caută user-ul după ID și verifică modificările
4. Actualizează doar password-ul
5. Verifică în MySQL că password hash-ul s-a schimbat
6. Caută user-ul după username și verifică că datele sunt corecte

---

## 🎬 BONUS: Quick Test URLs pentru userii Looney Tunes

### Căutare după ID (GET):
```
http://localhost:8081/api/users/11111111-1111-1111-1111-111111111111  # Bugs Bunny (ADMIN)
http://localhost:8081/api/users/22222222-2222-2222-2222-222222222222  # Daffy Duck (CLIENT)
http://localhost:8081/api/users/33333333-3333-3333-3333-333333333333  # Porky Pig (CLIENT)
http://localhost:8081/api/users/44444444-4444-4444-4444-444444444444  # Tweety Bird (CLIENT)
http://localhost:8081/api/users/55555555-5555-5555-5555-555555555555  # Sylvester Cat (CLIENT)
http://localhost:8081/api/users/66666666-6666-6666-6666-666666666666  # Road Runner (CLIENT)
http://localhost:8081/api/users/77777777-7777-7777-7777-777777777777  # Yosemite Sam (ADMIN)
http://localhost:8081/api/users/88888888-8888-8888-8888-888888888888  # Wile E. Coyote (CLIENT)
http://localhost:8081/api/users/99999999-9999-9999-9999-999999999999  # Marvin Martian (CLIENT)
http://localhost:8081/api/users/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa  # Tasmanian Devil (CLIENT)
```

### Căutare după Username (GET):
```
http://localhost:8081/api/users/username/bugs.bunny
http://localhost:8081/api/users/username/daffy.duck
http://localhost:8081/api/users/username/porky.pig
http://localhost:8081/api/users/username/tweety.bird
http://localhost:8081/api/users/username/sylvester.cat
http://localhost:8081/api/users/username/road.runner
http://localhost:8081/api/users/username/yosemite.sam
http://localhost:8081/api/users/username/wile.e.coyote
http://localhost:8081/api/users/username/marvin.martian
http://localhost:8081/api/users/username/tasmanian.devil
```

### Exemplu Update Rapid (PUT):
**URL:** `http://localhost:8081/api/users/66666666-6666-6666-6666-666666666666`
**Body:**
```json
{
  "fullName": "Road Runner - Speed Demon"
}
```

---

## 📦 IMPORT POSTMAN COLLECTION (OPȚIONAL)

Poți crea o colecție Postman cu toate request-urile de mai sus:

1. Deschide Postman
2. Click pe "Import" → "Raw text"
3. Copiază și lipește structura de mai jos
4. Click "Import"

**Sau creează manual:**
- Folder: "User Microservice Tests"
  - GET All Users
  - GET User by ID (Bugs Bunny)
  - GET User by Username (Daffy Duck)
  - POST Create Admin
  - POST Create Client
  - PUT Update User
  - DELETE Delete User
  - POST Validate Username Duplicate
  - POST Validate Email Duplicate
  - POST Validate Short Password
  - POST Validate Invalid Role
  - GET User Not Found

---

**Succes! 🎉**

**Microserviciul Users este gata de testare cu cei 10 useri Looney Tunes existenți!**
