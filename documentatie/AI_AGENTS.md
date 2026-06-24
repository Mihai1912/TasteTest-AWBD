# Agenți AI — Partea a II-a §12

Acest document acoperă bonusul **Agenți AI – Dezvoltare** din grila proiectului. Platforma TasteTest include un asistent AI integrat în aplicație ("TasteBot"), care rulează ca microserviciu dedicat, se integrează în service mesh-ul existent și folosește apeluri de unelte (tool calling) pentru a interoga backend-ul live.

## Arhitectură

```
┌─────────────┐    POST /api/v1/agent/chat    ┌──────────────┐    Claude API    ┌──────────────┐
│  Angular    │ ─────────────────────────────▶│ api-gateway  │ ───────────────▶ │  Anthropic   │
│  /assistant │                               └──────┬───────┘                  └──────────────┘
└─────────────┘                                      │  lb://tastetest-agent
                                                     ▼
                                              ┌──────────────┐
                                              │ agent-service│
                                              │  (port 8083) │
                                              └──────┬───────┘
                                                     │  apeluri unelte (lb://tastetest-restaurant, lb://tastetest-user)
                                                     ▼
                                              ┌──────────────┐
                                              │ servicii     │
                                              │ existente    │
                                              └──────────────┘
```

## Ce face

TasteBot este un asistent de chat alimentat de un LLM, care răspunde la întrebările utilizatorilor despre catalogul TasteTest. În culise, agentul nu vede baza de date — dispune de un set restrâns de **unelte** care încapsulează endpoint-uri REST reale ale serviciilor existente. Când utilizatorul întreabă ceva de genul *"care este restaurantul cu cel mai mare rating?"*, modelul decide să apeleze `getTopRatedRestaurants`, agent-service redirecționează apelul (împreună cu JWT-ul utilizatorului) către `tastetest-restaurant`, iar rezultatul este reintrodus în bucla de raționament a modelului.

Asta înseamnă:
- **Date live, fără fabricație** — modelul nu poate inventa restaurante, deoarece uneltele merg întotdeauna la catalogul real
- **Autorizarea este păstrată** — uneltele rezervate adminilor (de ex. listarea tuturor utilizatorilor) returnează 403 pentru apelanții non-admin, iar modelul transmite acest lucru utilizatorului
- **Nicio bază de date nouă** — agentul este un pur orchestrator peste serviciile existente

## Unelte expuse

| Unealtă | Endpoint suport | Roluri |
|---|---|---|
| `listRestaurants` | `GET /api/v1/restaurant/getAll` | USER, ADMIN, RESTAURANT_OWNER |
| `getTopRatedRestaurants` | `GET /api/v1/restaurant/top-rated` | USER, ADMIN, RESTAURANT_OWNER |
| `getRestaurantDetails(id)` | `GET /api/v1/restaurant/get/{id}` | USER, ADMIN, RESTAURANT_OWNER |
| `getAverageRating(id)` | `GET /api/v1/restaurant/getRatings/{id}` | USER, ADMIN, RESTAURANT_OWNER |
| `findRestaurantIdByName(name)` | `GET /api/v1/restaurant/getRestaurantId/{name}` | USER, ADMIN, RESTAURANT_OWNER |
| `listAllUsers` | `GET /api/v1/user/all` | **Doar ADMIN** |
| `getUserReviews(userId)` | `GET /api/v1/user/{id}/reviews` | **Doar ADMIN** |

## Stack tehnologic

- **Spring Boot 3.4.5** + **Spring Cloud 2024.0.1** (la fel ca serviciile surori)
- **Spring AI 1.0.0** cu adaptorul `spring-ai-starter-model-anthropic`
- **Anthropic Claude Haiku 4.5** (`claude-haiku-4-5`) — rapid, ieftin, suportă apeluri de unelte
- Înregistrare **Eureka** ca `tastetest-agent`
- **Securitate JWT** identică cu notification-service (HS512, comutatorul `vars.security.enable`)
- **`RestTemplate` cu load-balancing** și un interceptor care transmite mai departe `Authorization` din cererea de intrare, astfel încât apelurile uneltelor sunt autentificate ca utilizatorul original

## Cum se rulează

Cheia API Anthropic este furnizată printr-o variabilă de mediu. Cu `dummy-please-override`, serviciul tot pornește; apelurile eșuează în momentul cererii.

### Local (fără docker)
```fish
cd agent-service
set -x ANTHROPIC_API_KEY sk-ant-...
mvn spring-boot:run
```

### Compose (podman sau docker)
```fish
set -x ANTHROPIC_API_KEY sk-ant-...
podman compose up -d agent-service
```

Intrarea din compose transmite mai departe `ANTHROPIC_API_KEY` și (opțional) `ANTHROPIC_MODEL` din mediul gazdei.

## Frontend

O rută nouă la `/assistant` (vizibilă în bara de navigare ca **Ask TasteBot**) găzduiește un panou de chat care comunică cu `/api/v1/agent/chat` prin gateway. Autentificarea este impusă prin `AuthGuard`, astfel încât utilizatorii anonimi sunt trimiși mai întâi la login.

## Testare

```fish
# obține un JWT pentru un USER
set TOKEN (curl -s -X POST http://localhost/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@taste.test","password":"password"}' | jq -r .token)

# întreabă agentul
curl -s -X POST http://localhost/api/v1/agent/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"message":"What are the top-rated restaurants?"}' | jq .
```

## Fișiere de interes

- [agent-service/pom.xml](../agent-service/pom.xml) — dependențele modulului
- [agent-service/src/main/java/com/example/agent/api/AgentController.java](../agent-service/src/main/java/com/example/agent/api/AgentController.java) — punctul de intrare
- [agent-service/src/main/java/com/example/agent/config/ChatClientConfig.java](../agent-service/src/main/java/com/example/agent/config/ChatClientConfig.java) — system prompt + bean-ul ChatClient
- [agent-service/src/main/java/com/example/agent/tools/](../agent-service/src/main/java/com/example/agent/tools/) — clase de unelte adnotate cu `@Tool`
- [api-gateway/src/main/resources/application.yml](../api-gateway/src/main/resources/application.yml) — ruta `agent-api` (declarată **înainte** de ruta generică `/api/**`)
- [frontend/src/app/components/assistant/](../frontend/src/app/components/assistant/) — interfața de chat
