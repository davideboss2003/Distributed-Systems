# 🐳 GHID COMPLET - Deployment cu Docker Desktop

## 📋 PREREQUISITE

Înainte de a începe, asigură-te că ai instalat:
- ✅ **Docker Desktop** (descarcă de la: https://www.docker.com/products/docker-desktop)
- ✅ Docker Desktop este **pornit** (vezi iconița în system tray)

---

## PASUL 1: Verifică că Docker Desktop rulează

### Windows:
1. Caută "Docker Desktop" în Start Menu
2. Deschide aplicația
3. Așteaptă până vezi "Docker Desktop is running" (iconița devine verde)
4. Verifică în terminal:

```bash
docker --version
docker-compose --version
```

**Output așteptat:**
```
Docker version 24.x.x
Docker Compose version v2.x.x
```

---

## PASUL 2: Oprește MySQL local (IMPORTANT!)

Dacă ai MySQL pornit local pe portul 3306, trebuie să-l oprești pentru a evita conflictele:

```bash
# Windows - oprește serviciul MySQL
net stop MySQL80

# SAU din Services (services.msc)
# Caută "MySQL80" și dă Stop
```

**De ce?** Docker va porni propriul MySQL pe port 3307, dar aplicația din container va folosi portul intern 3306.

---

## PASUL 3: Navighează în directorul proiectului

Deschide **Command Prompt** sau **PowerShell** și navighează în folderul proiectului:

```bash
cd C:\path\to\user-microservice
# Înlocuiește cu calea ta reală
```

Verifică că ești în directorul corect (trebuie să vezi fișierele Docker):

```bash
dir
```

**Trebuie să vezi:**
- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`
- `pom.xml`
- `src/`

---

## PASUL 4: Build și pornește containerele

### Opțiunea A: Cu Docker Desktop GUI

1. Deschide **Docker Desktop**
2. Click pe **"Containers"** în sidebar
3. Click pe butonul **"+"** (sau "Create")
4. Selectează **"From docker-compose.yml"**
5. Navighează la fișierul tău `docker-compose.yml`
6. Click **"Start"**

### Opțiunea B: Cu Command Line (RECOMANDAT)

```bash
# Build imaginile și pornește containerele
docker-compose up --build
```

**Ce se întâmplă:**
1. ⏳ Docker descarcă imaginea MySQL 8.0 (prima dată durează ~2-3 minute)
2. 🔨 Docker compilează aplicația ta Spring Boot cu Maven (~3-5 minute)
3. 📦 Docker creează imaginea pentru aplicația ta
4. 🚀 Docker pornește ambele containere (MySQL + aplicația ta)

**Output așteptat:**
```
[+] Building 120.5s (15/15) FINISHED
[+] Running 3/3
 ✔ Network user-microservice_users-network    Created
 ✔ Container mysql-users-db                   Started
 ✔ Container user-microservice-app            Started
```

---

## PASUL 5: Verifică că containerele rulează

### În Docker Desktop:
1. Click pe **"Containers"** în sidebar
2. Ar trebui să vezi un grup: **"user-microservice"**
3. Sub el, 2 containere:
   - 🟢 **mysql-users-db** (running)
   - 🟢 **user-microservice-app** (running)

### În Command Line:
```bash
docker-compose ps
```

**Output așteptat:**
```
NAME                        STATUS              PORTS
mysql-users-db              Up (healthy)        0.0.0.0:3307->3306/tcp
user-microservice-app       Up (healthy)        0.0.0.0:8081->8081/tcp
```

---

## PASUL 6: Verifică log-urile aplicației

### În Docker Desktop:
1. Click pe containerul **"user-microservice-app"**
2. Click pe tab-ul **"Logs"**
3. Caută mesajul: `Started UserMicroserviceApplication in X.XXX seconds`

### În Command Line:
```bash
# Vezi log-urile în timp real
docker-compose logs -f user-microservice

# SAU doar ultimele 50 de linii
docker-compose logs --tail=50 user-microservice
```

**Mesaj de succes:**
```
user-microservice-app  | Started UserMicroserviceApplication in 15.234 seconds
user-microservice-app  | Tomcat started on port 8081
```

---

## PASUL 7: Testează aplicația în Postman

Acum aplicația rulează în Docker! Testează-o exact ca înainte:

### TEST 1: Health Check
**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8081/actuator/health`

**Response așteptat:**
```json
{
  "status": "UP"
}
```

### TEST 2: Lista utilizatori
**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8081/api/users`

**Response:** Lista goală `[]` (baza de date este nouă)

### TEST 3: Creează primul user
**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8081/api/users`
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "username": "admin",
  "password": "admin123456",
  "role": "ADMIN",
  "fullName": "Administrator",
  "email": "admin@energy.ro"
}
```

**Response așteptat (201 CREATED):**
```json
{
  "id": "a1b2c3d4-...",
  "username": "admin",
  "role": "ADMIN",
  "fullName": "Administrator",
  "email": "admin@energy.ro"
}
```

---

## PASUL 8: Conectează-te la MySQL din Docker

Dacă vrei să vezi baza de date din container:

### Opțiunea A: Cu Docker Desktop
1. Click pe containerul **"mysql-users-db"**
2. Click pe tab-ul **"Exec"**
3. Rulează:
```bash
mysql -u root -proot energy_users_db
```

### Opțiunea B: Cu Command Line
```bash
# Conectează-te la containerul MySQL
docker exec -it mysql-users-db mysql -u root -proot energy_users_db
```

**Comenzi SQL utile:**
```sql
-- Vezi tabelele
SHOW TABLES;

