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
- **Node.js 20.19+** și **npm** — pentru frontend (Angular); scripturile din `frontend/package.json` pornesc automat Angular CLI prin `npx` dacă ai o versiune mai veche instalată local

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

## Configurare centralizată cu Config Server

Pentru scenariul opțional de microservicii, backend-ul citește acum configurația dintr-un **Spring Cloud Config Server** local, separat de aplicația principală.

Structura folosită în repo:

```
config-server/   # serviciul Spring Cloud Config Server
config-repo/     # fișierele de configurare centralizată
```

Ce este externalizat:

- `token.secret` și `token.ttl`
- credențialele inițiale de admin
- setările `spring.datasource` și `spring.flyway` pentru profilul `dev`
- credentialele de mail

Refresh dinamic:

- după modificarea unui fișier din `config-repo/`, poți forța reîncărcarea la runtime cu `POST /actuator/refresh`
- beans care consumă aceste valori sunt marcate cu `@RefreshScope`

Config Server rulează pe `http://localhost:8888` și este pornit automat prin `docker compose up --build`.

---

## Arhitectura microserviciilor

Aplicația a fost desfăcută din monolit într-un set de microservicii independente, înregistrate în Eureka și accesate prin API Gateway. Fiecare microserviciu are propriul `pom.xml`, propriul `Dockerfile` și propriul ciclu de viață.

| Serviciu | Director | Eureka name | Port (intern) | Port (host) | Rol |
|---|---|---|---|---|---|
| Discovery Server | `discovery-server/` | `tastetest-discovery-server` | 8761 | 8761 | registry Eureka |
| Config Server | `config-server/` | `tastetest-config-server` | 8888 | 8888 | configurare centralizată |
| API Gateway | `api-gateway/` | `tastetest-api-gateway` | 80 | 8090 | Spring Cloud Gateway, intrare publică |
| Auth Service | `auth-service/` | `tastetest-auth` | 8091 | 8091 | autentificare + emitere JWT |
| User Service | `user-service/` | `tastetest-user` | 8092 | 8092 | conturi, roluri, recenzii |
| Restaurant Service | `restaurant-service/` | `tastetest-restaurant` | 8093 | 8093 | restaurante, meniuri, rating |
| Notification Service | `notification-service/` | `tastetest-notification-service` | 8082 | 8082 | notificări (Saga step 2, vezi [DESIGN_PATTERNS.md](DESIGN_PATTERNS.md)) |
| Agent Service | `agent-service/` | `tastetest-agent` | 8083 | 8083 | AI assistant Claude (Part II §12, vezi [AI_AGENTS.md](AI_AGENTS.md)) |
| Backend original | `backend-original/` | `tastetest-awdb` | 8090 | — (intern) | monolit legacy păstrat pentru migrare graduală |
| Frontend (Angular shell) | `frontend/` | — | 80 | 4200 | UI principal |
| MFE Admin | `mfe-admin/` | — | 80 | 4201 | micro-frontend admin (Part II §10), embedat în shell prin iframe |
| PostgreSQL | — (image) | — | 5432 | 5432 | bază de date partajată |
| Redis | — (image) | — | 6379 | 6379 | caching layer pentru Restaurant Service (Part II §9, vezi [NOSQL_CACHING.md](NOSQL_CACHING.md)) |
| MongoDB | — (image) | — | 27017 | 27017 | bază NoSQL — istoric notificări (Part II §9, vezi [NOSQL_CACHING.md](NOSQL_CACHING.md)) |
| Prometheus | — (image) | — | 9090 | 9090 | scrape `/actuator/prometheus` |
| Grafana | — (image) | — | 3000 | 3000 | dashboard observability |
| Zipkin | — (image) | — | 9411 | 9411 | tracing distribuit |

Comunicarea inter-servicii folosește **Spring Cloud LoadBalancer** peste Eureka — apelurile sunt scrise ca `http://<eureka-name>/...` și sunt rezolvate la runtime la o instanță concretă (vezi `RestClientConfig` în `restaurant-service` și `agent-service`).

