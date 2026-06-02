# AI Agents — Part II §12

This document covers the **AI Agents – Dezvoltare** bonus from the project rubric. The TasteTest platform ships with an in-app AI assistant ("TasteBot") that runs as a dedicated microservice, integrates into the existing service mesh, and uses tool calling to query the live backend.

## Architecture

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
                                                     │  tool calls (lb://tastetest-restaurant, lb://tastetest-user)
                                                     ▼
                                              ┌──────────────┐
                                              │ existing svcs│
                                              └──────────────┘
```

## What it does

TasteBot is an LLM-powered chat assistant that answers user questions about the TasteTest catalog. Behind the scenes the agent doesn't see the database — it has a small set of **tools** that wrap real REST endpoints on the existing services. When the user asks something like *"what's the top-rated restaurant?"* the model decides to call `getTopRatedRestaurants`, the agent-service forwards the call (with the user's JWT) to `tastetest-restaurant`, and the result is fed back into the model's reasoning loop.

This means:
- **Live data, no fabrication** — the model can't make up restaurants because the tools always go to the real catalog
- **Authorization is preserved** — admin-only tools (e.g. listing all users) return 403 for non-admin callers, and the model surfaces that to the user
- **No new database** — the agent is a pure orchestrator over existing services

## Tools exposed

| Tool | Backing endpoint | Roles |
|---|---|---|
| `listRestaurants` | `GET /api/v1/restaurant/getAll` | USER, ADMIN, RESTAURANT_OWNER |
| `getTopRatedRestaurants` | `GET /api/v1/restaurant/top-rated` | USER, ADMIN, RESTAURANT_OWNER |
| `getRestaurantDetails(id)` | `GET /api/v1/restaurant/get/{id}` | USER, ADMIN, RESTAURANT_OWNER |
| `getAverageRating(id)` | `GET /api/v1/restaurant/getRatings/{id}` | USER, ADMIN, RESTAURANT_OWNER |
| `findRestaurantIdByName(name)` | `GET /api/v1/restaurant/getRestaurantId/{name}` | USER, ADMIN, RESTAURANT_OWNER |
| `listAllUsers` | `GET /api/v1/user/all` | **ADMIN only** |
| `getUserReviews(userId)` | `GET /api/v1/user/{id}/reviews` | **ADMIN only** |

## Stack

- **Spring Boot 3.4.5** + **Spring Cloud 2024.0.1** (matches sibling services)
- **Spring AI 1.0.0** with the `spring-ai-starter-model-anthropic` adapter
- **Anthropic Claude Haiku 4.5** (`claude-haiku-4-5`) — fast, cheap, supports tool calling
- **Eureka** registration as `tastetest-agent`
- **JWT security** identical to notification-service (HS512, `vars.security.enable` toggle)
- **Load-balanced `RestTemplate`** with an interceptor that forwards `Authorization` from the inbound request, so tool calls are authenticated as the original user

## Running it

The Anthropic API key is provided via env var. With `dummy-please-override` the service still boots; calls fail at request time.

### Local (no docker)
```fish
cd agent-service
set -x ANTHROPIC_API_KEY sk-ant-...
mvn spring-boot:run
```

### Compose (podman or docker)
```fish
set -x ANTHROPIC_API_KEY sk-ant-...
podman compose up -d agent-service
```

The compose entry forwards `ANTHROPIC_API_KEY` and (optionally) `ANTHROPIC_MODEL` from the host environment.

## Frontend

A new route at `/assistant` (visible in the navbar as **Ask TasteBot**) hosts a chat panel that talks to `/api/v1/agent/chat` through the gateway. Auth is enforced via `AuthGuard`, so anonymous users are bounced to login first.

## Testing

```fish
# get a JWT for a USER
set TOKEN (curl -s -X POST http://localhost/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@taste.test","password":"password"}' | jq -r .token)

# ask the agent
curl -s -X POST http://localhost/api/v1/agent/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"message":"What are the top-rated restaurants?"}' | jq .
```

## Files of interest

- [agent-service/pom.xml](agent-service/pom.xml) — module dependencies
- [agent-service/src/main/java/com/example/agent/api/AgentController.java](agent-service/src/main/java/com/example/agent/api/AgentController.java) — entry point
- [agent-service/src/main/java/com/example/agent/config/ChatClientConfig.java](agent-service/src/main/java/com/example/agent/config/ChatClientConfig.java) — system prompt + ChatClient bean
- [agent-service/src/main/java/com/example/agent/tools/](agent-service/src/main/java/com/example/agent/tools/) — `@Tool`-annotated tool classes
- [api-gateway/src/main/resources/application.yml](api-gateway/src/main/resources/application.yml) — `agent-api` route (declared **before** the `/api/**` catch-all)
- [frontend/src/app/components/assistant/](frontend/src/app/components/assistant/) — chat UI
