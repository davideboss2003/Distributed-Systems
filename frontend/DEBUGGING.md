# Debugging Guide

## Verificare rapidă a sistemului

### 1. Verifică că toate serviciile backend rulează

```bash
docker-compose ps
```

Toate ar trebui să fie "Up" și "healthy".

### 2. Testează endpoint-urile direct

```bash
# Test Traefik dashboard
curl http://localhost:8080/api/overview

# Test auth endpoint
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'

# Test users endpoint (cu token)
curl http://localhost/api/users \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Test devices endpoint
curl http://localhost/api/devices \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. Verifică logs-urile microserviciilor

```bash
# Toate logs
docker-compose logs -f

# Doar auth service
docker-compose logs -f auth-microservice

# Doar user service
docker-compose logs -f user-microservice

# Doar device service
docker-compose logs -f device-microservice

# Traefik logs
docker-compose logs -f traefik
```

## Probleme comune și soluții

### Frontend nu se conectează la backend

**Simptom**: Erori CORS sau "Failed to fetch"

**Soluții**:
1. Verifică că backend-ul rulează:
   ```bash
   docker-compose ps
   ```

2. Verifică `.env.local`:
   ```bash
   cat .env.local
   # Ar trebui să conțină: NEXT_PUBLIC_API_URL=http://localhost
   ```

3. Testează direct endpoint-ul:
   ```bash
   curl http://localhost/api/users
   ```

4. Verifică browser console pentru erori detaliate

### Token invalid sau expirat

**Simptom**: Redirect constant către login

**Soluții**:
1. Șterge token-ul din localStorage:
   ```javascript
   // În browser console
   localStorage.clear()
   ```

2. Verifică validitatea token-ului:
   ```javascript
   // În browser console
   const token = localStorage.getItem('token')
   console.log(token)
   
   // Decode JWT (fără verificare)
   const base64Url = token.split('.')[1]
   const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
   const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
     return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
   }).join(''))
   console.log(JSON.parse(jsonPayload))
   ```

3. Login din nou

### Erori de TypeScript

**Simptom**: Erori în terminal sau browser

**Soluții**:
1. Reinstalează dependențele:
   ```bash
   rm -rf node_modules package-lock.json
   npm install --legacy-peer-deps
   ```

2. Verifică diagnostice:
   ```bash
   npm run lint
   ```

3. Restart dev server:
   ```bash
   # Ctrl+C apoi
   npm run dev
   ```

### Port 3000 ocupat

**Simptom**: "Port 3000 is already in use"

**Soluții Windows**:
```bash
# Găsește procesul
netstat -ano | findstr :3000

# Omoară procesul (înlocuiește PID cu numărul găsit)
taskkill /PID <PID> /F

# Sau rulează pe alt port
npm run dev -- -p 3001
```

### Baza de date goală

**Simptom**: Nu există utilizatori sau dispozitive

**Soluții**:
1. Creează un admin:
   ```bash
   docker exec -it mysql-auth-db mysql -u root -proot energy_auth_db
   
   INSERT INTO users (username, email, password, role) 
   VALUES ('admin', 'admin@example.com', 
           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
           'ADMIN');
   ```

2. Sau înregistrează-te prin UI

### Microservice nu pornește

**Simptom**: Container în stare "Restarting" sau "Unhealthy"

**Soluții**:
1. Verifică logs:
   ```bash
   docker-compose logs user-microservice
   ```

2. Verifică că baza de date este healthy:
   ```bash
   docker-compose ps mysql-users
   ```

3. Restart serviciu:
   ```bash
   docker-compose restart user-microservice
   ```

4. Rebuild dacă ai făcut modificări:
   ```bash
   docker-compose up -d --build user-microservice
   ```

## Debugging în browser

### Chrome DevTools

1. **Network Tab**
   - Verifică toate request-urile
   - Verifică status codes
   - Verifică headers (Authorization)
   - Verifică response bodies

2. **Console Tab**
   - Verifică erori JavaScript
   - Testează API calls manual:
     ```javascript
     fetch('http://localhost/api/users', {
       headers: {
         'Authorization': 'Bearer ' + localStorage.getItem('token')
       }
     }).then(r => r.json()).then(console.log)
     ```

3. **Application Tab**
   - Verifică localStorage
   - Șterge date dacă e necesar

4. **React DevTools**
   - Inspectează component state
   - Verifică props
   - Verifică context values

## Debugging în VS Code

### Launch Configuration

Creează `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Next.js: debug server-side",
      "type": "node-terminal",
      "request": "launch",
      "command": "npm run dev"
    },
    {
      "name": "Next.js: debug client-side",
      "type": "chrome",
      "request": "launch",
      "url": "http://localhost:3000"
    }
  ]
}
```

### Breakpoints

1. Pune breakpoints în cod
2. Rulează debug configuration
3. Inspectează variabile
4. Step through code

## Verificare performanță

### Lighthouse

1. Deschide Chrome DevTools
2. Lighthouse tab
3. Generate report
4. Verifică:
   - Performance
   - Accessibility
   - Best Practices
   - SEO

### React DevTools Profiler

1. Instalează React DevTools extension
2. Profiler tab
3. Record interaction
4. Analizează render times

## Comenzi utile

### Docker

```bash
# Restart toate serviciile
docker-compose restart

