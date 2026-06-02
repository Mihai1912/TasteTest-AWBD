#!/usr/bin/env bash
# Temporary script — verifies distributed JWT security across the gateway and downstream services.
# Delete when done. Run with: ./test-jwt-security.sh
set -u

GW="http://localhost:8090"
ADMIN_EMAIL="admin@admin.com"
ADMIN_PASS="admin"
USER_EMAIL="plain$RANDOM@user.com"
USER_PASS="plainpass"
USER_NAME="plain$RANDOM"

PASS=0; FAIL=0
green() { printf '\033[32m%s\033[0m\n' "$*"; }
red()   { printf '\033[31m%s\033[0m\n' "$*"; }
yel()   { printf '\033[33m%s\033[0m\n' "$*"; }
hdr()   { printf '\n\033[1;36m== %s ==\033[0m\n' "$*"; }

# expect <label> <expected_status> <actual_status>
expect() {
  if [ "$2" = "$3" ]; then green "  PASS  $1 (HTTP $3)"; PASS=$((PASS+1));
  else                     red   "  FAIL  $1 (expected $2, got $3)"; FAIL=$((FAIL+1)); fi
}

extract_token() {
  # Reads JSON from stdin, prints access_token field. Falls back gracefully.
  python3 -c 'import sys,json
try:
  d=json.load(sys.stdin); print(d.get("access_token") or d.get("accessToken") or d.get("token") or "")
except Exception: print("")'
}

status() {
  # status <method> <url> [extra curl args...]
  curl -s -o /dev/null -w '%{http_code}' -X "$1" "$2" "${@:3}"
}

hdr "0. Sanity: gateway reachable"
GW_STATUS=$(curl -s -o /dev/null -w '%{http_code}' "$GW/actuator/health" || echo "000")
if [ "$GW_STATUS" = "000" ]; then red "Gateway unreachable at $GW. Run ./run.sh up first."; exit 1; fi
yel "Gateway responded ($GW_STATUS at /actuator/health)"

hdr "1. Login as admin -> get JWT"
LOGIN_BODY=$(curl -s -X POST "$GW/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASS\"}")
ADMIN_TOKEN=$(echo "$LOGIN_BODY" | extract_token)
if [ -z "$ADMIN_TOKEN" ]; then
  red "Login failed. Body was:"; echo "$LOGIN_BODY"; exit 1
fi
green "Got admin token (${#ADMIN_TOKEN} chars)"

hdr "2. Unauthenticated requests are rejected (expect 401)"
expect "GET /api/v1/restaurant/getAll  no-token" 401 "$(status GET  $GW/api/v1/restaurant/getAll)"
expect "GET /api/v1/user/all            no-token" 401 "$(status GET  $GW/api/v1/user/all)"
expect "POST /api/internal/notifications no-token" 401 "$(status POST $GW/api/internal/notifications -H 'Content-Type: application/json' -d '{\"title\":\"x\",\"message\":\"y\",\"sourceService\":\"test\"}')"

hdr "3. Authenticated admin requests succeed (expect 2xx)"
RC=$(status GET  $GW/api/v1/restaurant/getAll -H "Authorization: Bearer $ADMIN_TOKEN")
expect "GET /api/v1/restaurant/getAll   admin" 200 "$RC"
RC=$(status GET  $GW/api/v1/user/all -H "Authorization: Bearer $ADMIN_TOKEN")
expect "GET /api/v1/user/all            admin (PreAuthorize ADMIN)" 200 "$RC"
RC=$(status POST $GW/api/internal/notifications \
       -H "Authorization: Bearer $ADMIN_TOKEN" \
       -H 'Content-Type: application/json' \
       -d '{"title":"hello","message":"world","sourceService":"test-script"}')
expect "POST /api/internal/notifications admin" 200 "$RC"

hdr "4. Header spoofing without a token is blocked (expect 401)"
RC=$(status GET $GW/api/v1/user/all \
       -H "X-Auth-Email: admin@admin.com" \
       -H "X-Auth-Roles: ADMIN")
expect "GET /api/v1/user/all spoofed X-Auth-* no-token" 401 "$RC"

hdr "5. Tampered token rejected (expect 401)"
RC=$(status GET $GW/api/v1/restaurant/getAll -H "Authorization: Bearer ${ADMIN_TOKEN}X")
expect "GET /api/v1/restaurant/getAll tampered" 401 "$RC"

hdr "6. Garbage token rejected (expect 401)"
RC=$(status GET $GW/api/v1/restaurant/getAll -H "Authorization: Bearer not-a-real-jwt")
expect "GET /api/v1/restaurant/getAll garbage" 401 "$RC"

hdr "7. Register a non-admin USER, then try ADMIN-only endpoint"
REG_BODY=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$GW/api/v1/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$USER_EMAIL\",\"username\":\"$USER_NAME\",\"password\":\"$USER_PASS\"}")
yel "register HTTP $REG_BODY  ($USER_EMAIL)"

USER_LOGIN=$(curl -s -X POST "$GW/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$USER_EMAIL\",\"password\":\"$USER_PASS\"}")
USER_TOKEN=$(echo "$USER_LOGIN" | extract_token)

if [ -z "$USER_TOKEN" ]; then
  red "  Plain-user login failed (body: $USER_LOGIN). Skipping role-based checks."
  FAIL=$((FAIL+1))
else
  green "  Got plain-user token"
  RC=$(status GET $GW/api/v1/user/all -H "Authorization: Bearer $USER_TOKEN")
  expect "GET /api/v1/user/all plain-user (expect 403)" 403 "$RC"
  RC=$(status GET $GW/api/v1/restaurant/getAll -H "Authorization: Bearer $USER_TOKEN")
  expect "GET /api/v1/restaurant/getAll plain-user (expect 200)" 200 "$RC"
fi

hdr "Summary"
green "PASS: $PASS"
if [ "$FAIL" -gt 0 ]; then red "FAIL: $FAIL"; exit 1; else green "FAIL: 0"; fi
