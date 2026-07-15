#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
IMAGE="${IMAGE:-chenile/bulk-upload-process-sample:local}"

cd "$ROOT_DIR"
mvn test package
docker build -t "$IMAGE" .
docker image inspect "$IMAGE" --format 'Built {{.Id}} size={{.Size}}'

echo "Built $IMAGE for Docker Desktop Kubernetes"
