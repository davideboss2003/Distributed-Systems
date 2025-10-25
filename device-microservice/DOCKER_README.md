# 🐳 Docker Deployment - Device Microservice

## 📁 Fișiere Docker

### 1. **Dockerfile**
Fișierul care descrie cum se construiește imaginea Docker pentru aplicația ta.

**Ce face:**
- Folosește Maven pentru a compila aplicația Java
- Creează un container mic și optimizat cu doar aplicația compilată
- Expune portul 8082

### 2. **docker-compose.yml**
Fișierul care orchestrează toate containerele necesare (aplicație + bază de date).

**Ce face:**
- Pornește un container MySQL pentru baza de date
- Pornește containerul cu aplicația Device Microservice
- Conectează cele două containere într-o rețea privată
- Configurează variabilele de mediu

### 3. **.dockerignore**
Similar cu `.gitignore`, dar pentru Docker.

**Ce face:**
- Exclude fișiere inutile din imaginea Docker (IDE settings, logs, etc.)
- Face build-ul mai rapid și imaginea mai mică

---

## 🚀 Cum să rulezi aplicația cu Docker

### Pasul 1: Asigură-te că ai Docker instalat

```bash
# Verifică versiunea Docker
docker --version

# Verifică versiunea Docker Compose
docker-compose --version
```

Dacă nu ai Docker instalat, descarcă-l de aici: https://www.docker.com/products/docker-desktop

---

### Pasul 2: Build și pornește containerele

```bash
# Din directorul proiectului (unde este docker-compose.yml)
docker-compose up --build
```

**Ce se întâmplă:**
1. Se construiește imaginea Docker pentru aplicație
2. Se descarcă imaginea MySQL
3. Se pornesc ambele containere
4. Aplicația va fi disponibilă pe `http://localhost:8082`
5. MySQL va fi disponibil pe portul `3307`

---

### Pasul 3: Verifică că totul funcționează

```bash
# Verifică containerele care rulează
docker ps

# Verifică health-ul aplicației
curl http://localhost:8082/actuator/health
```

---

## 🛠️ Comenzi utile Docker

### Pornire containere (în background)
```bash
docker-compose up -d
```

### Oprire containere
```bash
docker-compose down
```

### Oprire și ștergere volume (șterge și baza de date!)
```bash
docker-compose down -v
```

### Vezi log-urile aplicației
```bash
# Toate log-urile
docker-compose logs

# Doar aplicația
docker-compose logs device-microservice

# Doar MySQL
docker-compose logs mysql-devices

# Follow logs (live)
docker-compose logs -f device-microservice
```

### Rebuild aplicația după modificări
```bash
docker-compose up --build
```

### Intră în containerul aplicației
```bash
docker exec -it device-microservice sh
```

### Intră în MySQL
```bash
docker exec -it mysql-devices-db mysql -u root -proot energy_devices_db
```

---

## 📊 Verificare bază de date în Docker

```bash
# Conectează-te la MySQL din container
docker exec -it mysql-devices-db mysql -u root -proot

# În MySQL prompt
USE energy_devices_db;
SHOW TABLES;
SELECT * FROM devices;
EXIT;
```

---

## 🔧 Troubleshooting

### Problema 1: Port 3307 deja folosit
**Soluție:** Schimbă portul în `docker-compose.yml`:
```yaml
ports:
  - "3308:3306"  # Folosește alt port
```

### Problema 2: Aplicația nu se conectează la MySQL
**Cauză:** MySQL nu este încă gata când aplicația pornește

**Soluție:** Aplicația va reîncerca automat. Sau repornește containerul:
```bash
docker-compose restart device-microservice
```

### Problema 3: Erori la build
**Soluție:** Șterge imaginile vechi și rebuild:
```bash
docker-compose down
docker system prune -a
docker-compose up --build
```

### Problema 4: Baza de date goală
**Cauză:** Datele nu sunt populate automat

**Soluție:** Populează manual:
```bash
docker exec -i mysql-devices-db mysql -u root -proot energy_devices_db < src/main/resources/data.sql
```

---

## 📝 Variabile de mediu importante

În `docker-compose.yml` poți modifica:

```yaml
environment:
  # Conexiune la baza de date
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql-devices:3306/energy_devices_db
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: root
  
  # JWT Secret (TREBUIE să fie identic cu auth-microservice!)
  JWT_SECRET: your-secret-key-min-256-bits-for-hs256-algorithm-energy-management
```

---

## 🎯 Pentru prezentarea proiectului

### Demonstrează deployment-ul Docker:

1. **Arată fișierele Docker:**
   - `Dockerfile` - cum se construiește aplicația
   - `docker-compose.yml` - orchestrarea serviciilor
   - `.dockerignore` - optimizare

2. **Pornește aplicația:**
   ```bash
   docker-compose up --build
   ```

3. **Arată că funcționează:**
   ```bash
   # Health check
   curl http://localhost:8082/actuator/health
   
   # Vezi containerele
   docker ps
   ```

4. **Testează în Postman:**
   - GET all devices
   - GET device by ID
   - POST create device (cu token ADMIN)

5. **Arată log-urile:**
   ```bash
   docker-compose logs device-microservice
   ```

---

## ✅ Checklist pentru proiect

- [x] Dockerfile creat și funcțional
- [x] docker-compose.yml cu MySQL și aplicație
- [x] .dockerignore pentru optimizare
- [ ] Aplicația pornește cu `docker-compose up`
- [ ] Toate endpoint-urile funcționează
- [ ] Baza de date este populată
- [ ] Health check funcționează

---

**Succes la proiect! 🎉**
