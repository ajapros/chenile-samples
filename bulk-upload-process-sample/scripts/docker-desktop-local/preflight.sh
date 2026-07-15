#!/usr/bin/env bash
set -euo pipefail

required=(docker kubectl helm curl jq)
for tool in "${required[@]}"; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "Missing required tool: $tool" >&2
    exit 1
  fi
done

context="$(kubectl config current-context)"
if [[ "$context" != "docker-desktop" ]]; then
  echo "Expected kubectl context docker-desktop but found: $context" >&2
  exit 1
fi

docker version --format 'Docker server {{.Server.Version}}'
kubectl version --client=true
helm version --short
kubectl cluster-info
kubectl wait --for=condition=Ready nodes --all --timeout=120s
kubectl get nodes -o wide

echo "Docker Desktop Kubernetes preflight passed"
