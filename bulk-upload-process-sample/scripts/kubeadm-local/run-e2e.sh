#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
NAMESPACE="${NAMESPACE:-chenile-bulk-upload}"
OUT_DIR="$ROOT_DIR/target/kubeadm-bulk-upload-audit"
mkdir -p "$OUT_DIR"

kubectl apply -f "$ROOT_DIR/k8s/base/00-namespace.yaml"
kubectl apply -k "$ROOT_DIR/k8s/kubeadm-local"
kubectl -n "$NAMESPACE" rollout status deploy/postgres --timeout=180s
kubectl -n "$NAMESPACE" rollout status deploy/minio --timeout=180s
kubectl -n "$NAMESPACE" rollout status deploy/bulk-upload-api --timeout=240s
kubectl -n "$NAMESPACE" get scaledobject bulk-upload-worker-postgres

kubectl -n "$NAMESPACE" port-forward svc/bulk-upload-api 8095:8095 >"$OUT_DIR/port-forward.log" 2>&1 &
port_forward_pid=$!
cleanup() {
  kill "$port_forward_pid" >/dev/null 2>&1 || true
}
trap cleanup EXIT
sleep 5

csv="$OUT_DIR/bulk-upload.csv"
cat > "$csv" <<'CSV'
one,ok
,bad
three,ok
ERROR,bad
five,ok
six,ok
seven,ok
eight,ok
CSV

curl -fsS -F "file=@${csv}" "http://localhost:8095/bulk-uploads?chunkSize=2&chunksPerGroup=2" \
  | tee "$OUT_DIR/upload-response.json"
upload_id="$(jq -r '.uploadId' "$OUT_DIR/upload-response.json")"
if [[ -z "$upload_id" || "$upload_id" == "null" ]]; then
  echo "Upload response did not contain uploadId" >&2
  exit 1
fi
echo "$upload_id" > "$OUT_DIR/upload-id.txt"

echo "Waiting for KEDA to scale workers"
for _ in {1..60}; do
  replicas="$(kubectl -n "$NAMESPACE" get deploy bulk-upload-worker -o jsonpath='{.status.replicas}' 2>/dev/null || true)"
  if [[ "${replicas:-0}" != "" && "${replicas:-0}" -gt 0 ]]; then
    break
  fi
  sleep 5
done

echo "Waiting for upload report completion: $upload_id"
for _ in {1..120}; do
  curl -fsS "http://localhost:8095/bulk-uploads/${upload_id}/report" > "$OUT_DIR/report.json"
  status="$(jq -r '.upload.status' "$OUT_DIR/report.json")"
  if [[ "$status" == "SUCCESS" || "$status" == "SUCCESS_WITH_ERRORS" ]]; then
    curl -fsS "http://localhost:8095/bulk-uploads/${upload_id}/audit" > "$OUT_DIR/audit.json"
    "$ROOT_DIR/scripts/kubeadm-local/collect-audit.sh" "$upload_id"
    echo "Upload completed with status: $status"
    exit 0
  fi
  sleep 5
done

echo "Upload did not complete in time: $upload_id" >&2
"$ROOT_DIR/scripts/kubeadm-local/collect-audit.sh" "$upload_id" || true
exit 1
