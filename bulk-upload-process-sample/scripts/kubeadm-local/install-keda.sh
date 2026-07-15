#!/usr/bin/env bash
set -euo pipefail

helm repo add kedacore https://kedacore.github.io/charts >/dev/null
helm repo update kedacore >/dev/null
helm upgrade --install keda kedacore/keda \
  --namespace keda \
  --create-namespace

kubectl -n keda rollout status deploy/keda-operator --timeout=180s
kubectl api-resources | grep -i scaledobject >/dev/null

echo "KEDA is installed and ScaledObject CRD is available"
