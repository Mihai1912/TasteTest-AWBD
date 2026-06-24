# Micro-Frontend-uri

Acest document acoperă Partea a II-a §10 din cerințele proiectului: împărțirea frontend-ului în module independente.

## Arhitectura pe scurt

```
                        Browser (http://localhost:4200)
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
  ┌────────────────────┐        compoziție la runtime ┌────────────────────┐
  │   Shell (host)     │   ────── iframe ──────►     │   mfe-admin        │
  │   frontend/        │   src=//localhost:4201/     │   mfe-admin/       │
  │                    │       #token=<jwt>          │                    │
  │   Angular 21       │                             │   Angular 21       │
  │   port 4200        │                             │   port 4201        │
  └────────┬───────────┘                             └─────────┬──────────┘
           │                                                   │
           └────────────────── /api/v1 ────────────────────────┘
                                  ▼
                       api-gateway (port 8090)
```

Panoul de Admin a fost desprins din shell într-un workspace Angular propriu. Cele două sunt:

- **versionate independent** — `package.json` și `angular.json` separate,
- **construite independent** — fiecare are propriul Dockerfile multi-stage (build `node:22-alpine` → servire `nginx:1.27-alpine`),
- **deployate independent** — serviciu compose separat, port publicat separat (`4201`).

Shell-ul știe doar două lucruri despre MFE: URL-ul său public și contractul de autentificare (un JWT predat prin fragmentul URL-ului iframe-ului).

## De ce a fost desprins *Admin*?

Admin este cea mai curată "cusătură" din această aplicație:

- o singură rută (`/admin`) fără stare partajată cu restul interfeței,
- audiență distinctă (operatori vs. utilizatori finali) — un ritm de release diferit este plauzibil,
- graf de date auto-conținut — apelează doar `/api/v1/{user,role,feedback}/*`,
- modul de eșec al unui build de admin defect este delimitat (utilizatorii obișnuiți continuă să funcționeze).

Acestea sunt candidate MFE de manual: o capabilitate coerentă care beneficiază de proprietate independentă.

## Independența — dovezile

| Aspect | Shell (`frontend/`) | MFE (`mfe-admin/`) |
|---|---|---|
| Țintă de build | `dist/frontend/browser` | `dist/mfe-admin/browser` |
| Container | `tastetest-awbd_frontend_1` | `tastetest-awbd_mfe-admin_1` |
| Port publicat | 4200 | 4201 |
| Deține proxy-ul către api gateway în nginx | da (`/api/`) | da (`/api/`) |
| Știe că celălalt există | doar ca URL | deloc |
| Sursa componentei `Admin` | loader subțire de iframe | implementare completă |
| Sursa `UserService` / `RoleService` / `FeedbackService` | (legacy, nemaireferențiat din shell) | copii proprii |
| Sursa `admin.model.ts` / `review.model.ts` | (legacy) | copii proprii |

Duplicarea modelelor + serviciilor este *deliberată* — acesta este costul independenței și exact ceea ce face ca oricare parte să fie înlocuibilă fără a o atinge pe cealaltă.

## Compoziție la runtime: iframe + predarea prin fragmentul URL

Componenta `Admin` a shell-ului este un loader subțire (`frontend/src/app/components/admin/admin.ts`):

1. citește JWT-ul din `localStorage.access_token`,
2. calculează `iframeUrl = ${ADMIN_MFE_URL}/#token=${encodeURIComponent(jwt)}`,
3. randează `<iframe [src]="iframeUrl">`.

`main.ts` al MFE-ului (`mfe-admin/src/main.ts`):

1. la bootstrap, parsează `window.location.hash` pentru `#token=...`,
2. scrie token-ul în propriul `localStorage.access_token`,
3. `history.replaceState` pentru a curăța fragmentul din bara de adrese,
4. face bootstrap la componenta standalone `AdminRoot`,
5. fiecare apel HTTP de ieșire este interceptat de `AuthInterceptor`, care atașează `Authorization: Bearer <jwt>` din `localStorage`.

### De ce un fragment URL, nu un parametru de query

Fragmentele URL (`#…`) nu sunt trimise serverelor — nici nginx-ul MFE-ului, nici gateway-ul, nici vreun proxy de logare nu vor vedea JWT-ul. Parametrii de query (`?token=…`) ar scăpa în log-urile de acces nginx și în antetele `Referer`.

### De ce un iframe, nu Module Federation / Angular Elements

Cerința proiectului numește *Module Federation / Single-SPA*. Am ales compoziția bazată pe iframe deoarece:

- **izolare imbatabilă** — fără ciocniri de Zone.js sau de injectoare între două runtime-uri Angular montate în același DOM (un risc real cu Angular Elements, deoarece shell-ul este de asemenea Angular 21),
- **cu adevărat poliglotă** — MFE-ul ar putea fi rescris în React/Vue/Solid mâine, cu zero modificări în shell,
- **trivial de raționat** — `<iframe src=URL>` este un contract pe care orice evaluator îl poate verifica,
- **deploy independent în sensul cel mai puternic** — MFE-ul își poate livra propriile polyfill-uri, poate schimba versiunea majoră de Angular și nu va strica niciodată shell-ul.

