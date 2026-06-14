# NoSQL & Caching

This document covers Part II §9 of the project requirements: integrate at least one NoSQL database and a caching layer for frequently-accessed data, and demonstrate the performance benefit.

## Architecture at a glance

```
                         ┌──────────────────┐
        cache reads      │  restaurant-      │      ┌────────┐
   ┌─────────────────────┤  service          ├─────►│ Redis  │  (cache: allRestaurants,
   │                      │  (port 8093)      │      │ :6379  │   topRated, ratings)
   │                      └─────────┬─────────┘      └────────┘
   │                                │
   │                                ▼
   │                          PostgreSQL (project schema)
   │
   │                      ┌──────────────────┐
   │   notification log   │  notification-    │      ┌────────┐
   └─────────────────────►│  service          ├─────►│ MongoDB│  (collection: notifications)
                           │  (port 8082)      │      │ :27017 │
                           └──────────────────┘      └────────┘
```

Two technologies, two distinct requirements:

- **Redis** — caching layer for `restaurant-service` (in-memory, TTL-based).
- **MongoDB** — the project's NoSQL database, used by `notification-service` to persist a notification history.

## Caching layer (Redis) — restaurant-service

### What's cached and why

[`RestaurantService`](restaurant-service/src/main/java/com/example/restaurant/service/RestaurantService.java) has three read paths that are disproportionately expensive relative to how often the underlying data changes:

| Method | Endpoint | Cost without cache | Cache | TTL |
|---|---|---|---|---|
| `getAllRestaurants()` | `GET /api/v1/restaurant/getAll` | `SELECT *` over all restaurants, mapped on every call | `allRestaurants` | 60s |
| `getTopRatedRestaurants()` | `GET /api/v1/restaurant/top-rated` | loads **all** restaurants, then for **each** one runs a separate query for its reviews to compute an average — classic N+1 | `topRated` | 30s |
| `getRatings(id)` | `GET /api/v1/restaurant/getRatings/{id}` | loads all reviews for a restaurant and averages them on every call | `ratings` (keyed by restaurant id) | 30s |

Configuration lives in [`restaurant-service/.../config/CacheConfig.java`](restaurant-service/src/main/java/com/example/restaurant/config/CacheConfig.java): `@EnableCaching` + a `RedisCacheManagerBuilderCustomizer` that gives each of the three caches its own TTL, serialized as JSON via `GenericJackson2JsonRedisSerializer`.

### Why TTL-based expiry instead of explicit invalidation

`getRatings`/`getTopRatedRestaurants` depend on **reviews**, which are written from `user-service` — a different JVM with its own Spring cache context. There is no in-process event to hook a `@CacheEvict` onto when a review is created there (doing this properly would require a message broker / cache-invalidation event, which is out of scope here).

For restaurant CRUD — which **is** local to `restaurant-service` — explicit invalidation is used:

- `addRestaurant`, `updateRestaurant`, `deleteRestaurant` are annotated `@CacheEvict(value = {"allRestaurants", "topRated"}, allEntries = true)`, so the restaurant list and the top-rated list are immediately consistent after a mutation made through this service.
- Rating-derived caches (`topRated`, `ratings`) rely on their 30s TTL to pick up review changes made elsewhere. This is an explicit, documented eventual-consistency trade-off: a new review can take up to 30s to affect `top-rated` / `getRatings`.

### How to verify the performance benefit

```bash
# First call — cache miss, computes ratings for every restaurant (N+1 queries)
time curl -s http://localhost:8090/api/v1/restaurant/top-rated -H "Authorization: Bearer $TOKEN" > /dev/null

# Second call within 30s — cache hit, served straight from Redis
time curl -s http://localhost:8090/api/v1/restaurant/top-rated -H "Authorization: Bearer $TOKEN" > /dev/null
```

The second call is noticeably faster (no Postgres round-trips). You can also inspect the cache directly:

```bash
redis-cli -h localhost KEYS '*'
# topRated::SimpleKey []
# allRestaurants::SimpleKey []
# ratings::<restaurant-uuid>
```

## NoSQL store (MongoDB) — notification-service

[`notification-service`](notification-service/src/main/java/com/example/tastetestawdb/notificationservice) was previously **stateless**: `POST /api/notifications` computed a response and returned it without persisting anything. It now persists every notification as a document in MongoDB.

- [`model/NotificationLog.java`](notification-service/src/main/java/com/example/tastetestawdb/notificationservice/model/NotificationLog.java) — document stored in the `notifications` collection (title, message, source service, status, instance, timestamp).
- [`repository/NotificationLogRepository.java`](notification-service/src/main/java/com/example/tastetestawdb/notificationservice/repository/NotificationLogRepository.java) — `MongoRepository`, plus `findTop20ByOrderByProcessedAtDesc()`.
- [`api/NotificationController.java`](notification-service/src/main/java/com/example/tastetestawdb/notificationservice/api/NotificationController.java):
  - `POST /api/notifications` — saves a `NotificationLog` in addition to returning the existing response.
  - `GET /api/notifications/history` — returns the last 20 notifications.

### Why MongoDB and why here

Notifications are append-only, schema-light event records — a natural fit for a document store rather than another table in the shared Postgres schema. It is a genuinely separate database (own container, own volume), demonstrating a polyglot-persistence setup rather than reusing the existing relational DB.

### How to verify

```bash
TOKEN=$(curl -s -X POST http://localhost/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@taste.test","password":"password"}' | jq -r .token)

curl -s -X POST http://localhost:8090/api/internal/notifications \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Test","message":"Hello","sourceService":"manual-test"}'

curl -s http://localhost:8090/api/internal/notifications/history \
  -H "Authorization: Bearer $TOKEN" | jq .

# Or query Mongo directly
mongosh mongodb://localhost:27017/tastetest_notifications --eval "db.notifications.find().pretty()"
```

## Files added / modified

| File | Change |
|---|---|
| `restaurant-service/pom.xml` | **modified** — added `spring-boot-starter-data-redis`, `spring-boot-starter-cache` |
| `restaurant-service/.../config/CacheConfig.java` | **new** — `@EnableCaching` + per-cache TTL configuration |
| `restaurant-service/.../service/RestaurantService.java` | **modified** — `@Cacheable`/`@CacheEvict` on the hot read/write paths |
| `config-repo/tastetest-restaurant.yml`, `tastetest-restaurant-dev.yml` | **modified** — `spring.data.redis.host`/`port` |
| `notification-service/pom.xml` | **modified** — added `spring-boot-starter-data-mongodb` |
| `notification-service/.../model/NotificationLog.java` | **new** — Mongo document |
| `notification-service/.../repository/NotificationLogRepository.java` | **new** — `MongoRepository` |
| `notification-service/.../api/NotificationController.java` | **modified** — persists notifications, adds `/history` endpoint |
| `notification-service/src/main/resources/application.yml` | **modified** — `spring.data.mongodb.uri` |
| `docker-compose.yml` | **modified** — new `redis` and `mongo` services, wired as dependencies of `restaurants` / `notification-service`, new `tastetest-mongo-vol` volume |
