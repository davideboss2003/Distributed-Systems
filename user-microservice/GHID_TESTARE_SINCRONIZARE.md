# 🔄 Ghid Testare Sincronizare User ↔ Device Microservice

## ✅ Ce am implementat

### În User Microservice (acest proiect):
- **CREATE**: Când creezi un user → se trimite automat la Device Microservice
- **UPDATE**: Când modifici username-ul → se actualizează automat în Device Microservice
- **DELETE**: Când ștergi un user → se șterge automat din Device Microservice (inclusiv toate device-urile lui)

### În Device Microservice:
- Primește sincronizările și actualizează tabelul `users`
- La DELETE, șterge automat toate device-urile asociate user-ului

---

## 🧪 Testare Completă

### Pregătire:
1. **Pornește ambele microservicii:**
   - User Microservice: `http://localhost:8081`
   - Device Microservice: `http://localhost:8082`

2. **Verifică că ambele baze de date sunt active:**
   - `energy_users_db` (User Microservice)
   - `energy_devices_db` (Device Microservice)

---

## Test 1: CREATE User (Sincronizare Automată)

### 1.1 Creează un user în User Microservice:
```http
POST http://localhost:8081/api/users
Content-Type: application/json

{
  "username": "test_sync_user",
  "email": "sync@test.com",
  "password": "password123",
  "role": "USER",
  "fullName": "Test Sync User"
}
```

**Răspuns așteptat:** `201 Created` + user creat cu ID

### 1.2 Verifică în Device Microservice:
```sql
-- Conectează-te la energy_devices_db
SELECT * FROM users WHERE username = 'test_sync_user';
```

**Rezultat așteptat:** User-ul apare în tabelul `users` din Device Microservice

---

## Test 2: UPDATE Username (Sincronizare Automată)

### 2.1 Modifică username-ul:
```http
PUT http://localhost:8081/api/users/{USER_ID}
Content-Type: application/json

{
  "username": "test_sync_user_MODIFIED",
  "email": "sync@test.com",
  "role": "USER",
  "fullName": "Test Sync User"
}
```

**Răspuns așteptat:** `200 OK` + user actualizat

### 2.2 Verifică în Device Microservice:
```sql
-- Conectează-te la energy_devices_db
SELECT * FROM users WHERE id = '{USER_ID}';
```

**Rezultat așteptat:** Username-ul e actualizat la `test_sync_user_MODIFIED`

---

## Test 3: DELETE User + Device-uri (Sincronizare Automată)

### 3.1 Creează câteva device-uri pentru user:
```http
POST http://localhost:8082/api/devices
Content-Type: application/json

{
  "description": "Device 1 for sync test",
  "address": "Address 1",
  "maxHourlyConsumption": 100.0,
  "userId": "{USER_ID}"
}
```

Repetă pentru 2-3 device-uri.

### 3.2 Verifică device-urile create:
```sql
-- Conectează-te la energy_devices_db
SELECT * FROM devices WHERE user_id = '{USER_ID}';
```

**Rezultat așteptat:** 2-3 device-uri asociate user-ului

### 3.3 Șterge user-ul din User Microservice:
```http
DELETE http://localhost:8081/api/users/{USER_ID}
```

**Răspuns așteptat:** `204 No Content`

### 3.4 Verifică în Device Microservice:
```sql
-- Conectează-te la energy_devices_db

-- User-ul trebuie să fie șters
SELECT * FROM users WHERE id = '{USER_ID}';

-- Toate device-urile trebuie să fie șterse
SELECT * FROM devices WHERE user_id = '{USER_ID}';
```

**Rezultat așteptat:** 
- ❌ User-ul NU mai există
- ❌ Device-urile NU mai există

---

## 📊 Verificare Log-uri

### User Microservice (console):
```
✅ User synced to Device Microservice: {USER_ID} - {USERNAME}
✅ User deleted from Device Microservice: {USER_ID}
```

### Device Microservice (console):
```
✅ User synced: {USER_ID} - {USERNAME}
✅ Deleting all devices for user: {USER_ID}
✅ User deleted: {USER_ID}
```

---

## ⚠️ Troubleshooting

### Problema: Sincronizarea nu funcționează

1. **Verifică că Device Microservice rulează:**
   ```http
   GET http://localhost:8082/actuator/health
   ```

2. **Verifică URL-ul în `application.properties`:**
   ```properties
   device.microservice.url=http://localhost:8082
   ```

3. **Verifică log-urile pentru erori:**
   - Caută `Failed to sync user` sau `Failed to delete user`

### Problema: Device-urile nu se șterg

1. **Verifică că metoda `deleteByUser()` există în DeviceRepository:**
   ```java
   void deleteByUser(User user);
   ```

2. **Verifică că `UserSyncService.deleteUser()` apelează metoda:**
   ```java
   deviceRepository.deleteByUser(user);
   ```

---

## 🎯 Rezultat Final

După toate testele, ar trebui să ai:

✅ **CREATE**: User creat în User MS → apare automat în Device MS  
✅ **UPDATE**: Username modificat în User MS → se actualizează în Device MS  
✅ **DELETE**: User șters din User MS → se șterge din Device MS + toate device-urile lui

**Sincronizarea e completă și automată!** 🚀
