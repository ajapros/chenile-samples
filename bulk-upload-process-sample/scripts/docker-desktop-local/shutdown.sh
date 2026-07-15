#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${NAMESPACE:-chenile-bulk-upload}"
REMOVE_KEDA="${REMOVE_KEDA:-false}"

echo "Deleting sample namespace: ${NAMESPACE}"
kubectl delete namespace "$NAMESPACE" --ignore-not-found

if [[ "$REMOVE_KEDA" == "true" ]]; then
  echo "Removing KEDA from local cluster"
  helm uninstall keda -n keda >/dev/null 2>&1 || true
  kubectl delete namespace keda --ignore-not-found
else
  echo "KEDA left installed. Set REMOVE_KEDA=true to remove local KEDA as well."
fi
