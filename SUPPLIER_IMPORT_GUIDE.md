# Tiekėjo Atsargų Importavimas iš CSV

## Apžvalga

Ši funkcija leidžia importuoti didelius atsargų duomenų kiekius iš FORMAPAK buhalterinės programos CSV formato. Sistema palaiko batch apdorojimą ir gali efektyviai apdoroti 100,000+ prekių įrašų.

## Funkcionalumas

- ✅ Batch importavimas dideliems failams (500 įrašų batches)
- ✅ Automatinis tiekėjų kūrimas
- ✅ Automatinis kategorijų kūrimas
- ✅ Prekių kūrimas arba atnaujinimas
- ✅ Atsargų kiekių atnaujinimas
- ✅ Detalus importo rezultatų ataskaita
- ✅ Klaidų valdymas ir validacija
- ✅ UTF-8 palaikymas (lietuviški simboliai)

## CSV Formato Reikalavimai

### Privalomi stulpeliai:

- **Pogrupio kodas** - Prekės kodas (unikalus identifikatorius)
- **Pogrupio pavadinimas** - Prekės pavadinimas
- **Kiekis** - Prekės kiekis (turi būti > 0)

### Papildomi stulpeliai:

- **Grupės kodas** - Kategorijos kodas
- **Grupės pavadinimas** - Kategorijos pavadinimas
- **Mat.vnt** - Matavimo vienetas (vnt, m, kg, l)
- **Brūkšninis kodas** - EAN/barcode
- **PVM %** - PVM procentas
- **Pajamavimo kaina EUR** - Įsigijimo kaina
- **Didmeninė kaina** - Didmeninė pardavimo kaina
- **Mažmeninė kaina** - Mažmeninė pardavimo kaina
- **Pajamavimo data** - Data formatu YYYY.MM.DD (pvz., 2018.12.20)
- **Tiekėjo kodas** - Tiekėjo kodas
- **Tiekėjo pavadinimas** - Tiekėjo pavadinimas

### Pavyzdinis CSV failas:

Žiūrėkite `supplier-inventory-example.csv`

## API Naudojimas

### Endpoint:

```
POST /api/import/supplier-inventory
```

### Autorizacija:

Reikalauja autentifikacijos ir vieno iš šių leidimų:
- `PRODUCT_MANAGE`
- `ADMIN_FULL`

### Parametrai:

| Parametras | Tipas | Privalomas | Default | Aprašymas |
|------------|-------|------------|---------|-----------|
| file | MultipartFile | Taip | - | CSV failas |
| warehouseCode | String | Ne | "MAIN" | Sandėlio kodas |
| updateExisting | Boolean | Ne | false | Ar atnaujinti esamas prekes |

### cURL Pavyzdys:

```bash
# Importuoti naujus produktus (neatnaujinant esamų)
curl -X POST \
  'http://localhost:8080/api/import/supplier-inventory?warehouseCode=MAIN&updateExisting=false' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/atsargos.csv'

# Importuoti ir atnaujinti esamus produktus
curl -X POST \
  'http://localhost:8080/api/import/supplier-inventory?warehouseCode=MAIN&updateExisting=true' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/atsargos.csv'
```

### JavaScript/Fetch Pavyzdys:

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

const response = await fetch(
  '/api/import/supplier-inventory?warehouseCode=MAIN&updateExisting=false',
  {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    },
    body: formData
  }
);

