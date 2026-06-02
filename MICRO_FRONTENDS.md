# Micro-Frontends

This document covers Part II §10 of the project requirements: split the frontend into independent modules.

## Architecture at a glance

```
                        Browser (http://localhost:4200)
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
  ┌────────────────────┐        runtime composition  ┌────────────────────┐
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

The Admin Panel was split out of the shell into its own Angular workspace. The two are:

- **independently versioned** — separate `package.json` and `angular.json`,
- **independently built** — each has its own multi-stage Dockerfile (`node:22-alpine` build → `nginx:1.27-alpine` serve),
- **independently deployed** — separate compose service, separate published port (`4201`).

The shell only knows two things about the MFE: its public URL and the authentication contract (a JWT handed off through the iframe URL fragment).

## Why split *Admin* off?

Admin is the cleanest seam in this app:

- single route (`/admin`) with no shared state with the rest of the UI,
- distinct audience (operators vs. end users) — different release cadence is plausible,
- self-contained data graph — calls only `/api/v1/{user,role,feedback}/*`,
- the failure mode of a broken admin build is bounded (regular users keep working).

These are textbook MFE candidates: a coherent capability that benefits from independent ownership.

## Independence — the receipts

| Concern | Shell (`frontend/`) | MFE (`mfe-admin/`) |
|---|---|---|
| Build target | `dist/frontend/browser` | `dist/mfe-admin/browser` |
| Container | `tastetest-awbd_frontend_1` | `tastetest-awbd_mfe-admin_1` |
| Published port | 4200 | 4201 |
| Owns API gateway proxy in nginx | yes (`/api/`) | yes (`/api/`) |
| Knows the other exists | only as a URL | not at all |
| Source of `Admin` component | thin iframe loader | full implementation |
| Source of `UserService` / `RoleService` / `FeedbackService` | (legacy, no longer referenced from the shell) | own copies |
| Source of `admin.model.ts` / `review.model.ts` | (legacy) | own copies |

The model + service duplication is *deliberate* — that's what independence costs, and it's exactly what makes either side replaceable without touching the other.

## Runtime composition: iframe + URL fragment handoff

The shell's `Admin` component is a thin loader (`frontend/src/app/components/admin/admin.ts`):

1. read JWT from `localStorage.access_token`,
2. compute `iframeUrl = ${ADMIN_MFE_URL}/#token=${encodeURIComponent(jwt)}`,
3. render `<iframe [src]="iframeUrl">`.

The MFE's `main.ts` (`mfe-admin/src/main.ts`):

1. on bootstrap, parse `window.location.hash` for `#token=...`,
2. write the token to its own `localStorage.access_token`,
3. `history.replaceState` to scrub the fragment from the URL bar,
4. bootstrap the standalone `AdminRoot` component,
5. each outbound HTTP call is intercepted by `AuthInterceptor`, which attaches `Authorization: Bearer <jwt>` from `localStorage`.

### Why a URL fragment, not a query parameter

URL fragments (`#…`) are not sent to servers — neither the MFE's nginx, the gateway, nor any logging proxy will see the JWT. Query parameters (`?token=…`) would leak into nginx access logs and `Referer` headers.

### Why an iframe, not Module Federation / Angular Elements

The project requirement names *Module Federation / Single-SPA*. We chose iframe-based composition because:

- **bulletproof isolation** — no Zone.js or injector clashes between two Angular runtimes mounted in the same DOM (a real risk with Angular Elements, since the shell is also Angular 21),
- **truly polyglot** — the MFE could be rewritten in React/Vue/Solid tomorrow with zero shell changes,
- **trivial to reason about** — `<iframe src=URL>` is a contract any reviewer can verify,
- **independent deploy in the strongest sense** — the MFE can ship its own polyfills, change Angular major version, and never break the shell.

Trade-offs we accepted:
- shared global UI patterns (e.g. the navbar) don't reach inside the iframe — but that's also what makes the boundary clean,
- cross-frame events would need `postMessage` if we ever wanted them (we don't, today).

If we needed deep visual integration in the future, swapping iframe for `<script type="module">` + a custom element registration is a one-component change in the shell.

## Auth flow end-to-end

1. User signs in at the shell (`/login`). Shell's `AuthService` stores the JWT in its `localStorage`.
2. User navigates to `/admin`. Shell renders the `Admin` loader with `<iframe src="http://localhost:4201/#token=<jwt>">`.
3. MFE bootstraps, lifts the token off the fragment, stores it in *its* `localStorage`, scrubs the URL.
4. MFE makes API calls to its own nginx `/api/` location, which proxies to `api-gateway:80`. Each request carries `Authorization: Bearer <jwt>`.
5. The gateway validates the JWT (the Part II §6 setup) and forwards to `user-service` / `restaurant-service` etc.

If the user logs out in the shell, navigating away from `/admin` unmounts the iframe and the MFE's `localStorage` no longer matters until the next visit. (A future enhancement is `iframe.contentWindow.postMessage({type:'logout'})` so the MFE can self-clear immediately — left for later.)

## Files added / modified

| File | Change |
|---|---|
| `mfe-admin/` (whole tree) | **new** — independent Angular 21 workspace for the Admin Panel |
| `mfe-admin/Dockerfile` | **new** — multi-stage `node → nginx` build |
| `mfe-admin/nginx.conf` | **new** — serves `index.html`, proxies `/api/` to `api-gateway:80`, `frame-ancestors` allows the shell to embed it |
| `mfe-admin/src/main.ts` | **new** — token handoff via URL fragment, then `bootstrapApplication(AdminRoot)` |
| `mfe-admin/src/app/admin/*` | **new** — full Admin UI (lifted from the shell) |
| `mfe-admin/src/app/services/*` | **new** — own copies of `ApiService` / `UserService` / `RoleService` / `FeedbackService` / `AuthInterceptor` |
| `frontend/src/app/components/admin/admin.ts` | **modified** — replaced with thin iframe loader |
| `frontend/src/app/components/admin/admin.html` | **modified** — banner + iframe |
| `frontend/src/app/components/admin/admin.css` | **modified** — frame styles |
| `frontend/src/environments/environment{,.prod}.ts` | **modified** — added `adminMfeUrl: 'http://localhost:4201'` |
| `docker-compose.yml` | **modified** — added `mfe-admin` service on host port 4201; shell now `depends_on: mfe-admin` |

The shell still ships the (no-longer-imported) `UserService` / `RoleService` / `admin.model.ts`. Cleanest follow-up would be to delete them; left in place to keep this diff focused on the MFE split.

## How to verify

```bash
./run-podman.sh up
# wait for healthchecks to settle (~60s)
./run-podman.sh ps        # expect 'mfe-admin' service Up
curl -I http://localhost:4201/    # MFE serving HTML directly
curl -I http://localhost:4200/    # shell serving HTML

# Then in a browser:
#   1. http://localhost:4200/login → log in as admin@admin.com / admin
#   2. navigate to /admin → see the "Admin (micro-frontend)" banner with the
#      MFE origin printed, and the embedded admin UI underneath.
#   3. browser DevTools → Network → confirm requests come from origin
#      http://localhost:4201 (the MFE), not http://localhost:4200 (the shell).
#   4. DevTools → Application → Frames → confirm two separate Angular apps.
```

To kill *just* the MFE and confirm fault isolation:

```bash
podman stop tastetest-awbd_mfe-admin_1
# /admin in the shell shows a broken iframe; everything else still works.
podman start tastetest-awbd_mfe-admin_1
# /admin recovers on next reload.
```
