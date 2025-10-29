# Quick Start Guide

## Pași pentru a rula aplicația

### 1. Pornește microserviciile backend

În directorul principal al proiectului (unde se află `docker-compose.yml`):

```bash
docker-compose up -d
```

Acest lucru va porni:
- Traefik reverse proxy (port 80)
- 3 baze de date MySQL (porturi 3307, 3308, 3309)
- User microservice (intern pe 8081, expus prin Traefik la `/api/users`)
- Device microservice (intern pe 8082, expus prin Traefik la `/api/devices`)
- Auth microservice (intern pe 8083, expus prin Traefik la `/api/auth`)

### 2. Verifică că serviciile sunt pornite

```bash
docker-compose ps
```

Toate serviciile ar trebui să fie în starea "Up" și "healthy".

### 3. Pornește frontend-ul

În directorul `frontend`:

```bash
cd frontend
npm run dev
```

### 4. Accesează aplicația

Deschide browser-ul la: **http://localhost:3000**

Vei fi redirecționat automat către pagina de login.

## Primul login

### Opțiunea 1: Înregistrare nouă
1. Click pe "Register here" din pagina de login
2. Completează formularul cu:
   - Username
   - Email
   - Password
3. După înregistrare, vei fi redirecționat către login

### Opțiunea 2: Creează un admin direct în baza de date

Dacă ai nevoie de un cont admin imediat, poți crea unul direct în baza de date:

```bash
# Conectează-te la containerul MySQL auth
docker exec -it mysql-auth-db mysql -u root -proot energy_auth_db

# Inserează un utilizator admin (parola: "admin123")
INSERT INTO users (username, email, password, role) 
VALUES ('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');
```

Apoi loghează-te cu:
- Email: `admin@example.com`
- Password: `admin123`

## Funcționalități disponibile

### Pentru ADMIN:
- **Dashboard** - Overview al sistemului
- **Users** - Management complet utilizatori (CRUD)
- **Devices** - Management complet dispozitive (CRUD + asociere cu utilizatori)

### Pentru CLIENT:
- **Dashboard** - Overview personal
- Vizualizare dispozitive asociate (în dezvoltare)

## Troubleshooting

### Frontend nu se conectează la backend
- Verifică că toate containerele Docker sunt pornite: `docker-compose ps`
- Verifică că Traefik rulează pe port 80: `curl http://localhost/api/users`
- Verifică fișierul `.env.local` din frontend: `NEXT_PUBLIC_API_URL=http://localhost`

### Erori de autentificare
- Verifică că auth microservice este pornit
- Verifică logs: `docker-compose logs auth-microservice`
- Șterge token-ul din localStorage și încearcă din nou

### Erori de CORS
- Traefik ar trebui să gestioneze CORS automat
- Verifică configurația Traefik în `docker-compose.yml`

### Port 3000 deja ocupat
```bash
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Sau rulează pe alt port
npm run dev -- -p 3001
```

## Oprire servicii

```bash
# Oprește frontend
Ctrl + C în terminalul unde rulează npm run dev

# Oprește backend
docker-compose down

# Oprește și șterge volumele (ATENȚIE: șterge toate datele!)
docker-compose down -v
```

## Dezvoltare

### Hot reload
Frontend-ul are hot reload activat automat. Orice modificare în cod va reîncărca automat pagina.

### Verificare erori TypeScript
```bash
npm run lint
```

### Build pentru producție
```bash
npm run build
npm start
```

## Endpoints API disponibile

Toate prin `http://localhost`:

### Auth
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Înregistrare
- `GET /api/auth/validate?token=<token>` - Validare token

### Users
- `GET /api/users` - Lista utilizatori
- `GET /api/users/{id}` - Detalii utilizator
- `POST /api/users` - Creare utilizator
- `PUT /api/users/{id}` - Actualizare utilizator
- `DELETE /api/users/{id}` - Ștergere utilizator

### Devices
- `GET /api/devices` - Lista dispozitive
- `GET /api/devices/{id}` - Detalii dispozitiv
- `GET /api/devices/user/{userId}` - Dispozitive utilizator
- `POST /api/devices` - Creare dispozitiv
- `PUT /api/devices/{id}` - Actualizare dispozitiv
- `DELETE /api/devices/{id}` - Ștergere dispozitiv
- `PUT /api/devices/{deviceId}/assign/{userId}` - Asociere dispozitiv cu utilizator
