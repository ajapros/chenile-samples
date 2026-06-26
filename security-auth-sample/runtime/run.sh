#!/usr/bin/env bash

set -e

BASE_DIR="$(dirname "$0")"
COMPOSE_FILE="$BASE_DIR/spring-auth/docker-compose.yml"

docker compose -f "$COMPOSE_FILE" up --build
