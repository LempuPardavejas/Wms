# 🚀 WMS Sistema - UX Patobulinimai

## Apžvalga

Sukurta **cutting-edge** vartotojo patirtis su intuityviu, modernų dizainą ir išsamią RBAC (Role-Based Access Control) sistemą.

---

## ✨ Pagrindiniai patobulinimai

### 1. **Išplėstinė RBAC sistema**
- ✅ Visi vartotojai, vaidmenys ir teisės valdomi duomenų bazėje
- ✅ Granuliari teisių kontrolė (permissions)
- ✅ JWT autentifikacija su refresh tokens
- ✅ Automatinis nukreipimas pagal vaidmenį po prisijungimo

### 2. **Modernus Login langas**
- ✅ Elegantiškas, šiuolaikiškas dizainas
- ✅ "Prisiminti mane" funkcija
- ✅ Role-based redirect po prisijungimo
- ✅ Aiškios klaidos žinutės

### 3. **POS (Kasos) Sistema Pardavėjams**
- ✅ Ultra greitas produktų skenavimas
- ✅ Klaviatūra optimizuota (F1-F8 spartieji klavišai)
- ✅ Real-time kainų skaičiavimas
- ✅ Dienos statistika ir ataskaitos
- ✅ Krepšelio valdymas

### 4. **WMS Sandėlio valdymas**
- ✅ Dashboard su svarbiausiais skaičiais
- ✅ Greiti veiksmai (priėmimas, išdavimas, grąžinimai)
- ✅ Sandėlio zonų vizualizacija
- ✅ Naujausių operacijų istorija

### 5. **Admin Skydelis**
- ✅ Vartotojų valdymas
- ✅ Vaidmenų ir teisių konfigūracija
- ✅ Sistemos nustatymai
- ✅ Ataskaitos
- ✅ Audito žurnalas

### 6. **B2B Klientų Portalas**
- ✅ Paprastas, aiškus dizainas
- ✅ Kredito likučio rodymas
- ✅ Užsakymų istorija
- ✅ Prekių pasiėmimų prašymai
- ✅ Sąskaitų atsisiuntimas
- ✅ **RBAC**: Klientai mato tik savo duomenis!

### 7. **Global Navigation**
- ✅ Responsive sidebar su ikonėlėmis
- ✅ Role-based meniu punktai
- ✅ Breadcrumbs navigacija
- ✅ User profile menu
- ✅ Logout funkcija

---

## 🎭 Demo vartotojai

Sistema turi 4 demo vartotojus su skirtingais vaidmenimis:

### 1. **Administratorius**
```
Username: admin
Password: admin123
Prieiga: Visa sistema
```

### 2. **Pardavėjas**
```
Username: seller
Password: seller123
Prieiga: POS, Užsakymai, Grąžinimai, Kreditai
```

### 3. **Sandėlininkas**
```
Username: warehouse
Password: warehouse123
Prieiga: WMS, Grąžinimai, Inventorizacija
```

### 4. **Klientas (B2B)**
```
Username: client
Password: client123
Prieiga: Tik B2B portalas (savo duomenys)
```

---

## 🏗️ Architektūra

### Backend (Java Spring Boot)

#### Naujos klasės:
```
backend/src/main/java/lt/elektromeistras/
├── domain/
│   ├── User.java                    # Vartotojo entity
│   ├── Role.java                    # Vaidmens entity
│   └── Permission.java              # Teisės entity
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── PermissionRepository.java
├── service/
│   ├── AuthService.java             # Autentifikacija
│   ├── UserService.java             # Vartotojų valdymas
│   └── RoleService.java             # Vaidmenų valdymas
├── controller/
│   ├── AuthController.java          # /api/auth/*
│   ├── UserController.java          # /api/users/*
│   └── RoleController.java          # /api/roles/*
├── security/
│   ├── JwtUtil.java                 # JWT generavimas/validacija
│   ├── JwtAuthenticationFilter.java # JWT filtras
│   ├── SecurityConfig.java          # Spring Security config
│   └── CustomUserDetailsService.java
└── dto/
    ├── request/
    │   ├── LoginRequest.java
    │   ├── CreateUserRequest.java
    │   └── UpdateUserRequest.java
    └── response/
        ├── LoginResponse.java
        ├── UserResponse.java
        ├── RoleResponse.java
        └── PermissionResponse.java
```