# Rebuild și restart
docker-compose up -d --build

# Oprește toate
docker-compose down

# Oprește și șterge volume-uri (ATENȚIE: pierde date!)
docker-compose down -v

# Verifică resource usage
docker stats

# Intră în container
docker exec -it user-microservice-app bash

# Verifică logs în timp real
docker-compose logs -f --tail=100
```

### NPM

```bash
# Curăță cache
npm cache clean --force

# Reinstalează dependențe
rm -rf node_modules package-lock.json
npm install --legacy-peer-deps

# Verifică versiuni
npm list

# Audit securitate
npm audit

# Fix vulnerabilități
npm audit fix
```

### Git

```bash
# Verifică status
git status

# Resetează modificări
git checkout -- .

# Resetează la commit anterior
git reset --hard HEAD~1

# Stash modificări
git stash
git stash pop
```

## Monitorizare în producție

### Health Checks

```bash
# Frontend
curl http://localhost:3000

# Backend services prin Traefik
curl http://localhost/api/users
curl http://localhost/api/devices
curl http://localhost/api/auth/validate?token=test

# Traefik dashboard
curl http://localhost:8080/api/overview
```

### Logs

```bash
# Frontend logs (în terminal unde rulează npm run dev)
# Backend logs
docker-compose logs -f

# Export logs
docker-compose logs > logs.txt
```

## Testare manuală

### Checklist funcționalități

- [ ] Login cu credențiale valide
- [ ] Login cu credențiale invalide (ar trebui să eșueze)
- [ ] Register utilizator nou
- [ ] Logout
- [ ] Creare utilizator (ADMIN)
- [ ] Editare utilizator (ADMIN)
- [ ] Ștergere utilizator (ADMIN)
- [ ] Creare dispozitiv (ADMIN)
- [ ] Editare dispozitiv (ADMIN)
- [ ] Ștergere dispozitiv (ADMIN)
- [ ] Asociere dispozitiv cu utilizator (ADMIN)
- [ ] Acces la rute protejate fără autentificare (ar trebui să redirecționeze)
- [ ] Acces CLIENT la rute ADMIN (ar trebui să redirecționeze)

### Test scenarii

1. **Happy Path**
   - Register → Login → Create User → Create Device → Assign Device → Logout

2. **Error Handling**
   - Login cu parolă greșită
   - Creare utilizator cu email duplicat
   - Ștergere utilizator inexistent
   - Request fără token

3. **Edge Cases**
   - Token expirat
   - Network offline
   - Backend down
   - Concurrent requests

## Contact și suport

Pentru probleme care nu sunt acoperite aici:
1. Verifică logs-urile detaliate
2. Caută eroarea în documentația Next.js
3. Verifică issues pe GitHub pentru dependențe
4. Creează un issue nou cu:
   - Descrierea problemei
   - Pași de reproducere
   - Logs relevante
   - Environment info (OS, Node version, etc.)
