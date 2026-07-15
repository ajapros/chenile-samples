#!/usr/bin/env bash
set -euo pipefail

required=(docker kubectl kubeadm helm curl jq)
for tool in "${required[@]}"; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "Missing required tool: $tool" >&2
    exit 1
  fi
done

kubeadm version -o short
docker version --format 'Docker server {{.Server.Version}}'
kubectl version --client=true
helm version --short

context="$(kubectl config current-context)"
if [[ -z "$context" ]]; then
  echo "No current kubectl context is configured" >&2
  exit 1
fi
echo "Using kubectl context: $context"

kubectl cluster-info
kubectl wait --for=condition=Ready nodes --all --timeout=120s
kubectl get nodes -o wide

echo "kubeadm-local preflight passed"