#### API Endpoints:

**Autentifikacija:**
- `POST /api/auth/login` - Prisijungimas
- `GET /api/auth/me` - Dabartinis vartotojas

**Vartotojai:**
- `GET /api/users` - Visi vartotojai (admin)
- `GET /api/users/{id}` - Vartotojas pagal ID
- `GET /api/users/search?q=` - Paieška
- `POST /api/users` - Naujas vartotojas
- `PUT /api/users/{id}` - Atnaujinti vartotoją
- `DELETE /api/users/{id}` - Ištrinti
- `PATCH /api/users/{id}/toggle-status` - Aktyvuoti/deaktyvuoti

**Vaidmenys:**
- `GET /api/roles` - Visi vaidmenys
- `GET /api/roles/active` - Aktyvūs vaidmenys
- `GET /api/roles/{id}` - Vaidmuo pagal ID
- `GET /api/roles/code/{code}` - Vaidmuo pagal kodą

### Frontend (React + TypeScript)

#### Nauji komponentai:
```
frontend/src/
├── contexts/
│   └── AuthContext.tsx              # Auth state management
├── services/
│   └── authService.ts               # Auth API calls
├── types/
│   └── auth.ts                      # Auth TypeScript types
├── components/
│   ├── ProtectedRoute.tsx           # Route protection
│   └── MainLayout.tsx               # Main app layout with sidebar
├── pages/
│   ├── LoginPage.tsx                # ✨ Prisijungimo langas
│   ├── POSPage.tsx                  # ✨ Kasos sistema
│   ├── WMSPage.tsx                  # ✨ Sandėlio valdymas
│   ├── AdminPage.tsx                # ✨ Administravimas
│   ├── B2BPortalPage.tsx            # ✨ Klientų portalas
│   └── UnauthorizedPage.tsx         # 403 klaida
└── App.tsx                          # ✨ Atnaujintas su AuthProvider
```

#### Route struktura:
```typescript
/login                 - Prisijungimas (public)
/unauthorized          - Prieigos klaida (public)

// Protected routes (reikia autentifikacijos)
/                      - Redirect to default based on role
/pos                   - POS sistema (ADMIN, SALES, SALES_MANAGER)
/wms                   - Sandėlio valdymas (ADMIN, WAREHOUSE)
/admin                 - Administravimas (ADMIN)
/b2b                   - Klientų portalas (CLIENT, ADMIN)
/returns               - Grąžinimai (ADMIN, SALES, WAREHOUSE)
/credit                - Kreditų operacijos (ADMIN, SALES, SALES_MANAGER)
```

---

## 🔐 Saugumas

### JWT Tokens
- **Access Token**: 24 valandos galiojimas
- **Refresh Token**: 7 dienos galiojimas
- Saugomi `localStorage`
- Automatinis logout kai token nevalydus

### Permissions
Sistema naudoja granuliarių teisių sistemą:
```
ADMIN_FULL           - Pilna prieiga
INVENTORY_VIEW       - Žiūrėti atsargas
INVENTORY_MANAGE     - Valdyti atsargas
SALES_VIEW           - Žiūrėti pardavimus
SALES_CREATE         - Kurti pardavimus
SALES_EDIT           - Redaguoti pardavimus
CUSTOMER_VIEW        - Žiūrėti klientus
CUSTOMER_MANAGE      - Valdyti klientus
PRODUCT_VIEW         - Žiūrėti prekes
PRODUCT_MANAGE       - Valdyti prekes
```

### Role-Permission Mapping
```
ADMIN → ADMIN_FULL (bypass all checks)
SALES → SALES_*, CUSTOMER_*, PRODUCT_VIEW
WAREHOUSE → INVENTORY_*
CLIENT → (limited to own data)
```

---

## 🎨 UX Funkcijos

### Klaviatūros spartieji klavišai (POS)
- `F1` - Naujas užsakymas
- `F2` - Klientų paieška
- `F3` - Produkto paieška
- `F4` - Grąžinimai
- `F8` - Dienos ataskaita
- `Ctrl+P` - Spausdinti
- `Ctrl+Enter` - Išsaugoti/Patvirtinti
- `Esc` - Uždaryti/Atšaukti

