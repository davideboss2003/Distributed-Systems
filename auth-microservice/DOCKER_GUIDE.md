# 🐳 GHID DOCKER - Auth Microservice

## 📦 Fișiere Docker Create

1. **Dockerfile** - Creează imaginea aplicației
2. **docker-compose.yml** - Orchestrează containerele (MySQL + Auth Service)
3. **.dockerignore** - Exclude fișiere inutile din build

---

## 🚀 COMENZI RAPIDE

### Pornește tot (MySQL + Auth Service):
```bash
docker-compose up -d
```

### Oprește tot:
```bash
docker-compose down
```

### Vezi logurile:
```bash
docker-compose logs -f auth-service
```

### Rebuild după modificări:
```bash
docker-compose up -d --build
```

---

## 📋 PAȘI DETALIAT

### 1. Verifică că Docker rulează:
```bash
docker --version
docker-compose --version
```

### 2. Build și pornește containerele:
```bash
# Din directorul proiectului (unde e docker-compose.yml)
docker-compose up -d
```

**Ce face:**
- Descarcă imaginea MySQL 8.0
- Build-uiește aplicația Spring Boot
- Creează containerele
- Pornește MySQL
- Așteaptă ca MySQL să fie healthy
- Pornește Auth Service
- Conectează serviciile în rețea

### 3. Verifică că rulează:
```bash
# Vezi toate containerele
docker ps

# Trebuie să vezi:
# - energy-auth-mysql (port 3306)
# - energy-auth-service (port 8083)
```

### 4. Testează aplicația:
```bash
# Health check
curl http://localhost:8083/actuator/health

# Login
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@energy.ro\",\"password\":\"admin123\"}"
```

---

## 🔧 COMENZI UTILE

### Vezi logurile:
```bash
# Toate serviciile
docker-compose logs -f

# Doar auth-service
docker-compose logs -f auth-service

# Doar MySQL
docker-compose logs -f mysql
```

### Restart servicii:
```bash
# Restart toate
docker-compose restart

# Restart doar auth-service
docker-compose restart auth-service
```

### Oprește și șterge tot (inclusiv volume):
```bash
docker-compose down -v
```

### Intră în container:
```bash
# În auth-service
docker exec -it energy-auth-service sh

# În MySQL
docker exec -it energy-auth-mysql mysql -u root -proot
```

### Rebuild după modificări cod:
```bash
docker-compose up -d --build
```

---

## 🗄️ CREARE ADMIN

### Metoda 1: Din MySQL container
```bash
# Intră în MySQL
docker exec -it energy-auth-mysql mysql -u root -proot

# Rulează comenzile SQL
USE energy_auth_db;

INSERT INTO auth_users (id, username, email, password_hash, role, created_at, updated_at)
VALUES (
    UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440000', '-', '')),
    'admin',
    'admin@energy.ro',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    NOW(),
    NOW()
);

SELECT * FROM auth_users;
EXIT;
```

### Metoda 2: Cu script SQL
```bash
# Copiază scriptul în container
docker cp create_first_admin.sql energy-auth-mysql:/tmp/

# Execută scriptul
docker exec -i energy-auth-mysql mysql -u root -proot < create_first_admin.sql
```

---

## 🐛 TROUBLESHOOTING

### Problema: "port is already allocated"
```bash
# Verifică ce folosește portul
netstat -ano | findstr :8083
netstat -ano | findstr :3306

# Oprește procesul sau schimbă portul în docker-compose.yml
```

### Problema: "Cannot connect to MySQL"
```bash
# Verifică că MySQL este healthy
docker ps

# Vezi logurile MySQL
docker-compose logs mysql

# Așteaptă ~30 secunde pentru inițializare
```

### Problema: Auth service nu pornește
```bash
# Vezi logurile
docker-compose logs auth-service

# Verifică că MySQL este pornit
docker ps | grep mysql

# Restart
docker-compose restart auth-service
```

### Problema: Modificări în cod nu apar
```bash
# Rebuild imaginea
docker-compose up -d --build

# Sau șterge imaginea și rebuild
docker-compose down
docker rmi energy-auth-service
docker-compose up -d --build
```

---

## 📊 ARHITECTURA

```
┌─────────────────────────────────────────┐
│         Docker Network (bridge)          │
│                                          │
│  ┌──────────────┐    ┌───────────────┐  │
│  │    MySQL     │    │ Auth Service  │  │
│  │  Container   │◄───┤   Container   │  │
│  │              │    │               │  │
│  │  Port: 3306  │    │  Port: 8083   │  │
│  └──────────────┘    └───────────────┘  │
│         │                    │           │
└─────────┼────────────────────┼───────────┘
          │                    │
          │                    │
     Volume: mysql-data   Exposed: 8083
```

---

## 🎯 PENTRU PROIECTUL DE FACULTATE

### Ce trebuie să predai:
1. ✅ **Dockerfile** - pentru build aplicație
2. ✅ **docker-compose.yml** - pentru orchestrare
3. ✅ **.dockerignore** - pentru optimizare

### Cum să demonstrezi:
```bash
# 1. Pornește tot
docker-compose up -d

# 2. Verifică că rulează
docker ps

# 3. Creează admin (vezi Metoda 1 mai sus)

# 4. Testează în Postman
# - Login: POST http://localhost:8083/api/auth/login
# - Register: POST http://localhost:8083/api/auth/register
# - Validate: GET http://localhost:8083/api/auth/validate

# 5. Arată logurile
docker-compose logs auth-service

# 6. Oprește tot
docker-compose down
```

---

## 📝 EXPLICAȚII FIȘIERE

### Dockerfile:
- **Multi-stage build** - optimizează dimensiunea imaginii
- **Stage 1 (build):** Compilează aplicația cu Maven
- **Stage 2 (runtime):** Rulează doar JAR-ul (fără Maven)
- **Healthcheck:** Verifică că aplicația răspunde

### docker-compose.yml:
- **mysql service:** Baza de date
- **auth-service:** Aplicația Spring Boot
- **depends_on:** Auth așteaptă ca MySQL să fie healthy
- **networks:** Serviciile comunică între ele
- **volumes:** Datele MySQL persistă

### .dockerignore:
- Exclude fișiere inutile din build
- Reduce dimensiunea imaginii
- Accelerează build-ul

---

## ✅ CHECKLIST FINAL

Înainte de predare, verifică:
- [ ] `docker-compose up -d` pornește ambele containere
- [ ] `docker ps` arată 2 containere running
- [ ] Aplicația răspunde pe http://localhost:8083
- [ ] Poți face login cu admin
- [ ] Poți crea useri noi
- [ ] Logurile nu arată erori
- [ ] `docker-compose down` oprește tot corect

---

**Gata! Ai tot ce îți trebuie pentru Docker deployment! 🎉**
