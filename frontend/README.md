# Energy Management System - Frontend

Frontend application pentru sistemul de management energetic cu trei microservicii (Users, Devices, Auth).

## Tehnologii

- **Next.js 15** - React framework
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **Radix UI** - UI components
- **SWR** - Data fetching
- **Sonner** - Toast notifications

## Instalare

```bash
npm install --legacy-peer-deps
```

## Configurare

Creează fișierul `.env.local` cu următoarea configurație:

```env
NEXT_PUBLIC_API_URL=http://localhost
```

Acest URL va fi folosit pentru a comunica cu microserviciile prin Traefik reverse proxy.

## Rulare

```bash
npm run dev
```

Aplicația va rula pe `http://localhost:3000`

## Structură

```
frontend/
├── app/
│   ├── dashboard/
│   │   ├── login/          # Pagina de login
│   │   ├── register/       # Pagina de înregistrare
│   │   ├── users/          # Management utilizatori (ADMIN)
│   │   ├── devices/        # Management dispozitive (ADMIN)
│   │   └── page.tsx        # Dashboard overview
│   ├── layout.tsx          # Layout principal
│   └── page.tsx            # Pagina de start (redirect)
├── components/
│   ├── ui/                 # Componente UI reutilizabile
│   ├── auth-provider.tsx   # Context pentru autentificare
│   ├── protected-route.tsx # Guard pentru rute protejate
│   └── dashboard-layout.tsx # Layout pentru dashboard
├── lib/
│   ├── api.ts             # Client API pentru microservicii
│   ├── auth.ts            # Utilități pentru autentificare
│   └── utils.ts           # Utilități generale
└── hooks/
    └── use-toast.ts       # Hook pentru notificări
```

## Funcționalități

### Autentificare
- Login cu email și parolă
- Înregistrare utilizatori noi
- JWT token management
- Role-based access (ADMIN/CLIENT)

### Management Utilizatori (ADMIN)
- Vizualizare listă utilizatori
- Creare utilizatori noi
- Editare utilizatori existenți
- Ștergere utilizatori
- Atribuire roluri (ADMIN/CLIENT)

### Management Dispozitive (ADMIN)
- Vizualizare listă dispozitive
- Creare dispozitive noi
- Editare dispozitive existente
- Ștergere dispozitive
- Asociere dispozitive cu utilizatori
- Vizualizare consum maxim orar

## API Endpoints

Toate request-urile sunt făcute prin Traefik reverse proxy pe `http://localhost`:

- **Auth**: `/api/auth/*`
- **Users**: `/api/users/*`
- **Devices**: `/api/devices/*`

## Roluri

### ADMIN
- Acces complet la toate funcționalitățile
- Management utilizatori
- Management dispozitive
- Asociere dispozitive cu utilizatori

### CLIENT
- Acces la dashboard
- Vizualizare propriile dispozitive

## Build Production

```bash
npm run build
npm start
```

## Note

- Aplicația folosește `localStorage` pentru stocarea token-ului JWT
- Token-ul este validat automat la fiecare request
- Rutele protejate redirecționează automat către login dacă utilizatorul nu este autentificat
- Componentele UI sunt construite cu Radix UI și Tailwind CSS pentru consistență
