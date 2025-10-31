# Kredito Sistema (Credit Transaction System)

Pilna kreditinė sistema, skirta pakeisti rankinį rašymą į sąsiuvinį. Sistema leidžia įmonėms imti prekes į skolą ir mėnesio gale gauti detalius išrašus.

## 🎯 Pagrindinės funkcijos

### 1. **Greitas paėmimas/grąžinimas (Quick Pickup/Return)**
- ⚡ Ultra greitas įvedimas naudojant kliento ir prekių kodus
- 🔄 Automatinis balansų atnaujinimas
- 📝 Kiekviena operacija įrašoma su data, laiku ir atlikusio asmens vardu
- ✅ Patvirtinimo sistema su parašų funkcionalumu

### 2. **Klientų valdymas**
- 💳 Kredito limitų nustatymas
- 📊 Dabartinės skolos sekimas
- ⚠️ Automatinis įspėjimas viršijus limitą
- 📅 Mokėjimo terminų valdymas

### 3. **Transakcijų istorija**
- 📋 Visų operacijų istorija
- 🔍 Greita paieška ir filtravimas
- 📊 Statistika ir suvestinės
- 🏷️ Statusų valdymas (Laukiama, Patvirtinta, Į sąskaitą, Atšaukta)

### 4. **Mėnesio išrašai**
- 📄 Detalūs mėnesio išrašai su visomis operacijomis
- 📊 Automatinis paėmimų ir grąžinimų skaičiavimas
- 🖨️ Spausdinimo funkcija
- 📥 PDF eksportavimas (planuojama)

### 5. **Rolės ir teisės**
- 👤 **Klientas (savitarna)** - gali savarankiškai pasiimti prekes
- 👨‍💼 **Darbuotojas** - valdo transakcijas, grąžinimus, paėmimus
- 👨‍💻 **Administratorius** - pilnas valdymas

## 🏗️ Architektūra

### Backend (Spring Boot)

```
backend/
├── domain/
│   ├── CreditTransaction.java          # Pagrindinė transakcijos entity
│   └── CreditTransactionLine.java      # Transakcijos eilutės
├── repository/
│   ├── CreditTransactionRepository.java
│   └── CreditTransactionLineRepository.java
├── service/
│   └── CreditTransactionService.java   # Verslo logika
├── controller/
│   └── CreditTransactionController.java # REST API
└── dto/
    ├── request/
    │   ├── QuickCreditPickupRequest.java
    │   ├── CreateCreditTransactionRequest.java
    │   └── ConfirmCreditTransactionRequest.java
    └── response/
        ├── CreditTransactionResponse.java
        └── CreditTransactionSummaryResponse.java
```

### Frontend (React + TypeScript)

```
frontend/src/
├── components/
│   ├── QuickCreditPickupDialog.tsx     # Greitas įvedimo langas
│   └── MonthlyStatementDialog.tsx      # Mėnesio išrašų generavimas
├── pages/
│   └── CreditTransactionsPage.tsx      # Pagrindinis puslapis
└── services/
    └── creditTransactionService.ts     # API calls
```

### Duomenų bazė

```sql
-- Pagrindinė transakcijų lentelė
CREATE TABLE credit_transactions (
    id UUID PRIMARY KEY,
    transaction_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,  -- PICKUP or RETURN
    status VARCHAR(20) NOT NULL,            -- PENDING, CONFIRMED, INVOICED, CANCELLED
    total_amount DECIMAL(19,2) NOT NULL,
    total_items INT NOT NULL,
    performed_by VARCHAR(200) NOT NULL,
    performed_by_role VARCHAR(20) NOT NULL,
    signature_data TEXT,
    confirmed_at TIMESTAMP,
    confirmed_by VARCHAR(200),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Transakcijų eilutės
CREATE TABLE credit_transaction_lines (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(500) NOT NULL,
    quantity DECIMAL(19,3) NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    line_total DECIMAL(19,2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL
);
```

## 🚀 API Endpoints

### Greitas paėmimas
```http
POST /api/credit-transactions/quick-pickup
Content-Type: application/json

{
  "customerCode": "B001",
  "items": [
    { "productCode": "CAB-001", "quantity": 50 },
    { "productCode": "SW-001", "quantity": 5 }
  ],
  "performedBy": "Jonas Jonaitis",
  "performedByRole": "EMPLOYEE",
  "notes": "Skubūs darbai"
}
```

### Patvirtinti transakciją
```http
POST /api/credit-transactions/{id}/confirm
Content-Type: application/json

{
  "confirmedBy": "Petras Petraitis",
  "signatureData": "base64_encoded_signature",
  "notes": "Patvirtinta"
}
```

### Gauti mėnesio išrašą
```http
GET /api/credit-transactions/customer/{customerId}/statement/{year}/{month}
```

### Ieškoti transakcijų
```http
GET /api/credit-transactions/search?q=elektros&page=0&size=20
```

### Gauti kliento transakcijas
```http
GET /api/credit-transactions/customer/{customerId}?page=0&size=20
```

## 📊 Duomenų srautai

### 1. Paėmimo procesas