-- Vezi userii
SELECT * FROM users;

-- Ieși
EXIT;
```

---

## PASUL 9: Oprește containerele

### Când termini testarea:

```bash
# Oprește containerele (datele rămân salvate)
docker-compose down
```

### Dacă vrei să ștergi și datele (baza de date):

```bash
# Oprește și șterge volume-urile
docker-compose down -v
```

**În Docker Desktop:**
1. Click pe grupul **"user-microservice"**
2. Click pe butonul **"Stop"** (⏹️)
3. Sau click pe **"Delete"** (🗑️) pentru a șterge tot

---

## PASUL 10: Repornește containerele (fără rebuild)

Dacă ai oprit containerele și vrei să le repornești:

```bash
# Pornește containerele existente (fără rebuild)
docker-compose up

# SAU în background (detached mode)
docker-compose up -d
```

---

## 🔄 WORKFLOW COMPLET

### Prima dată (cu build):
```bash
# 1. Build și pornește
docker-compose up --build

# 2. Testează în Postman
# http://localhost:8081/api/users

# 3. Oprește când termini
docker-compose down
```

### Următoarele rulări (fără rebuild):
```bash
# 1. Pornește containerele existente
docker-compose up

# 2. Testează în Postman

# 3. Oprește
docker-compose down
```

### Când modifici codul:
```bash
# 1. Oprește containerele
docker-compose down

# 2. Rebuild imaginea
docker-compose up --build

# 3. Testează noile modificări
```

---

## 📊 COMENZI UTILE DOCKER

### Verificare containere:
```bash
# Vezi toate containerele care rulează
docker ps

# Vezi toate containerele (inclusiv oprite)
docker ps -a

# Vezi log-urile unui container
docker logs user-microservice-app

# Vezi log-urile în timp real
docker logs -f user-microservice-app
```

### Verificare imagini:
```bash
# Vezi toate imaginile
docker images

# Șterge imagini nefolosite
docker image prune
```

### Verificare volume-uri:
```bash
# Vezi toate volume-urile
docker volume ls

