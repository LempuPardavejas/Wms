# Prisidėjimo Gairės

Dėkojame už susidomėjimą prisidėti prie ELEKTRO MEISTRAS projekto!

## Kaip Prisidėti

### 1. Fork Repozitoriją

Sukurkite savo fork repozitorijos GitHub platformoje.

### 2. Klonuokite Fork

```bash
git clone https://github.com/your-username/elektromeistras.git
cd elektromeistras
```

### 3. Sukurkite Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 4. Atlikite Pakeitimus

- Laikykitės kodo stiliaus gairių
- Pridėkite testus savo pakeitimams
- Atnaujinkite dokumentaciją jei reikia

### 5. Commit Pakeitimai

```bash
git add .
git commit -m "feat: add new feature"
```

#### Commit Message Formatas

Naudokite Conventional Commits formatą:

- `feat:` - Nauja funkcija
- `fix:` - Klaidos pataisymas
- `docs:` - Dokumentacijos pakeitimai
- `style:` - Kodo stiliaus pakeitimai
- `refactor:` - Kodo refaktoringas
- `test:` - Testų pridėjimas/keitimas
- `chore:` - Build/tooling pakeitimai

### 6. Push į Fork

```bash
git push origin feature/your-feature-name
```

### 7. Sukurkite Pull Request

Eikite į GitHub ir sukurkite Pull Request iš savo fork į pagrindinę repozitoriją.

## Kodo Stiliaus Gairės

### Java (Backend)

- Naudokite Java 17 features
- Laikykitės Google Java Style Guide
- Visos public klasės ir metodai turi turėti Javadoc
- Naudokite Lombok anotacijas švaresniam kodui

### TypeScript (Frontend)

- Naudokite TypeScript strict mode
- Funkcionalūs komponentai su hooks
- Prop types turi būti aiškiai apibrėžti
- Naudokite ESLint ir Prettier

## Testavimas

### Backend Testai

```bash
cd backend
mvn test
```

Minimalus test coverage: 80%

### Frontend Testai

```bash
cd frontend
npm test
```

## Pull Request Procesas

1. Įsitikinkite, kad visi testai praeina
2. Atnaujinkite README.md jei reikia
3. Pull Request aprašyme aiškiai apibūdinkite pakeitimus
4. Susieti su Issues jei taikoma
5. Palaukite code review

## Code Review Kriterijai

- Kodas kompiliuojasi be klaidų
- Visi testai praeina
- Pakeitimai atitinka projektų architektūrą
- Dokumentacija atnaujinta
- Nėra saugumo pažeidimų

## Klausimai?

Jei turite klausimų, sukurkite Issue arba susisiekite su komanda.

Dėkojame už jūsų indėlį! 🎉