Compromisuri pe care le-am acceptat:
- șabloanele de UI globale partajate (de ex. bara de navigare) nu ajung în interiorul iframe-ului — dar tocmai asta face ca granița să fie curată,
- evenimentele cross-frame ar avea nevoie de `postMessage` dacă le-am dori vreodată (nu le dorim, astăzi).

Dacă am avea nevoie de o integrare vizuală profundă în viitor, schimbarea iframe-ului cu `<script type="module">` + înregistrarea unui custom element este o modificare la o singură componentă în shell.

## Fluxul de autentificare cap-coadă

1. Utilizatorul se autentifică în shell (`/login`). `AuthService`-ul shell-ului stochează JWT-ul în `localStorage`-ul său.
2. Utilizatorul navighează la `/admin`. Shell-ul randează loader-ul `Admin` cu `<iframe src="http://localhost:4201/#token=<jwt>">`.
3. MFE-ul face bootstrap, extrage token-ul din fragment, îl stochează în *propriul* `localStorage`, curăță URL-ul.
4. MFE-ul face apeluri API către propria locație nginx `/api/`, care le proxy-ează către `api-gateway:80`. Fiecare cerere poartă `Authorization: Bearer <jwt>`.
5. Gateway-ul validează JWT-ul (configurarea de la Partea a II-a §6) și redirecționează către `user-service` / `restaurant-service` etc.

Dacă utilizatorul se deconectează în shell, navigarea în afara `/admin` demontează iframe-ul și `localStorage`-ul MFE-ului nu mai contează până la vizita următoare. (O îmbunătățire viitoare este `iframe.contentWindow.postMessage({type:'logout'})`, astfel încât MFE-ul să se poată curăța singur imediat — lăsat pentru mai târziu.)

## Fișiere adăugate / modificate

| Fișier | Modificare |
|---|---|
| `mfe-admin/` (tot arborele) | **nou** — workspace Angular 21 independent pentru Panoul de Admin |
| `mfe-admin/Dockerfile` | **nou** — build multi-stage `node → nginx` |
| `mfe-admin/nginx.conf` | **nou** — servește `index.html`, proxy-ează `/api/` către `api-gateway:80`, `frame-ancestors` permite shell-ului să-l încorporeze |
| `mfe-admin/src/main.ts` | **nou** — predarea token-ului prin fragmentul URL, apoi `bootstrapApplication(AdminRoot)` |
| `mfe-admin/src/app/admin/*` | **nou** — interfața completă de Admin (preluată din shell) |
| `mfe-admin/src/app/services/*` | **nou** — copii proprii ale `ApiService` / `UserService` / `RoleService` / `FeedbackService` / `AuthInterceptor` |
| `frontend/src/app/components/admin/admin.ts` | **modificat** — înlocuit cu un loader subțire de iframe |
| `frontend/src/app/components/admin/admin.html` | **modificat** — banner + iframe |
| `frontend/src/app/components/admin/admin.css` | **modificat** — stiluri pentru frame |
| `frontend/src/environments/environment{,.prod}.ts` | **modificat** — adăugat `adminMfeUrl: 'http://localhost:4201'` |
| `docker-compose.yml` | **modificat** — adăugat serviciul `mfe-admin` pe portul gazdă 4201; shell-ul are acum `depends_on: mfe-admin` |

Shell-ul livrează în continuare `UserService` / `RoleService` / `admin.model.ts` (care nu mai sunt importate). Cel mai curat follow-up ar fi ștergerea lor; au fost lăsate pe loc pentru a menține acest diff concentrat pe desprinderea MFE-ului.

## Cum verifici

```bash
./run-podman.sh up
# așteaptă să se stabilizeze healthcheck-urile (~60s)
./run-podman.sh ps        # se așteaptă serviciul 'mfe-admin' în starea Up
curl -I http://localhost:4201/    # MFE servind HTML direct
curl -I http://localhost:4200/    # shell servind HTML

# Apoi într-un browser:
#   1. http://localhost:4200/login → autentifică-te ca admin@admin.com / admin
#   2. navighează la /admin → vezi banner-ul "Admin (micro-frontend)" cu
#      originea MFE-ului afișată, iar interfața de admin încorporată dedesubt.
#   3. browser DevTools → Network → confirmă că cererile vin de la originea
#      http://localhost:4201 (MFE-ul), nu http://localhost:4200 (shell-ul).
#   4. DevTools → Application → Frames → confirmă două aplicații Angular separate.
```

Pentru a opri *doar* MFE-ul și a confirma izolarea la defect:

```bash
podman stop tastetest-awbd_mfe-admin_1
# /admin în shell afișează un iframe defect; tot restul funcționează în continuare.
podman start tastetest-awbd_mfe-admin_1
# /admin își revine la următoarea reîncărcare.
```
