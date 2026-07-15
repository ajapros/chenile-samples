#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
IMAGE="${IMAGE:-chenile/bulk-upload-process-sample:local}"

cd "$ROOT_DIR"
mvn test package
docker build -t "$IMAGE" .

nodes="$(kubectl get nodes -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}')"
if [[ -z "$nodes" ]]; then
  echo "No Kubernetes nodes found. Run preflight first." >&2
  exit 1
fi

archive="$(mktemp -t bulk-upload-image.XXXXXX.tar)"
cleanup() {
  rm -f "$archive"
}
trap cleanup EXIT

docker save "$IMAGE" -o "$archive"

for node in $nodes; do
  echo "Importing $IMAGE into kubeadm node runtime on $node"
  if command -v ctr >/dev/null 2>&1; then
    sudo ctr -n k8s.io images import "$archive"
  else
    echo "ctr is not available. Install containerd tools or import $archive manually into node $node." >&2
    exit 1
  fi
done

echo "Built and imported $IMAGE"
