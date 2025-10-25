# 📊 Sincronizare Date între Microservicii

## 🎯 Problema identificată de profesor

În arhitectura de microservicii, fiecare serviciu trebuie să fie **independent** și să aibă propriile date. 

**Problema:** Device Microservice păstra doar `user_id` (String), dar nu știa nimic despre user când acesta se modifica în User Microservice.

## ✅ Soluția implementată

### 1. **Tabela `users` locală în Device Microservice**

Am creat o **copie locală** a tabelei users cu doar informațiile necesare:
- `id` - UUID-ul user-ului
- `username` - numele user-ului
- `email` - email-ul user-ului  
- `role` - rolul user-ului (ADMIN/CLIENT)

**NU** stocăm:
- `password_hash` - nu este necesar
- alte date sensibile

### 2. **Relație Many-to-One între Device și User**

```java
@Entity
public class Device {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // Relație cu User local
}
```

**NU** este nevoie de tabel `user_device` separat pentru că:
- Un device aparține unui singur user (Many-to-One)
- Hibernate gestionează automat relația prin `user_id` foreign key

### 3. **Sincronizare automată**

Când se creează/actualizează un device cu un user:

```java
private User syncUserFromUserMicroservice(String userId) {
    // 1. Apelează User Microservice pentru a obține datele user-ului
    var userResponse = restTemplate.getForObject(
        "http://localhost:8081/api/users/" + userId, 
        Map.class
    );
    
    // 2. Salvează/actualizează user-ul în baza locală
    User localUser = userRepository.findById(userId).orElse(new User());
    localUser.setId(userId);
    localUser.setUsername(userResponse.get("username"));
    localUser.setEmail(userResponse.get("email"));
    localUser.setRole(userResponse.get("role"));
    
    return userRepository.save(localUser);
}
```

## 📋 Structura bazei de date

### Device Microservice DB (`energy_devices_db`)

```
┌─────────────────────┐
│      users          │  ← Copie locală
├─────────────────────┤
│ id (PK)             │
│ username            │
│ email               │
│ role                │
│ created_at          │
│ updated_at          │
└─────────────────────┘
          ▲
          │ user_id (FK)
          │
┌─────────────────────┐
│     devices         │
├─────────────────────┤
│ id (PK)             │
│ description         │
│ address             │
│ max_hourly_consumption │
│ user_id (FK)        │  ← Foreign Key către users
│ created_at          │
│ updated_at          │
└─────────────────────┘
```

## 🔄 Fluxul de sincronizare

### Scenariu 1: Creare device nou

```
1. ADMIN creează device cu userId="123"
2. Device Service apelează User Microservice: GET /api/users/123
3. User Microservice returnează: {id, username, email, role}
4. Device Service salvează user-ul în tabela locală `users`
5. Device Service creează device-ul cu relație către user local
```

### Scenariu 2: Assign device la alt user

```
1. ADMIN face assign device la userId="456"
2. Device Service apelează User Microservice: GET /api/users/456
3. User Microservice returnează datele user-ului
4. Device Service actualizează/creează user-ul în tabela locală
5. Device Service actualizează device.user_id = "456"
```

### Scenariu 3: User se modifică în User Microservice

**Problema:** Dacă username-ul se schimbă în User Microservice, Device Microservice nu știe automat.

**Soluții posibile:**

#### Opțiunea A: Sincronizare la cerere (implementată acum)
- La fiecare operație (create/update/assign), se sincronizează user-ul
- Simplu, dar datele pot fi ușor outdated

#### Opțiunea B: Event-Driven (pentru viitor - mai avansat)
- User Microservice publică un event când se modifică un user
- Device Microservice ascultă event-ul și actualizează copia locală
- Necesită RabbitMQ/Kafka

#### Opțiunea C: Endpoint de sincronizare
- Creezi un endpoint `/api/users/sync/{userId}` în Device Microservice
- User Microservice îl apelează când se modifică un user
- Webhook pattern

## 📊 Avantaje vs Dezavantaje

### ✅ Avantaje

1. **Independență** - Device Microservice nu depinde de User Microservice pentru citiri
2. **Performance** - Nu mai face call la User Microservice pentru fiecare device
3. **Relații clare** - Hibernate gestionează relația Many-to-One
4. **Queries eficiente** - Poți face JOIN-uri în SQL

### ⚠️ Dezavantaje

1. **Duplicare date** - Users sunt stocați în 2 locuri
2. **Sincronizare** - Trebuie să menții datele sincronizate
3. **Complexitate** - Mai mult cod de mentenanță

## 🎓 Pentru prezentarea la profesor

### Explică astfel:

1. **Problema:**
   - "Device Microservice păstra doar user_id ca String"
   - "Nu știa nimic despre user când acesta se modifica"

2. **Soluția:**
   - "Am creat o copie locală a tabelei users în Device Microservice"
   - "Relație Many-to-One între Device și User local"
   - "Sincronizare automată prin REST call la User Microservice"

3. **Beneficii:**
   - "Device Microservice este independent"
   - "Queries mai rapide (nu mai face REST calls)"
   - "Relații clare în baza de date"

4. **Trade-offs:**
   - "Duplicare controlată de date"
   - "Eventual consistency (datele se sincronizează la cerere)"

## 🔍 Întrebări posibile de la profesor

**Q: De ce nu folosești un tabel user_device separat?**
A: Nu este necesar pentru că relația este Many-to-One (un device aparține unui singur user). Hibernate gestionează automat prin foreign key `user_id`.

**Q: Ce se întâmplă dacă user-ul se șterge din User Microservice?**
A: Foreign key este configurat cu `ON DELETE SET NULL`, deci device-ul rămâne dar `user_id` devine NULL. Alternativ, poți configura `ON DELETE CASCADE` pentru a șterge și device-urile.

**Q: Cum sincronizezi datele când user-ul se modifică?**
A: Acum sincronizarea se face la cerere (când se creează/actualizează device). Pentru sincronizare în timp real, ar trebui implementat un sistem event-driven cu RabbitMQ/Kafka.

**Q: Nu este duplicare de date?**
A: Da, dar este o duplicare controlată și necesară în microservicii pentru independență. Stocăm doar datele esențiale (username, email, role), nu date sensibile (password).

---

**Implementarea este completă și funcțională! 🎉**
