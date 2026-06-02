#!/usr/bin/env bash
# Verifies the Saga (Orchestration) pattern in restaurant-service:
#   happy path:    POST /restaurant/add  -> persist + notify, restaurant visible in /getAll
#   compensation:  POST /restaurant/add with notification-service down -> 400 (controller swallows
#                  the SagaFailedException as 400), row rolled back via compensation.
# Requires the full stack up via ./run-podman.sh (gateway on :8090).
# Run with: ./test-saga.sh
set -u

GW="http://localhost:8090"
ADMIN_EMAIL="admin@admin.com"
ADMIN_PASS="admin"
SUFFIX=$RANDOM
HAPPY_NAME="SagaHappy-$SUFFIX"
ROLLBACK_NAME="SagaRollback-$SUFFIX"

# Allow overriding the compose service name for notification-service.
NOTIF_SVC="${NOTIF_SVC:-notification-service}"

# Pick the available compose front-end (mirrors run-podman.sh).
if podman compose version >/dev/null 2>&1; then
  PC="podman compose"
elif command -v podman-compose >/dev/null 2>&1; then
  PC="podman-compose"
else
  echo "Neither 'podman compose' nor 'podman-compose' is available."
  exit 1
fi

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

# expect_5xx <label> <actual_status>
expect_5xx() {
  if [ "$2" -ge 500 ] && [ "$2" -le 599 ]; then green "  PASS  $1 (HTTP $2)"; PASS=$((PASS+1));
  else                                          red   "  FAIL  $1 (expected 5xx, got $2)"; FAIL=$((FAIL+1)); fi
}

# assert_contains <label> <needle> <haystack>
assert_contains() {
  if echo "$3" | grep -q -- "$2"; then green "  PASS  $1"; PASS=$((PASS+1));
  else                                 red   "  FAIL  $1 (missing: $2)"; FAIL=$((FAIL+1)); fi
}

assert_not_contains() {
  if echo "$3" | grep -q -- "$2"; then red   "  FAIL  $1 (unexpected: $2 still present)"; FAIL=$((FAIL+1));
  else                                 green "  PASS  $1"; PASS=$((PASS+1)); fi
}

extract_token() {
  python3 -c 'import sys,json
try:
  d=json.load(sys.stdin); print(d.get("access_token") or d.get("accessToken") or d.get("token") or "")
except Exception: print("")'
}

post_status() {
  # post_status <url> <token> <body> -> prints HTTP status code
  curl -s -o /dev/null -w '%{http_code}' -X POST "$1" \
    -H "Authorization: Bearer $2" \
    -H 'Content-Type: application/json' \
    -d "$3"
}

# Always try to bring notification-service back up on exit, even on failure.
trap '$PC start "$NOTIF_SVC" >/dev/null 2>&1 || true' EXIT

hdr "0. Sanity: gateway reachable"
GW_STATUS=$(curl -s -o /dev/null -w '%{http_code}' "$GW/actuator/health" || echo "000")
if [ "$GW_STATUS" = "000" ]; then
  red "Gateway unreachable at $GW. Run ./run-podman.sh up first."; exit 1
fi
yel "Gateway responded ($GW_STATUS at /actuator/health)"

hdr "1. Login as admin -> get JWT"
LOGIN_BODY=$(curl -s -X POST "$GW/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASS\"}")
TOKEN=$(echo "$LOGIN_BODY" | extract_token)
if [ -z "$TOKEN" ]; then
  red "Login failed. Body was:"; echo "$LOGIN_BODY"; exit 1
fi
green "Got admin token (${#TOKEN} chars)"

hdr "2. Happy path: addRestaurant should persist AND notify"
HAPPY_BODY="{\"name\":\"$HAPPY_NAME\",\"address\":\"1 Saga St\",\"phone\":\"555-0101\",\"website\":\"https://saga.example\",\"schedule\":\"Mon-Fri 9-17\"}"
RC=$(post_status "$GW/api/v1/restaurant/add" "$TOKEN" "$HAPPY_BODY")
expect "POST /api/v1/restaurant/add (happy)" 201 "$RC"

LIST=$(curl -s -H "Authorization: Bearer $TOKEN" "$GW/api/v1/restaurant/getAll")
assert_contains "Happy restaurant '$HAPPY_NAME' present in /getAll" "$HAPPY_NAME" "$LIST"
yel "Check restaurant-service logs for: [SAGA] step persistRestaurant ok / step notifyOwner ok / AddRestaurant completed"
yel "Check notification-service logs for incoming POST /api/notifications with title='Restaurant created'"

hdr "3. Compensation: notification-service down -> saga must fail and rollback"
yel "Stopping $NOTIF_SVC..."
$PC stop "$NOTIF_SVC" >/dev/null
yel "Waiting briefly for the load balancer to notice..."
sleep 5

ROLLBACK_BODY="{\"name\":\"$ROLLBACK_NAME\",\"address\":\"x\",\"phone\":\"x\",\"website\":\"x\",\"schedule\":\"x\"}"
RC=$(post_status "$GW/api/v1/restaurant/add" "$TOKEN" "$ROLLBACK_BODY")
expect "POST /api/v1/restaurant/add with notif-service down" 400 "$RC"

yel "Restarting $NOTIF_SVC..."
$PC start "$NOTIF_SVC" >/dev/null
sleep 3

LIST=$(curl -s -H "Authorization: Bearer $TOKEN" "$GW/api/v1/restaurant/getAll")
assert_not_contains "Rolled-back restaurant '$ROLLBACK_NAME' absent from /getAll (compensation deleted it)" "$ROLLBACK_NAME" "$LIST"
yel "Check restaurant-service logs for: [SAGA] AddRestaurant failed ... running 1 compensation(s) / [SAGA] compensation deleteRestaurant"

hdr "Summary"
green "PASS: $PASS"
if [ "$FAIL" -gt 0 ]; then red "FAIL: $FAIL"; exit 1; else green "FAIL: 0"; fi