# Șterge volume-uri nefolosite
docker volume prune
```

### Curățare completă:
```bash
# Șterge tot ce nu este folosit (containere, imagini, volume-uri, rețele)
docker system prune -a --volumes
```

---

## 🐛 TROUBLESHOOTING

### Problema 1: "Port 8081 is already in use"
**Cauză:** Aplicația Spring Boot rulează deja local.

**Soluție:**
```bash
# Oprește aplicația Spring Boot locală
# SAU schimbă portul în docker-compose.yml:
ports:
  - "8082:8081"  # Acum aplicația va fi pe localhost:8082
```

### Problema 2: "Port 3307 is already in use"
**Cauză:** Alt serviciu folosește portul 3307.

**Soluție:**
```bash
# Schimbă portul în docker-compose.yml:
ports:
  - "3308:3306"  # Folosește portul 3308
```

### Problema 3: "Cannot connect to MySQL"
**Cauză:** MySQL nu a pornit complet.

**Soluție:**
```bash
# Verifică health check-ul
docker-compose ps

# Așteaptă până vezi "healthy" la mysql-users-db
# SAU verifică log-urile:
docker-compose logs mysql-users
```

### Problema 4: "Build failed" sau "Maven error"
**Cauză:** Erori de compilare în cod.

**Soluție:**
```bash
# Testează build-ul local mai întâi:
mvn clean package -DskipTests

# Dacă merge local, apoi:
docker-compose up --build
```

### Problema 5: Containerele nu pornesc
**Cauză:** Docker Desktop nu rulează sau erori de configurare.

**Soluție:**
1. Verifică că Docker Desktop este pornit (iconița verde)
2. Restart Docker Desktop
3. Verifică log-urile:
```bash
docker-compose logs
```

### Problema 6: "No space left on device"
**Cauză:** Docker a umplut disk-ul cu imagini/containere vechi.

**Soluție:**
```bash
# Curăță tot ce nu este folosit
docker system prune -a --volumes

# Confirmă cu "y"
```

---

## 📝 DIFERENȚE: Local vs Docker

| Aspect | Local (fără Docker) | Docker |
|--------|---------------------|--------|
| **MySQL** | localhost:3306 | localhost:3307 |
| **Aplicație** | localhost:8081 | localhost:8081 |
| **Pornire** | `mvn spring-boot:run` | `docker-compose up` |
| **Oprire** | `Ctrl+C` | `docker-compose down` |
| **Baza de date** | MySQL local | MySQL în container |
| **Modificări cod** | Hot reload | Rebuild necesar |
| **Portabilitate** | Depinde de sistem | Funcționează peste tot |

---

## ✅ CHECKLIST FINAL

- [ ] Docker Desktop instalat și pornit
- [ ] MySQL local oprit (dacă există)
- [ ] Fișierele `Dockerfile`, `docker-compose.yml`, `.dockerignore` create
- [ ] Rulat `docker-compose up --build` cu succes
- [ ] Ambele containere sunt "healthy" în Docker Desktop
- [ ] Aplicația răspunde la `http://localhost:8081/actuator/health`
- [ ] Poți crea useri prin Postman
- [ ] Poți vedea userii în baza de date MySQL din container

---

## 🎓 PENTRU PREZENTAREA LA FACULTATE

### Ce să arăți:
1. **Docker Desktop** cu cele 2 containere running (mysql-users-db + user-microservice-app)
2. **Postman** cu request-uri funcționale (GET, POST, PUT, DELETE)
3. **Logs** din Docker Desktop care arată că aplicația rulează
4. **MySQL** din container cu datele salvate

### Ce să explici:
- **Dockerfile:** Multi-stage build (build + runtime)
- **docker-compose.yml:** Orchestrare MySQL + aplicație
- **.dockerignore:** Optimizare build
- **Networking:** Containerele comunică prin rețea Docker
- **Volumes:** Persistența datelor MySQL
- **Health checks:** Monitoring automat

---

**Succes cu proiectul! 🚀**

**Aplicația ta rulează acum în containere Docker, exact cum cere assignment-ul!**
