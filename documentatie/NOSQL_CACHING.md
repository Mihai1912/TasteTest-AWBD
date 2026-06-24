# NoSQL și Caching

Acest document acoperă Partea a II-a §9 din cerințele proiectului: integrarea a cel puțin unei baze de date NoSQL și a unui strat de caching pentru datele accesate frecvent, precum și demonstrarea beneficiului de performanță.

## Arhitectura pe scurt

```
                         ┌──────────────────┐
        citiri cache     │  restaurant-      │      ┌────────┐
   ┌─────────────────────┤  service          ├─────►│ Redis  │  (cache: allRestaurants,
   │                      │  (port 8093)      │      │ :6379  │   topRated, ratings)
   │                      └─────────┬─────────┘      └────────┘
   │                                │
   │                                ▼
   │                          PostgreSQL (schema project)
   │
   │                      ┌──────────────────┐
   │   jurnal notificări  │  notification-    │      ┌────────┐
   └─────────────────────►│  service          ├─────►│ MongoDB│  (colecție: notifications)
                           │  (port 8082)      │      │ :27017 │
                           └──────────────────┘      └────────┘
```

Două tehnologii, două cerințe distincte:

- **Redis** — strat de caching pentru `restaurant-service` (în memorie, bazat pe TTL).
- **MongoDB** — baza de date NoSQL a proiectului, folosită de `notification-service` pentru a persista un istoric al notificărilor.

## Strat de caching (Redis) — restaurant-service

### Ce este pus în cache și de ce

[`RestaurantService`](../restaurant-service/src/main/java/com/example/restaurant/service/RestaurantService.java) are trei căi de citire care sunt disproporționat de costisitoare în raport cu cât de des se modifică datele de bază:

| Metodă | Endpoint | Cost fără cache | Cache | TTL |
|---|---|---|---|---|
| `getAllRestaurants()` | `GET /api/v1/restaurant/getAll` | `SELECT *` peste toate restaurantele, mapat la fiecare apel | `allRestaurants` | 60s |
| `getTopRatedRestaurants()` | `GET /api/v1/restaurant/top-rated` | încarcă **toate** restaurantele, apoi pentru **fiecare** rulează o interogare separată pentru recenziile sale ca să calculeze o medie — clasicul N+1 | `topRated` | 30s |
| `getRatings(id)` | `GET /api/v1/restaurant/getRatings/{id}` | încarcă toate recenziile unui restaurant și le mediază la fiecare apel | `ratings` (cheia după id-ul restaurantului) | 30s |

Configurația se află în [`restaurant-service/.../config/CacheConfig.java`](../restaurant-service/src/main/java/com/example/restaurant/config/CacheConfig.java): `@EnableCaching` + un `RedisCacheManagerBuilderCustomizer` care dă fiecăruia dintre cele trei cache-uri propriul TTL, serializat ca JSON prin `GenericJackson2JsonRedisSerializer`.

### De ce expirare bazată pe TTL în loc de invalidare explicită

`getRatings`/`getTopRatedRestaurants` depind de **recenzii**, care sunt scrise din `user-service` — un JVM diferit, cu propriul context de cache Spring. Nu există niciun eveniment intern de proces de care să atașezi un `@CacheEvict` atunci când o recenzie este creată acolo (a face acest lucru corect ar necesita un broker de mesaje / un eveniment de invalidare a cache-ului, ceea ce depășește scopul aici).

Pentru operațiunile CRUD pe restaurante — care **sunt** locale pentru `restaurant-service` — se folosește invalidarea explicită:

- `addRestaurant`, `updateRestaurant`, `deleteRestaurant` sunt adnotate `@CacheEvict(value = {"allRestaurants", "topRated"}, allEntries = true)`, astfel încât lista de restaurante și lista celor mai bine cotate sunt imediat consistente după o modificare făcută prin acest serviciu.
- Cache-urile derivate din rating-uri (`topRated`, `ratings`) se bazează pe TTL-ul lor de 30s pentru a prelua modificările recenziilor făcute în altă parte. Acesta este un compromis explicit, documentat, de consistență în timp (eventual): o recenzie nouă poate dura până la 30s pentru a afecta `top-rated` / `getRatings`.

### Cum verifici beneficiul de performanță