Toate apelurile externe trec prin **API Gateway** (`http://localhost:8090`), care le rutează către microserviciul potrivit pe baza căii — vezi `api-gateway/src/main/resources/application.yml` pentru rute. Tokenul JWT emis de `auth-service` este acceptat de toate celelalte servicii (HS512, secret partajat prin `vars.security.secret`).

Endpoint demo Saga (Part II §8):

```http
POST /api/v1/integrations/notifications/demo
```

Exemplu body:

```json
{
  "title": "Test",
  "message": "Salut din backend"
}
```

Răspunsul vine de la Notification Service prin Eureka + Spring Cloud LoadBalancer.

Răspunsul include și `instanceId`, ca să poți vedea clar ce instanță a procesat cererea atunci când rulezi mai multe copii ale serviciului.

---

## NoSQL și Caching (Part II §9)

Documentație completă: [NOSQL_CACHING.md](NOSQL_CACHING.md).

- **Redis (caching layer)** — `restaurant-service` cache-uiește `getAllRestaurants()`, `getTopRatedRestaurants()` (anterior N+1 — încărca toate restaurantele și, pentru fiecare, toate recenziile, ca să calculeze media) și `getRatings(id)`, cu TTL diferențiat (60s / 30s / 30s) configurat în `restaurant-service/.../config/CacheConfig.java`. Mutațiile locale (`addRestaurant`, `updateRestaurant`, `deleteRestaurant`) fac `@CacheEvict` imediat; datele derivate din recenzii (scrise din `user-service`) se bazează pe TTL pentru consistență eventuală, pentru că nu există un mecanism cross-service de invalidare.
- **MongoDB (bază NoSQL)** — `notification-service` era complet stateless (răspundea la `POST /api/notifications` fără să persiste nimic). Acum salvează fiecare notificare ca document în colecția `notifications` și expune `GET /api/notifications/history` (ultimele 20).

Ambele rulează ca containere separate (`redis`, `mongo`) în `docker-compose.yml`, cu propriile healthcheck-uri și (pentru Mongo) volum persistent `tastetest-mongo-vol`.

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

Pornește Discovery Server + Config Server + backend + Notification Service + frontend + PostgreSQL împreună, plus stack-ul de monitorizare (Prometheus, Grafana și Zipkin):

```bash
docker compose up --build
```

- API Gateway (intrare publică): `http://localhost:8090`
- Frontend Angular (shell): `http://localhost:4200`
- MFE Admin: `http://localhost:4201`
- Discovery Server (Eureka): `http://localhost:8761`
- Config Server: `http://localhost:8888`
- Auth Service: `http://localhost:8091`
- User Service: `http://localhost:8092`
- Restaurant Service: `http://localhost:8093`
- Notification Service: `http://localhost:8082`
- Agent Service (TasteBot): `http://localhost:8083`
- PostgreSQL: `localhost:5432`
- Redis (cache, vezi [NOSQL_CACHING.md](NOSQL_CACHING.md)): `localhost:6379`
- MongoDB (istoric notificări, vezi [NOSQL_CACHING.md](NOSQL_CACHING.md)): `localhost:27017`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin)
- Zipkin: `http://localhost:9411`

Endpoint-uri utile pentru observability:

- health: `/actuator/health`
- metrics: `/actuator/metrics`
- Prometheus scrape: `/actuator/prometheus`
- refresh config: `POST /actuator/refresh`

Grafana vine cu un dashboard preconfigurat pentru CPU, memorie și request rate, alimentat din Prometheus.

Pentru refresh manual al configurației backend-ului:

```bash
curl -X POST http://localhost:8090/actuator/refresh
```

### 7. Demo load balancing și scalare

Pentru a rula două instanțe ale Notification Service fără conflict de porturi, folosește override-ul dedicat:

```bash
docker compose -f docker-compose.yml -f docker-compose.loadbalancing.yml up --build --scale notification-service=2
```

După pornire, trimite de mai multe ori cererea demo către backend:

```bash
curl -X POST http://localhost:8090/api/v1/integrations/notifications/demo \
   -H 'Content-Type: application/json' \
   -d '{"title":"Test","message":"Salut"}'
```

Dacă load balancing-ul funcționează, câmpul `instanceId` din răspuns va alterna între instanțele disponibile.

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
