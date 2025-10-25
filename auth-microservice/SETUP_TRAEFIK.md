# 🚀 SETUP COMPLET - Traefik Reverse Proxy

## 📁 STRUCTURA FINALĂ

```
C:\SD\
├── auth-microservice/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
├── user-microservice/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
├── device-microservice/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
└── docker-compose.yml  👈 AICI (în C:\SD\)
```

---

## 🔧 PAȘI DE SETUP

### 1. Copiază docker-compose-traefik.yml

```bash
# Din directorul auth-microservice
cd C:\SD

# Copiază fișierul
copy auth-microservice\docker-compose-traefik.yml docker-compose.yml
```

SAU creează manual `C:\SD\docker-compose.yml` cu conținutul din `docker-compose-traefik.yml`

---

### 2. Oprește containerele vechi

```bash
# Oprește fiecare microserviciu individual
cd C:\SD\auth-microservice
docker-compose down

cd C:\SD\user-microservice
docker-compose down

cd C:\SD\device-microservice
docker-compose down
```

---

### 3. Pornește tot cu Traefik

```bash
# Din directorul părinte
cd C:\SD

# Pornește toate serviciile
docker-compose up -d

# Verifică că toate rulează
docker ps
```

**Ar trebui să vezi 10 containere:**
1. traefik
2. energy-auth-service
3. energy-auth-mysql
4. user-microservice-app
5. mysql-users-db
6. device-microservice
7. mysql-devices-db

---

### 4. Verifică Traefik Dashboard

Deschide browser:
```
http://localhost:8080
```

Ar trebui să vezi:
- Dashboard Traefik
- 3 routers (auth, users, devices)
- 3 services (auth, users, devices)
- Toate healthy ✅

---

### 5. Testează în Postman

#### A. Login (prin Traefik):
```
POST http://localhost/api/auth/login

Body:
{
    "email": "admin@energy.ro",
    "password": "admin"
}
```

#### B. Get Users (prin Traefik):
```
GET http://localhost/api/users

Headers:
Authorization: Bearer <TOKEN_DE_LA_LOGIN>
```

#### C. Get Devices (prin Traefik):
```
GET http://localhost/api/devices

Headers:
Authorization: Bearer <TOKEN_DE_LA_LOGIN>
```

---

## 📊 COMPARAȚIE

### ÎNAINTE (fără Traefik):
```bash
# Auth
POST http://localhost:8083/api/auth/login

# Users
GET http://localhost:8081/api/users

# Devices
GET http://localhost:8082/api/devices
```

### DUPĂ (cu Traefik):
```bash
# Auth
POST http://localhost/api/auth/login

# Users
GET http://localhost/api/users

# Devices
GET http://localhost/api/devices
```

**Toate prin PORT 80!** 🎉

---

## 🔍 VERIFICĂRI

### 1. Verifică că Traefik rulează:
```bash
docker logs traefik
```

Ar trebui să vezi:
```
Configuration loaded from flags.
Traefik version 2.10.x
```

### 2. Verifică routing-ul:
```bash
curl http://localhost/api/auth/validate?token=test
```

Ar trebui să primești:
```
false
```

### 3. Verifică health checks:
```bash
# Auth
curl http://localhost/api/auth/actuator/health

# Users
curl http://localhost/api/users/actuator/health

# Devices
curl http://localhost/api/devices/actuator/health
```

---

## 🐛 TROUBLESHOOTING

### Problema: "Cannot connect to Docker daemon"
**Soluție:**
```bash
# Pornește Docker Desktop
# Așteaptă să pornească complet
# Încearcă din nou
```

### Problema: "404 Not Found" la request-uri
**Cauză:** Routing-ul nu funcționează
**Soluție:**
```bash
# Verifică labels în docker-compose.yml
# Restart Traefik
docker restart traefik
```

### Problema: "502 Bad Gateway"
**Cauză:** Serviciul nu răspunde
**Soluție:**
```bash
# Verifică că serviciul rulează
docker ps | findstr auth

# Vezi logurile
docker logs energy-auth-service
```

### Problema: Dashboard-ul nu se încarcă
**Cauză:** Portul 8080 este ocupat
**Soluție:**
```bash
# Verifică ce folosește portul
netstat -ano | findstr :8080

# Oprește procesul sau schimbă portul în docker-compose.yml
```

---

## 📝 COMENZI UTILE

### Pornește tot:
```bash
cd C:\SD
docker-compose up -d
```

### Oprește tot:
```bash
cd C:\SD
docker-compose down
```

### Rebuild un serviciu:
```bash
cd C:\SD
docker-compose up -d --build auth-service
```

### Vezi logurile:
```bash
# Toate serviciile
docker-compose logs -f

# Doar Traefik
docker-compose logs -f traefik

# Doar Auth
docker-compose logs -f auth-service
```

### Restart un serviciu:
```bash
docker-compose restart auth-service
```

---

## ✅ CHECKLIST FINAL

Înainte de predare:
- [ ] Toate cele 3 microservicii au Dockerfile în folderele lor
- [ ] docker-compose.yml este în C:\SD\ (folderul părinte)
- [ ] Traefik pornește și rulează
- [ ] Dashboard Traefik se încarcă (localhost:8080)
- [ ] Login funcționează prin Traefik (localhost/api/auth/login)
- [ ] Users funcționează prin Traefik (localhost/api/users)
- [ ] Devices funcționează prin Traefik (localhost/api/devices)
- [ ] Toate serviciile sunt healthy în Dashboard
- [ ] Poți face request-uri din Postman prin port 80

---

## 🎉 REZULTAT FINAL

Ai un sistem complet production-ready:
- ✅ Reverse Proxy (Traefik)
- ✅ 3 Microservicii independente
- ✅ 3 Baze de date MySQL separate
- ✅ Service Discovery automat
- ✅ Health Checks
- ✅ Dashboard pentru monitoring
- ✅ Un singur entry point (port 80)

**Gata pentru proiectul de facultate!** 🚀