```bash
# Primul apel — cache miss, calculează rating-urile pentru fiecare restaurant (interogări N+1)
time curl -s http://localhost:8090/api/v1/restaurant/top-rated -H "Authorization: Bearer $TOKEN" > /dev/null

# Al doilea apel în 30s — cache hit, servit direct din Redis
time curl -s http://localhost:8090/api/v1/restaurant/top-rated -H "Authorization: Bearer $TOKEN" > /dev/null
```

Al doilea apel este vizibil mai rapid (fără drumuri dus-întors la Postgres). Poți inspecta și cache-ul direct:

```bash
redis-cli -h localhost KEYS '*'
# topRated::SimpleKey []
# allRestaurants::SimpleKey []
# ratings::<restaurant-uuid>
```

## Depozit NoSQL (MongoDB) — notification-service

[`notification-service`](../notification-service/src/main/java/com/example/tastetestawdb/notificationservice) era anterior **fără stare** (stateless): `POST /api/notifications` calcula un răspuns și îl returna fără a persista nimic. Acum persistă fiecare notificare ca document în MongoDB.

- [`model/NotificationLog.java`](../notification-service/src/main/java/com/example/tastetestawdb/notificationservice/model/NotificationLog.java) — document stocat în colecția `notifications` (titlu, mesaj, serviciu sursă, status, instanță, timestamp).
- [`repository/NotificationLogRepository.java`](../notification-service/src/main/java/com/example/tastetestawdb/notificationservice/repository/NotificationLogRepository.java) — `MongoRepository`, plus `findTop20ByOrderByProcessedAtDesc()`.
- [`api/NotificationController.java`](../notification-service/src/main/java/com/example/tastetestawdb/notificationservice/api/NotificationController.java):
  - `POST /api/notifications` — salvează un `NotificationLog` pe lângă returnarea răspunsului existent.
  - `GET /api/notifications/history` — returnează ultimele 20 de notificări.

### De ce MongoDB și de ce aici

Notificările sunt înregistrări de evenimente de tip append-only, cu schemă lejeră — o potrivire naturală pentru un depozit de documente, mai degrabă decât încă un tabel în schema Postgres partajată. Este o bază de date cu adevărat separată (container propriu, volum propriu), demonstrând o configurație de persistență poliglotă în loc de reutilizarea BD relaționale existente.

### Cum verifici

```bash
TOKEN=$(curl -s -X POST http://localhost/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@taste.test","password":"password"}' | jq -r .token)

curl -s -X POST http://localhost:8090/api/internal/notifications \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Test","message":"Hello","sourceService":"manual-test"}'

curl -s http://localhost:8090/api/internal/notifications/history \
  -H "Authorization: Bearer $TOKEN" | jq .

# Sau interoghează Mongo direct
mongosh mongodb://localhost:27017/tastetest_notifications --eval "db.notifications.find().pretty()"
```

## Fișiere adăugate / modificate

| Fișier | Modificare |
|---|---|
| `restaurant-service/pom.xml` | **modificat** — adăugate `spring-boot-starter-data-redis`, `spring-boot-starter-cache` |
| `restaurant-service/.../config/CacheConfig.java` | **nou** — `@EnableCaching` + configurare TTL per cache |
| `restaurant-service/.../service/RestaurantService.java` | **modificat** — `@Cacheable`/`@CacheEvict` pe căile de citire/scriere intens utilizate |
| `config-repo/tastetest-restaurant.yml`, `tastetest-restaurant-dev.yml` | **modificat** — `spring.data.redis.host`/`port` |
| `notification-service/pom.xml` | **modificat** — adăugat `spring-boot-starter-data-mongodb` |
| `notification-service/.../model/NotificationLog.java` | **nou** — document Mongo |
| `notification-service/.../repository/NotificationLogRepository.java` | **nou** — `MongoRepository` |
| `notification-service/.../api/NotificationController.java` | **modificat** — persistă notificările, adaugă endpoint-ul `/history` |
| `notification-service/src/main/resources/application.yml` | **modificat** — `spring.data.mongodb.uri` |
| `docker-compose.yml` | **modificat** — servicii noi `redis` și `mongo`, conectate ca dependențe ale `restaurants` / `notification-service`, volum nou `tastetest-mongo-vol` |
