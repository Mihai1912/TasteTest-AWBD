# Pipeline CI/CD

Acest document acoperă Partea a II-a §11 din cerințele proiectului: build automatizat, teste automatizate, containerizare Docker și deployment automatizat în staging.

## Unde se află

[`.github/workflows/ci.yml`](../.github/workflows/ci.yml) — rulează la fiecare push și pull request care țintește `main`.

## Etapele pipeline-ului

```
push / PR → main
        │
        ├──► backend            (mvn -B verify, toate cele 9 module)
        │
        ├──► frontend [matrix]  (npm ci && npm run build — frontend, mfe-admin)
        │
        └──► docker-build-push  (necesită: backend, frontend; push doar pe main)
                  │
                  └─► build + push 11 imagini la ghcr.io, tag-uri ":staging" și ":sha-<commit>"
```

### 1. `backend` — build automatizat + teste automate

`./mvnw -B -ntp verify` la rădăcina repo-ului. [`pom.xml`](../pom.xml) de la rădăcină este un reactor multi-modul (`auth-service`, `user-service`, `restaurant-service`, `backend-original`, `config-server`, `discovery-server`, `api-gateway`, `notification-service`, `agent-service`) — o singură invocare Maven le compilează pe toate și rulează testele unitare existente (de ex. `ReviewServiceTest`, un test Mockito pur, fără dependențe externe, deci rulează fără bază de date).

### 2. `frontend` — build automatizat (Angular)

Job de tip matrix peste `frontend` (shell) și `mfe-admin` (micro-frontend-ul de admin, vezi [MICRO_FRONTENDS.md](MICRO_FRONTENDS.md)). Fiecare rulează `npm ci` + `npm run build` cu Node 20.

### 3. `docker-build-push` — containerizare Docker + deployment automat (staging)

Rulează doar pentru push-uri pe `main`, după ce `backend` și `frontend` reușesc. Un matrix construiește toate cele 11 imagini de servicii (aceleași perechi `context`/`dockerfile` folosite de [`docker-compose.yml`](../docker-compose.yml)) și le împinge către **GitHub Container Registry**:

- `ghcr.io/<owner>/tastetest-<service>:staging` — indică întotdeauna spre cel mai recent build de pe `main`, tag-ul "staging" pe care un deployment l-ar prelua.
- `ghcr.io/<owner>/tastetest-<service>:sha-<commit>` — imutabil, trasabil la commit-ul exact.

Împingerea unei imagini `:staging` noi este pasul de deployment automat: un mediu de staging care rulează `docker compose` cu `image: ghcr.io/<owner>/tastetest-<service>:staging` (`pull_policy: always`) preia noul build la următorul `docker compose pull && docker compose up -d`.

## Cum verifici

- Deschide tab-ul **Actions** pe GitHub — fiecare push/PR pe `main` afișează `backend` și cele două job-uri `frontend`.
- La un push pe `main`, apare `docker-build-push` și, odată ce este verde, imaginile apar la `https://github.com/<owner>?tab=packages` (un pachet `tastetest-*` per serviciu, cu tag-urile `staging` + `sha-*`).
- Local, aceleași comenzi Maven/npm pe care le rulează pipeline-ul pot fi reproduse cu `./mvnw -B -ntp verify` și `npm ci && npm run build` în `frontend/`/`mfe-admin/`.
