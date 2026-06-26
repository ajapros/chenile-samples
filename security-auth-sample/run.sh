#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$BASE_DIR/../.." && pwd)"
COMPOSE_FILE="$BASE_DIR/runtime/spring-auth/docker-compose.yml"

cd "$ROOT_DIR/chenile-security"
mvn -DskipTests install

cd "$ROOT_DIR/chenile-samples/security-auth-sample"
mvn -DskipTests package
docker compose -f "$COMPOSE_FILE" up --build
