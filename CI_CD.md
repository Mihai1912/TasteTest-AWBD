# CI/CD Pipeline

This document covers Part II §11 of the project requirements: automated build, automated tests, Docker containerization and automated deployment to staging.

## Where it lives

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) — runs on every push and pull request targeting `main`.

## Pipeline stages

```
push / PR → main
        │
        ├──► backend            (mvn -B verify, all 9 modules)
        │
        ├──► frontend [matrix]  (npm ci && npm run build — frontend, mfe-admin)
        │
        └──► docker-build-push  (needs: backend, frontend; push to main only)
                  │
                  └─► build + push 11 images to ghcr.io, tags ":staging" and ":sha-<commit>"
```

### 1. `backend` — build automatizat + teste automate

`./mvnw -B -ntp verify` at the repo root. The root [`pom.xml`](pom.xml) is a multi-module reactor (`auth-service`, `user-service`, `restaurant-service`, `backend-original`, `config-server`, `discovery-server`, `api-gateway`, `notification-service`, `agent-service`) — a single Maven invocation compiles all of them and runs the existing unit tests (e.g. `ReviewServiceTest`, a pure Mockito test with no external dependencies, so it runs without a database).

### 2. `frontend` — build automatizat (Angular)

Matrix job over `frontend` (shell) and `mfe-admin` (admin micro-frontend, see [MICRO_FRONTENDS.md](MICRO_FRONTENDS.md)). Each runs `npm ci` + `npm run build` with Node 20.

### 3. `docker-build-push` — Docker containerization + deployment automat (staging)

Runs only for pushes to `main`, after `backend` and `frontend` succeed. A matrix builds all 11 service images (the same `context`/`dockerfile` pairs used by [`docker-compose.yml`](docker-compose.yml)) and pushes them to **GitHub Container Registry**:

- `ghcr.io/<owner>/tastetest-<service>:staging` — always points at the latest build from `main`, the "staging" tag a deployment would pull.
- `ghcr.io/<owner>/tastetest-<service>:sha-<commit>` — immutable, traceable to the exact commit.

Pushing a new `:staging` image is the automated deployment step: a staging environment running `docker compose` with `image: ghcr.io/<owner>/tastetest-<service>:staging` (`pull_policy: always`) picks up the new build on its next `docker compose pull && docker compose up -d`.

## How to verify

- Open the **Actions** tab on GitHub — every push/PR to `main` shows `backend` and the two `frontend` jobs.
- On a push to `main`, `docker-build-push` appears and, once green, the images show up under `https://github.com/<owner>?tab=packages` (one `tastetest-*` package per service, `staging` + `sha-*` tags).
- Locally, the same Maven/npm commands the pipeline runs can be reproduced with `./mvnw -B -ntp verify` and `npm ci && npm run build` in `frontend/`/`mfe-admin/`.