const result = await response.json();
console.log('Import result:', result);
```

## Response Formatas

### Sėkmingas importas (200 OK):

```json
{
  "startTime": "2025-10-31T10:00:00",
  "endTime": "2025-10-31T10:05:23",
  "durationMs": 323000,
  "totalRows": 100000,
  "processedRows": 99850,
  "createdProducts": 15420,
  "updatedProducts": 0,
  "createdStock": 15420,
  "updatedStock": 84430,
  "skippedRows": 50,
  "errorRows": 100,
  "errors": [
    "Row 1245: Invalid data - missing required fields",
    "Row 3421: Product code cannot be empty"
  ],
  "warnings": [
    "Row 567: No supplier code provided, using UNKNOWN"
  ],
  "status": "SUCCESS"
}
```

### Dalinis importas (206 Partial Content):

Kai yra klaidų, bet dalis duomenų importuota sėkmingai:

```json
{
  "status": "PARTIAL",
  "processedRows": 5000,
  "errorRows": 50,
  "errors": ["..."],
  ...
}
```

### Klaida (500 Internal Server Error):

```json
{
  "status": "FAILED",
  "errors": [
    "Fatal error: Invalid CSV format"
  ]
}
```

## Duomenų Apdorojimo Logika

### 1. Tiekėjai (Suppliers)

- Jei tiekėjas su kodu egzistuoja - naudojamas esamas
- Jei ne - sukuriamas naujas automatiškai
- Jei tiekėjo kodas tuščias - naudojamas "UNKNOWN"

### 2. Kategorijos (Categories)

- Jei kategorija su kodu egzistuoja - naudojama esama
- Jei ne - sukuriama nauja automatiškai
- Jei kategorijos kodas tuščias - naudojama "UNCATEGORIZED"

### 3. Prekės (Products)

#### Naujos prekės kūrimas:
- SKU generuojamas automatiškai iš kodo (jei nėra)
- Nustatomas bazinis kainodara (mažmeninė kaina)
- Nustatoma savikaina (įsigijimo kaina)
- PVM nustatomas iš CSV arba default 21%

#### Esamų prekių atnaujinimas (kai updateExisting=true):
- Atnaujinamas pavadinimas
- Atnaujinama kategorija
- Atnaujinamos kainos
- Atnaujinamas aprašymas

### 4. Atsargos (Stock)

- Jei prekės atsargos sandėlyje egzistuoja - **PRIDEDAMAS** kiekis prie esamo
- Jei ne - sukuriamos naujos atsargos
- Atnaujinama paskutinio inventorizavimo data ir kiekis

**SVARBU:** Sistema **PRIDEDA** kiekius, o ne perrašo juos. Tai leidžia importuoti daugkartines tiekimo partijas.

## Matavimo Vienetų Konvertavimas

Sistema automatiškai konvertuoja lietuviškus matavimo vienetus:

| CSV formatas | Sistemos formatas |
|--------------|-------------------|
| vnt, vnt. | PCS |
| m, m. | M |
| kg, kg. | KG |
| l, l. | L |
| (kiti) | Didžiosios raidės |

## Performance

### Batch Apdorojimas:
- 500 įrašų per batch operaciją
- Optimizuotas DB rašymas
- Cache mechanizmas tiekėjams ir kategorijoms

### Tikėtinas greitis:
- ~100,000 įrašų per ~5 minutes
- Priklauso nuo serverio resursų ir DB našumo

## Klaidos ir Warnings

### Įprastos klaidos:

1. **"Invalid data - missing required fields"**
   - Trūksta privalomų laukų (prekės kodas, pavadinimas, kiekis)

2. **"Product code cannot be empty"**
   - Tuščias prekės kodas

3. **"Warehouse not found: XXX"**
   - Nurodytas neegzistuojantis sandėlio kodas

### Warnings:

- Praleistos eilutės su nevalidiais duomenimis
- Automatiškai sukurti tiekėjai/kategorijos

## Testavimas

### 1. Sukurti testinį failą:

Panaudokite `supplier-inventory-example.csv` kaip šabloną.

### 2. Patikrinti endpoint'ą:

```bash
# Testuoti su mažu failu
curl -X POST \
  'http://localhost:8080/api/import/supplier-inventory?warehouseCode=MAIN' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -F 'file=@supplier-inventory-example.csv'
```

### 3. Patikrinti rezultatus:

- Peržiūrėti sukurtas prekes per `/api/products`
- Patikrinti atsargas per stock API
- Peržiūrėti DB tiesiogiai

## Troubleshooting

### "File must be a CSV file"
- Failas turi turėti `.csv` plėtinį
- Patikrinkite failo pavadinimą

### "Warehouse not found"
- Sukurkite sandėlį su nurodytu kodu arba naudokite esamą
- Default sandėlis: "MAIN"

### UTF-8 problemos
- Patikrinkite, kad CSV failas būtų UTF-8 koduotės
- FORMAPAK paprastai eksportuoja UTF-8

### Per lėtas importas
- Padidinkite batch dydį `SupplierInventoryImportService.BATCH_SIZE`
- Optimizuokite DB connection pool
- Patikrinkite DB indeksus

## Saugumas

- Endpoint'as apsaugotas autentifikacija
- Reikia `PRODUCT_MANAGE` arba `ADMIN_FULL` leidimo
- Validuojami visi įvesties duomenys
- SQL injection apsauga per JPA
- File upload limitai (Spring Boot default: 1MB-10MB, konfigūruojama)

## Konfigūracija

### application.properties:

```properties
# Max file size for upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Batch size (optional, default 500)
# app.import.batch-size=500
```

## Pagalba ir Palaikymas

Jei kiltų klausimų ar problemų:
1. Patikrinkite server logs (`backend/logs/`)
2. Peržiūrėkite importo response su klaidų pranešimais
3. Validuokite CSV failą pagal formatą
4. Patikrinkite autorizaciją ir leidimus
