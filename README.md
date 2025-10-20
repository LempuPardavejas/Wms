# ELEKTRO MEISTRAS - Prekių Valdymo Sistema

Kompaktiška elektros prekių didmeninės ir mažmeninės prekybos valdymo sistema.

## 📋 Turinys

- [Apžvalga](#apžvalga)
- [Technologijos](#technologijos)
- [Funkcionalumas](#funkcionalumas)
- [Paleidimas](#paleidimas)
- [Kūrimas](#kūrimas)
- [API Dokumentacija](#api-dokumentacija)
- [Architektūra](#architektūra)

## 🎯 Apžvalga

ELEKTRO MEISTRAS yra visapusiška prekių valdymo sistema, sukurta elektros prekių pardavimui ir sandėlio valdymui. Sistema apima:

- **POS (Point of Sale)** - Greitą pardavimų valdymą
- **WMS (Warehouse Management)** - Mobiliąją sandėlio valdymo sistemą
- **Admin Panel** - Administratoriaus sąsają
- **B2B Portal** - Verslo klientų portalą

### Pagrindiniai Tikslai

- ✅ Sumažinti pardavimo laiką 60% (5min → 2min)
- ✅ Pasiekti 99% inventorizacijos tikslumą
- ✅ Automatizuoti 80% banko suderinimo
- ✅ Padidinti pardavimus 25% per metus

## 🛠 Technologijos

### Backend
- **Java 17** + **Spring Boot 3.2**
- **Spring Security** (JWT autentifikacija)
- **Spring Data JPA** + **Hibernate**
- **Liquibase** (duomenų bazės migracijos)
- **PostgreSQL 15** (pagrindinė DB)
- **Redis 7** (kešavimas/sesijos)
- **Elasticsearch 8** (paieška)

### Frontend
- **React 18** + **TypeScript 5**
- **Material-UI v5** (UI komponentai)
- **Redux Toolkit** (būsenos valdymas)
- **React Query** (duomenų gavimas)
- **React Router v6** (navigacija)
- **Vite** (build įrankis)

### Infrastructure
- **Docker** + **Docker Compose**
- **Nginx** (reverse proxy)
- **Maven** (Java build)

## ✨ Funkcionalumas

### MVP Funkcijos

#### POS Panelė
- ⚡ Universali paieška (produktai, klientai, užsakymai)
- 🛒 Realaus laiko krepšelio valdymas
- 💰 Dinaminis kainų skaičiavimas
- 📦 Atsargų tikrinimas
- 🧮 Skydelių komplektavimas
- 💳 Kelių mokėjimo metodų palaikymas

#### Sandėlio Valdymas (WMS)
- 📱 Mobili sąsaja sandėlininkams
- 🎯 Optimizuotas prekių rinkimo maršrutas
- 📊 Prekių gavimas ir išdavimas
- 🔌 Kabelių apdorojimas (ritiniai, pjovimas)
- ✅ Inventorizacija

#### Administravimas
- 📈 Pardavimų ataskaitos
- 💼 Klientų valdymas
- 🏦 Banko mokėjimų suderinimas
- 💵 Kainų valdymas
- 📋 Sąskaitų faktūrų generavimas

## 🚀 Paleidimas

### Reikalavimai

- Docker & Docker Compose
- Java 17+ (jei paleidžiate be Docker)
- Node.js 18+ (jei paleidžiate be Docker)
- Maven 3.8+ (jei paleidžiate be Docker)

### Greitas Paleidimas su Docker

1. **Klonuokite repozitoriją**
```bash
git clone <repository-url>
cd elektromeistras
```

2. **Sukurkite .env failą**
```bash
cp .env.example .env
# Redaguokite .env failą su savo nustatymais
```

3. **Paleiskite su Docker Compose**
```bash
docker-compose up -d
```

4. **Prieiga prie aplikacijų**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- PostgreSQL: localhost:5432
- Redis: localhost:6379
- Elasticsearch: http://localhost:9200

### Kūrimo Režimas (be Docker)

#### Backend

1. **Paleiskite PostgreSQL, Redis, Elasticsearch**
```bash
docker-compose up -d postgres redis elasticsearch
```

2. **Paleiskite Backend aplikaciją**
```bash
cd backend
mvn spring-boot:run
```

Backend bus pasiekiamas: http://localhost:8080

#### Frontend

1. **Įdiekite priklausomybes**
```bash
cd frontend
npm install
```

2. **Paleiskite development serverį**
```bash
npm run dev
```

Frontend bus pasiekiamas: http://localhost:3000

## 📊 Duomenų Bazė

### Schemos Migracija

Sistema naudoja Liquibase automatiniam duomenų bazės schemų valdymui. Migracijos paleistos automatiškai paleidus aplikaciją.

### Pradiniai Duomenys

Sukuriami šie pradiniai duomenys:
- Vartotojų rolės (ADMIN, SALES, WAREHOUSE)
- Teisės (SALES_CREATE, INVENTORY_MANAGE, etc.)
- Numatytasis sandėlis (WH-01)
- Kainų grupės (STANDARD, CONTRACTOR, BUSINESS)

### Manuali Duomenų Bazės Migracija
```bash
cd backend
mvn liquibase:update
```

## 🔑 API Dokumentacija

### Autentifikacija

**POST** `/api/auth/login`
```json
{
  "username": "admin",
  "password": "password"
}
```

Response:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

### Produktai

- `GET /api/products` - Gauti visus produktus
- `GET /api/products/{id}` - Gauti produktą pagal ID
- `GET /api/products/search?q={query}` - Ieškoti produktų
- `POST /api/products` - Sukurti naują produktą
- `PUT /api/products/{id}` - Atnaujinti produktą

### Užsakymai

- `GET /api/orders` - Gauti visus užsakymus
- `GET /api/orders/{id}` - Gauti užsakymą pagal ID
- `POST /api/orders` - Sukurti naują užsakymą
- `POST /api/orders/{id}/confirm` - Patvirtinti užsakymą
- `POST /api/orders/{id}/cancel` - Atšaukti užsakymą
- `POST /api/orders/{id}/complete` - Užbaigti užsakymą

### Klientai

- `GET /api/customers` - Gauti visus klientus
- `GET /api/customers/search?q={query}` - Ieškoti klientų
- `POST /api/customers` - Sukurti naują klientą

## 🏗 Architektūra

### Sistemos Architektūra

```
┌─────────────┐
│  Frontend   │ (React + TypeScript)
│  (Nginx)    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Backend   │ (Spring Boot)
│   (API)     │
└──────┬──────┘
       │
       ├──────────┬──────────┬──────────┐
       ▼          ▼          ▼          ▼
   ┌────────┐ ┌──────┐ ┌────────────┐
   │Postgres│ │Redis │ │Elasticsearch│
   └────────┘ └──────┘ └────────────┘
```

### Duomenų Modelis

**Core Entities:**
- Products (Produktai)
- ProductStock (Atsargos)
- Customers (Klientai)
- Orders & OrderLines (Užsakymai)
- Invoices & Payments (Sąskaitos ir Mokėjimai)
- Warehouses & Locations (Sandėliai ir Vietos)
- Users, Roles, Permissions (Vartotojai ir Teisės)

### Verslo Logika

#### Kainų Variklis
```java
1. Bazinė kaina (iš produkto)
2. Kainų grupės nuolaida (pagal klientą)
3. Kiekio nuolaida (pagal kiekį)
4. Akcijos (aktyvios promocijos)
= Galutinė kaina
```

#### Atsargų Rezervavimas
```java
1. Patikrinti prieinamumą
2. Rezervuoti atsargas
3. Patvirtinus užsakymą - sumažinti faktines atsargas
4. Atšaukus - atlaisvinti rezervaciją
```

#### Mokėjimų Suderinimas
Automatinis mokėjimų ir sąskaitų suderinimas pagal:
- Suma (50%)
- Sąskaitos numeris nuorodoje (30%)
- Kliento sutapimas (15%)
- PVM kodas nuorodoje (5%)

## 🧪 Testavimas

### Backend Testai
```bash
cd backend
mvn test
```

### Frontend Testai
```bash
cd frontend
npm test
```

### E2E Testai
```bash
npm run test:e2e
```

## 📦 Deployment

### Production Build

#### Backend
```bash
cd backend
mvn clean package -DskipTests
```

Sukuriamas JAR failas: `backend/target/elektromeistras-backend-1.0.0-SNAPSHOT.jar`

#### Frontend
```bash
cd frontend
npm run build
```

Sukuriamas build: `frontend/dist/`

### Docker Production Build
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 🔒 Saugumas

- **JWT** autentifikacija (15 min access, 7d refresh)
- **BCrypt** slaptažodžių hešavimas
- **HTTPS** only production
- **CORS** konfigūracija
- **SQL injection** apsauga (parameterized queries)
- **XSS** apsauga (input sanitization)
- **CSRF** tokens
- **Rate limiting**

## 📝 Licencija

Privatus projektas - Visi teisės saugomos

## 👥 Komanda

- Project Manager/Scrum Master
- Tech Lead
- Senior Backend Developers (Java)
- Frontend Developers (React)
- QA Engineers
- DevOps Engineer
- UI/UX Designer

## 📞 Kontaktai

Dėl klausimų ar problemų, susisiekite su projekto komanda.

---

**Versija:** 1.0.0  
**Paskutinis atnaujinimas:** 2025-10-20