```
1. Darbuotojas atidaro "Greitas paėmimas" langą
2. Pasirenka klientą (autocomplete)
3. Suveda prekių kodus ir kiekius
4. Sistema rodo:
   - Dabartinę kliento skolą
   - Naują skolą po operacijos
   - Įspėjimą jei viršijamas kredito limitas
5. Išsaugo transakciją (statusas: PENDING)
6. Tvirtina transakciją
7. Sistema atnaujina kliento balansą
```

### 2. Mėnesio išrašo generavimas

```
1. Pasirenkamas klientas
2. Pasirenkamas mėnuo ir metai
3. Sistema suranda visas CONFIRMED ir INVOICED transakcijas
4. Skaičiuoja:
   - Bendri paėmimai
   - Bendri grąžinimai
   - Grynoji suma
5. Rodo detalų sąrašą su data, laiku, atlikusiu
6. Galimybė spausdinti ar eksportuoti į PDF
```

## 🎨 UI Komponentai

### QuickCreditPickupDialog
Ultra greitas kredito paėmimo/grąžinimo langas:
- Klientų autocomplete su kredito informacija
- Sekvencinė prekių įvestis (kodas → kiekis → Enter)
- Realiu laiku skaičiuojama suma
- Vizualūs įspėjimai apie kredito limitą
- Klaviatūros spartieji klavišai (Ctrl+Enter išsaugoti)

### CreditTransactionsPage
Pagrindinis transakcijų valdymo puslapis:
- Transakcijų lentelė su filtrais
- Paieška pagal numerį, klientą
- Statistinės kortelės (pending, confirmed, totals)
- Veiksmai: peržiūra, patvirtinimas, atšaukimas

### MonthlyStatementDialog
Mėnesio išrašų generavimo langas:
- Kliento pasirinkimas
- Mėnesio ir metų pasirinkimas
- Detalus transakcijų sąrašas
- Suvestinė (paėmimai, grąžinimai, grynoji suma)
- Spausdinimo ir PDF funkcijos

## 🔐 Saugumas

### Autentifikacija ir autorizacija
```java
@PreAuthorize("hasAnyAuthority('CREDIT_MANAGE', 'SALES_MANAGE', 'ADMIN_FULL')")
public ResponseEntity<CreditTransactionResponse> createQuickPickup(...)

@PreAuthorize("hasAnyAuthority('CREDIT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
public ResponseEntity<CreditTransactionResponse> getTransactionById(...)
```

### Rolės:
- `CREDIT_VIEW` - Peržiūra
- `CREDIT_MANAGE` - Valdymas (kūrimas, patvirtinimas)
- `SALES_VIEW` - Pardavimų peržiūra
- `SALES_MANAGE` - Pardavimų valdymas
- `ADMIN_FULL` - Pilnas valdymas

## 📈 Optimizavimas

### Indeksai greičiui
```sql
CREATE INDEX idx_credit_customer_id ON credit_transactions(customer_id);
CREATE INDEX idx_credit_created_at ON credit_transactions(created_at);
CREATE INDEX idx_credit_customer_created ON credit_transactions(customer_id, created_at);
CREATE INDEX idx_credit_customer_status ON credit_transactions(customer_id, status);
```

### Greita paieška
- Kliento kodas indeksuotas
- Prekių kodai indeksuoti
- Transakcijų numeriai unikalūs ir indeksuoti

## 🧪 Testavimas

### Backend testai
```bash
cd backend
mvn test
```

### Frontend testai
```bash
cd frontend
npm test
```

### Rankinis testavimas
1. Sukurti paėmimą
2. Patvirtinti paėmimą
3. Patikrinti kliento balansą
4. Sukurti grąžinimą
5. Generuoti mėnesio išrašą

## 📝 Naudojimo pavyzdžiai

### 1. Kasdieninis paėmimas
```
Darbuotojas:
1. Spaudžia "Greitas paėmimas"
2. Įveda kliento kodą: B001
3. Įveda prekių kodus ir kiekius:
   - CAB-001: 100m
   - SW-001: 10vnt
4. Spaudžia "Išsaugoti"
5. Patvirtina operaciją
```

### 2. Mėnesio pabaiga
```
Buhalterė:
1. Atidaro "Mėnesio išrašai"
2. Pasirenka klientą: B001
3. Pasirenka mėnesį: 2025-10
4. Generuoja išrašą
5. Spausdina arba siunčia PDF klientui
```

### 3. Klientų savitarna
```
Klientas (prisijungęs):
1. Atidaro "Mano paėmimai"
2. Pasirenka prekes
3. Patvirtina
4. Sistema automatiškai užfiksuoja
```

## 🔄 Būsimi patobulinimai

- [ ] PDF generavimas su logotipu
- [ ] El. pašto siuntimas su išrašais
- [ ] SMS pranešimai apie viršytą limitą
- [ ] Mobilios aplikacijos (iOS/Android)
- [ ] Integracijos su apskaitos sistemomis
- [ ] QR kodų skanavimas prekėms
- [ ] Parašų planšetės palaikymas
- [ ] Automatinis sąskaitų faktūrų generavimas

## 📞 Palaikymas

Klausimai ar problemos? Susisiekite:
- Email: support@elektromeistras.lt
- Tel: +370 600 00000

## 📄 Licencija

Proprietary - Elektromeistras Ltd. © 2025
