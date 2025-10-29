# Frontend Structure Documentation

## Arhitectura aplicației

Frontend-ul este construit cu Next.js 15 (App Router) și folosește o arhitectură modulară cu separarea clară a responsabilităților.

## Structura de directoare

```
frontend/
│
├── app/                          # Next.js App Router
│   ├── dashboard/               # Rute protejate pentru dashboard
│   │   ├── devices/            # Management dispozitive
│   │   │   └── page.tsx        # Pagina principală devices (CRUD)
│   │   ├── login/              # Autentificare
│   │   │   └── page.tsx        # Pagina de login
│   │   ├── register/           # Înregistrare
│   │   │   └── page.tsx        # Pagina de înregistrare
│   │   ├── users/              # Management utilizatori
│   │   │   └── page.tsx        # Pagina principală users (CRUD)
│   │   └── page.tsx            # Dashboard overview
│   ├── globals.css             # Stiluri globale Tailwind
│   ├── layout.tsx              # Layout principal cu AuthProvider
│   └── page.tsx                # Pagina de start (redirect logic)
│
├── components/                  # Componente React reutilizabile
│   ├── gl/                     # Componente Three.js (nefolosite)
│   ├── ui/                     # Componente UI de bază
│   │   ├── button.tsx          # Buton cu variante
│   │   ├── card.tsx            # Card container
│   │   ├── dialog.tsx          # Modal dialog
│   │   ├── input.tsx           # Input field
│   │   ├── label.tsx           # Label pentru form
│   │   ├── select.tsx          # Dropdown select
│   │   ├── table.tsx           # Tabel cu header/body/row
│   │   ├── toast.tsx           # Toast notification
│   │   └── toaster.tsx         # Toast container
│   ├── auth-provider.tsx       # Context provider pentru autentificare
│   ├── dashboard-layout.tsx    # Layout pentru pagini dashboard
│   ├── protected-route.tsx     # HOC pentru protecția rutelor
│   └── utils.ts                # Utilități pentru componente
│
├── hooks/                       # Custom React hooks
│   └── use-toast.ts            # Hook pentru toast notifications
│
├── lib/                         # Biblioteci și utilități
│   ├── api.ts                  # Client API pentru microservicii
│   ├── auth.ts                 # Utilități JWT și autentificare
│   └── utils.ts                # Utilități generale (cn, etc.)
│
├── .env.local                   # Variabile de mediu (nu în git)
├── .gitignore                   # Fișiere ignorate de git
├── next.config.ts               # Configurare Next.js
├── package.json                 # Dependențe npm
├── postcss.config.mjs           # Configurare PostCSS
├── tailwind.config.ts           # Configurare Tailwind CSS
└── tsconfig.json                # Configurare TypeScript
```

## Fluxul de date

### 1. Autentificare
```
User Input → Login Page → API Client → Auth Microservice
                ↓
         JWT Token stored in localStorage
                ↓
         AuthProvider updates context
                ↓
         Protected routes check auth state
```

### 2. CRUD Operations
```
User Action → Page Component → API Client → Microservice
                                    ↓
                            SWR cache update
                                    ↓
                            UI re-render
```

## Componente principale

### AuthProvider (`components/auth-provider.tsx`)
- Gestionează starea de autentificare globală
- Stochează user info în context
- Oferă funcții `login()` și `logout()`
- Persistă token în localStorage

### ProtectedRoute (`components/protected-route.tsx`)
- HOC pentru protecția rutelor
- Verifică autentificarea
- Verifică rolul utilizatorului (ADMIN/CLIENT)
- Redirecționează către login dacă nu e autentificat

### DashboardLayout (`components/dashboard-layout.tsx`)
- Layout comun pentru toate paginile dashboard
- Sidebar cu navigare
- Header cu info utilizator
- Buton logout

### API Client (`lib/api.ts`)
- Singleton pentru comunicarea cu backend
- Metode pentru toate operațiunile CRUD
- Gestionează automat header-ul Authorization
- Interfețe TypeScript pentru toate entitățile

## Pagini și funcționalități

### `/` (Home)
- Verifică starea de autentificare
- Redirecționează către `/dashboard` dacă autentificat
- Redirecționează către `/dashboard/login` dacă nu

