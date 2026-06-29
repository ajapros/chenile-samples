#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-runtime/service-registry/docker-compose.yml}"
REGISTRY_URL="${REGISTRY_URL:-http://localhost:8090}"
RESTARTS="${RESTARTS:-3}"

compose() {
  docker compose -f "${COMPOSE_FILE}" "$@"
}

wait_for_http() {
  local url="$1"
  local label="$2"
  for _ in $(seq 1 60); do
    if curl -fsS "${url}" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "Timed out waiting for ${label} at ${url}" >&2
  return 1
}

registry_count() {
  compose exec -T postgres psql -U registry_user -d service_registry -Atc "select count(*) from service_definition;"
}

diagnostics_json() {
  curl -fsS "${REGISTRY_URL}/serviceregistry/diagnostics"
}

assert_clean_diagnostics() {
  diagnostics_json | python3 -c '
import json, sys
doc = json.load(sys.stdin)
payload = doc.get("payload") or {}
checks = [
    "duplicateServiceVersionGroups",
    "changedSameVersionGroups",
    "duplicateOperationLinks",
    "duplicateParamLinks",
    "invalidServiceRows",
]
bad = {key: payload.get(key) for key in checks if payload.get(key) not in (0, None)}
if bad:
    raise SystemExit(f"service registry diagnostics are not clean: {bad}")
'
}

wait_for_http "${REGISTRY_URL}/actuator/health" "registry health"
wait_for_http "http://localhost:8091/actuator/health" "orders health"
wait_for_http "http://localhost:8092/actuator/health" "payments health"
wait_for_http "${REGISTRY_URL}/serviceregistry" "registry list"

assert_clean_diagnostics
before="$(registry_count)"
echo "service_definition rows before restarts: ${before}"

for i in $(seq 1 "${RESTARTS}"); do
  echo "restart cycle ${i}/${RESTARTS}"
  compose restart mock-orders-service mock-payments-service
  wait_for_http "http://localhost:8091/actuator/health" "orders health"
  wait_for_http "http://localhost:8092/actuator/health" "payments health"
  wait_for_http "${REGISTRY_URL}/serviceregistry" "registry list"
  assert_clean_diagnostics
done

after="$(registry_count)"
echo "service_definition rows after restarts: ${after}"

if [ "${before}" != "${after}" ]; then
  echo "Registry row count changed after restarts. before=${before} after=${after}" >&2
  exit 1
fi

echo "Service registry restart idempotency validated."