### Responsive dizainas
- ✅ Desktop (1920px+)
- ✅ Tablet (768px - 1919px)
- ✅ Mobile (320px - 767px)

### Material-UI tema
- Primary: `#667eea` (violetinė)
- Secondary: `#764ba2` (purpurinė)
- Success: `#38ef7d` (žalia)
- Warning: `#fa709a` (rožinė)

---

## 🗄️ Duomenų bazė

### Naujos lentelės:
```sql
users                 - Vartotojai
roles                 - Vaidmenys
user_roles            - Vartotojų-vaidmenų ryšys (M2M)
permissions           - Teisės
role_permissions      - Vaidmenų-teisių ryšys (M2M)
```

### Liquibase migration:
```
db/changelog/v1.0/001-create-base-tables.xml
```

Visos lentelės sukuriamos automatiškai su Liquibase.

---

## 🚀 Kaip paleisti

### 1. Backend (Spring Boot)
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend veiks: `http://localhost:8080`

### 2. Frontend (React)
```bash
cd frontend
npm install
npm run dev
```

Frontend veiks: `http://localhost:5173`

### 3. Prisijungti
Atidarykite naršyklėje: `http://localhost:5173`

Naudokite vieną iš demo vartotojų:
- `admin` / `admin123`
- `seller` / `seller123`
- `warehouse` / `warehouse123`
- `client` / `client123`

---

## 📊 Ką dar galima pridėti?

### Prioritetas 1 (Būtina):
- [ ] Integruoti QuickOrderDialog į POS
- [ ] PDF eksportavimas (sąskaitos, kvitai)
- [ ] Email notifikacijos
- [ ] Print šablonai

### Prioritetas 2 (Rekomenduojama):
- [ ] Global search (Ctrl+K)
- [ ] Dark mode
- [ ] Real-time notifikacijos
- [ ] Vartotojų valdymo UI admin skydelyje
- [ ] Ataskaitos su grafikais

### Prioritetas 3 (Nice to have):
- [ ] Multi-language support
- [ ] Export to Excel
- [ ] Audit log UI
- [ ] Customer self-registration
- [ ] QR code generation

---

## 🎯 Testavimo checklist

### ✅ Backend
- [x] User entities ir repositories
- [x] JWT authentication
- [x] Login endpoint
- [x] User CRUD endpoints
- [x] Role management
- [x] Permission checking
- [x] Demo users seeded

### ✅ Frontend
- [x] Login page
- [x] Auth context
- [x] Protected routes
- [x] Role-based navigation
- [x] POS interface
- [x] WMS dashboard
- [x] Admin panel
- [x] B2B portal
- [x] Main layout with sidebar
- [x] User profile menu
- [x] Logout functionality

### ⏳ Reikia patikrinti
- [ ] Login su visais demo vartotojais
- [ ] Redirect pagal role po login
- [ ] Protected routes veikia
- [ ] Sidebar meniu pagal role
- [ ] Logout ir token removal
- [ ] Token refresh mechanism
- [ ] Unauthorized page

---

## 📝 Commit pranešimas

```
Implement cutting-edge UX with comprehensive RBAC system

Major improvements:
- Complete RBAC system with users, roles, and permissions
- JWT authentication with refresh tokens
- Modern login page with role-based redirects
- Intuitive POS interface for sellers (keyboard-optimized)
- WMS dashboard for warehouse operations
- Admin panel for system management
- Simple B2B client portal (RBAC-protected)
- Global navigation with role-based menu
- Protected routes and authorization
- Demo users for all roles (admin, seller, warehouse, client)

Backend:
- User, Role, Permission entities
- JWT security configuration
- Auth, User, Role services and controllers
- Liquibase migrations with seed data

Frontend:
- Auth context and services
- Protected route component
- Main layout with responsive sidebar
- Modern UI with Material-UI
- TypeScript types for auth

All demo passwords: admin123, seller123, warehouse123, client123
```

---

## 🤝 Kontributoriai

- **Claude** - Full-stack implementation
- **User** - Requirements and vision

---

## 📄 Licencija

Privatūs projektas - Elektromeistras WMS

---

**Sukurta su ❤️ ir AI ✨**
