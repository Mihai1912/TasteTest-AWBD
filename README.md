# TasteTest

## Entity Relationship Model (ERM)

Mai jos este schema bazei de date (ERM/ERD) folosită de aplicație.

![image1](./erm.png)

---

## Cerințe funcționale principale

1. **Autentificare și autorizare**
   - Utilizatorii se pot înregistra, autentifica și administra contul propriu.
   - Există roluri: `ADMIN`, `USER`, `RESTAURANT_OWNER`.
   - Administrarea rolurilor și a drepturilor se asigură la nivel de aplicație.

2. **Gestionează restaurante**
   - Utilizator cu rol `RESTAURANT_OWNER` poate adăuga, edita și sterge restaurantele pe care le deține.
   - Fiecare restaurant are detalii: denumire (unic), adresă, telefon, site, program.

3. **Gestionare categorii de restaurante**
   - Restaurantele pot avea una sau mai multe categorii (ex: românesc, italian, fast food).
   - Categorii pot fi administrate independent.

4. **Meniu și articole de meniu**
   - Fiecare restaurant are unul sau mai multe meniuri (ex: Meniu Mâncare, Meniu Băuturi).
   - Fiecare meniu are articole (ex: Ciorbă de burtă, Sarmale), cu denumire, preț, descriere.

5. **Recenzii și feedback**
   - Utilizatorii pot lăsa recenzii restaurantelor cu notă (1-5 stele) și comentarii.
   - Fiecare recenzie poate primi răspuns din partea unui reprezentant al restaurantului.
   - Utilizatorii pot trimite feedback general privind experiența lor în aplicație (feedback-uri anonime sau autentificate).

6. **Căutare și filtrare**
   - Utilizatorii pot căuta restaurante după nume, categorie, locație etc.
   - Posibilitate filtrare restaurante după rating, specific culinar (categorie) etc.

7. **Administrare**
   - Administratorii au acces la gestionarea utilizatorilor, restaurantelor, categoriilor, feedback-urilor.

---

## Alte detalii

- Structura DB respectă relațiile din poza de mai sus.
- Orice modificare a ERD-ului necesită actualizarea diagrama și a cerințelor de mai sus.

---

## Cerințe pentru rulare locală

- **Java 21** (proiectul folosește features moderne)
- **Maven Wrapper** (`./mvnw`) — inclus în proiect, nu necesită instalare separată
- **Docker + Docker Compose** — pentru a rula PostgreSQL local (profilul `dev`)
- **Node.js 18+** și **npm** — pentru frontend (Angular)

---

## Configurare Multi-Environment (Spring Profiles)

Proiectul este configurat cu **două profile Spring** care separă mediul de dezvoltare de cel de testare:

| Profil | Bază de date | Port | Flyway | DDL | Use case |
|--------|--------------|------|--------|-----|----------|
| `dev`  | PostgreSQL (Docker) | `8090` | activat | `none` (gestionată de Flyway) | dezvoltare zilnică |
| `test` | H2 in-memory | `8091` | dezactivat | `create-drop` (din entități) | teste unitare/integrare, demo rapid |

Fișierele de configurare:

```
src/main/resources/
├── application.yml          # config comuna + spring.profiles.active=dev (default)
├── application-dev.yml      # PostgreSQL + Flyway
└── application-test.yml     # H2 in-memory
```

---

## Pași de utilizare

### 1. Clonarea proiectului

```bash
git clone <repo-url>
cd TasteTest-AWDB
```

### 2. Rulare cu profilul `dev` (PostgreSQL)

**Pas 1 — pornește baza de date PostgreSQL în Docker:**

```bash
docker compose up -d tastetest-app-db
```

Verifică că BD-ul e healthy:

```bash
docker compose ps
```

**Pas 2 — pornește aplicația Spring Boot:**

```bash
./mvnw spring-boot:run
```

Profilul `dev` este activ implicit (definit în `application.yml`). Aplicația pornește pe `http://localhost:8090`.

**Pas 3 — accesează:**

- API: `http://localhost:8090/api/v1/...`
- Swagger UI: `http://localhost:8090/swagger-ui.html`

**Cont admin implicit (creat automat de `ApplicationInitializer`):**

- username: `admin`
- email: `admin@admin.com`
- parola: `admin`

### 3. Rulare cu profilul `test` (H2 in-memory)

Util pentru a porni rapid aplicația **fără Docker, fără PostgreSQL**:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

Aplicația pornește pe `http://localhost:8091`.

**Particularități profil `test`:**

- BD-ul este H2 in-memory, deci dispare la oprire
- Schema e generată automat de Hibernate din entități (`ddl-auto: create-drop`)
- Flyway este dezactivat (migrațiile sunt scrise pentru PostgreSQL)
- Securitatea (JWT) este dezactivată (`vars.security.enable: false`)
- `ApplicationInitializer` nu rulează (e adnotat `@Profile("!test")`)

### 4. Accesare consolă H2 (doar profil `test`)

Cu aplicația pornită pe profilul `test`, deschide în browser:

```
http://localhost:8091/h2-console
```

Completează formularul exact așa:

| Câmp | Valoare |
|------|---------|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:tastetest_test` |
| User Name | `sa` |
| Password | *(gol)* |

Apasă **Connect**. Vei vedea schema `PROJECT` cu toate tabelele generate din entități.

### 5. Rulare teste

Testele JUnit folosesc **automat profilul `test`** (datorită adnotării `@ActiveProfiles("test")` din `TasteTestAwdbApplicationTests`):

```bash
./mvnw test
```

### 6. Rulare cu Docker Compose (toate serviciile)

Pornește backend + frontend + PostgreSQL împreună:

```bash
docker compose up --build
```

- Backend (profil `dev`): `http://localhost:8090`
- Frontend Angular: `http://localhost:4200`
- PostgreSQL: `localhost:5432`

Pentru oprire:

```bash
docker compose down
```

Pentru oprire + ștergere date persistate:

```bash
docker compose down -v
```

### 7. Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

Aplicația pornește pe `http://localhost:4200` și se conectează la backend pe `http://localhost:8090`.

---

## Schimbarea profilului activ

Există mai multe moduri de a alege profilul:

**Linia de comandă (la rulare):**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

**Variabilă de mediu (recomandat în producție / Docker):**

```bash
export SPRING_PROFILES_ACTIVE=test
./mvnw spring-boot:run
```

**JAR construit:**

```bash
./mvnw clean package -DskipTests
java -jar target/TasteTest-AWDB-0.0.1-SNAPSHOT.jar --spring.profiles.active=test
```

**IntelliJ IDEA:**
Run → Edit Configurations → Active profiles: `dev` sau `test`.

---

## Build pentru producție

```bash
./mvnw clean package
```

JAR-ul rezultat: `target/TasteTest-AWDB-0.0.1-SNAPSHOT.jar`.

---

## Troubleshooting

**`Database "/Users/.../test" not found` în consola H2:**
Ai folosit JDBC URL default (`jdbc:h2:~/test`). Schimbă-l cu `jdbc:h2:mem:tastetest_test`.

**`Connection refused` la PostgreSQL pe profilul `dev`:**
BD-ul nu rulează. Pornește-l: `docker compose up -d tastetest-app-db`.

**`Admin role not found in the database` pe profilul `dev`:**
Migrațiile Flyway nu au rulat (probabil prima pornire). Verifică log-urile aplicației și asigură-te că Flyway e enabled în `application-dev.yml`.

**Portul 8090/8091 e deja folosit:**
Identifică procesul: `lsof -i :8090` și oprește-l, sau schimbă portul în yml-ul corespunzător.