### `/dashboard/login`
- Formular de login (email + password)
- Validare client-side
- Stochează JWT token la succes
- Link către register

### `/dashboard/register`
- Formular de înregistrare
- Validare password match
- Creează cont CLIENT by default
- Redirecționează către login la succes

### `/dashboard` (Overview)
- Protejat (orice rol)
- Cards cu statistici
- Quick actions
- Recent activity

### `/dashboard/users` (ADMIN only)
- Tabel cu toți utilizatorii
- CRUD complet:
  - Create: Dialog cu form
  - Read: Tabel cu toate datele
  - Update: Dialog cu form pre-populat
  - Delete: Dialog de confirmare
- Filtrare și sortare (în dezvoltare)

### `/dashboard/devices` (ADMIN only)
- Tabel cu toate dispozitivele
- CRUD complet + asociere cu utilizatori
- Afișare user asociat în tabel
- Dialog special pentru asociere device-user

## State Management

### Global State (Context)
- **AuthContext**: User info, login/logout functions
- Accesat prin `useAuth()` hook

### Local State (useState)
- Form data în pagini
- Dialog open/close states
- Loading states

### Server State (SWR)
- Cache pentru date de la API
- Auto-revalidare
- Optimistic updates
- Mutate pentru refresh manual

## Styling

### Tailwind CSS
- Utility-first CSS framework
- Custom theme în `tailwind.config.ts`
- Dark mode support (în dezvoltare)

### Componente UI
- Construite cu Radix UI primitives
- Styled cu Tailwind
- Variante definite cu `class-variance-authority`

### Culori și teme
```css
--background: hsl(0 0% 100%)
--foreground: hsl(222.2 84% 4.9%)
--primary: hsl(222.2 47.4% 11.2%)
--secondary: hsl(210 40% 96.1%)
--muted: hsl(210 40% 96.1%)
--accent: hsl(210 40% 96.1%)
--destructive: hsl(0 84.2% 60.2%)
--border: hsl(214.3 31.8% 91.4%)
--input: hsl(214.3 31.8% 91.4%)
--ring: hsl(222.2 84% 4.9%)
```

## API Integration

### Base URL
```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost"
```

### Request Flow
1. Component calls API method
2. API client adds Authorization header
3. Fetch request to Traefik
4. Traefik routes to correct microservice
5. Response parsed and returned
6. SWR updates cache
7. Component re-renders

### Error Handling
- Try-catch în toate API calls
- Toast notifications pentru erori
- Fallback UI pentru loading states

## TypeScript Types

### User
```typescript
interface User {
  id: string
  username: string
  role: "ADMIN" | "CLIENT"
  fullName: string
  email: string
}
```

### Device
```typescript
interface Device {
  id: string
  description: string
  address: string
  maximumHourlyConsumption: number
  userId: string | null
  username: string | null
  userEmail: string | null
  createdAt: string
  updatedAt: string
}
```

### Auth Response
```typescript
interface AuthResponse {
  token: string
  type: string
  userId: string
  username: string
  role: "ADMIN" | "CLIENT"
}
```

## Best Practices implementate

1. **Type Safety**: TypeScript strict mode
2. **Component Reusability**: UI components în `components/ui/`
3. **Separation of Concerns**: API logic separat de UI
4. **Error Handling**: Try-catch + user feedback
5. **Loading States**: Skeleton screens și spinners
6. **Optimistic Updates**: SWR pentru UX mai bun
7. **Protected Routes**: Role-based access control
8. **Clean Code**: ESLint + Prettier (configurate)

## Dezvoltare viitoare

### Features planificate
- [ ] Client dashboard cu dispozitive proprii
- [ ] Real-time updates cu WebSocket
- [ ] Grafice pentru consum energetic
- [ ] Export date (CSV, PDF)
- [ ] Filtrare și sortare avansată
- [ ] Paginare pentru liste mari
- [ ] Dark mode toggle
- [ ] Multi-language support
- [ ] Email notifications
- [ ] Password reset flow

### Îmbunătățiri tehnice
- [ ] Unit tests (Jest + React Testing Library)
- [ ] E2E tests (Playwright)
- [ ] Performance optimization
- [ ] PWA support
- [ ] Docker containerization
- [ ] CI/CD pipeline
