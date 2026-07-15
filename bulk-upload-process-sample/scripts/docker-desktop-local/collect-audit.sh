#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
NAMESPACE="${NAMESPACE:-chenile-bulk-upload}"
UPLOAD_ID="${1:-$(cat "$ROOT_DIR/target/docker-desktop-bulk-upload-audit/upload-id.txt" 2>/dev/null || true)}"
OUT_DIR="$ROOT_DIR/target/docker-desktop-bulk-upload-audit"
mkdir -p "$OUT_DIR"

kubectl -n "$NAMESPACE" get all > "$OUT_DIR/k8s-all.txt" || true
kubectl -n "$NAMESPACE" get scaledobject,hpa > "$OUT_DIR/keda-summary.txt" || true
kubectl -n "$NAMESPACE" describe scaledobject bulk-upload-worker-postgres > "$OUT_DIR/scaledobject.txt" || true
kubectl -n "$NAMESPACE" get events --sort-by=.lastTimestamp > "$OUT_DIR/events.txt" || true
kubectl -n "$NAMESPACE" logs deploy/bulk-upload-api --tail=300 > "$OUT_DIR/api.log" || true
kubectl -n "$NAMESPACE" logs deploy/bulk-upload-worker --tail=500 > "$OUT_DIR/worker.log" || true

if [[ -n "$UPLOAD_ID" ]]; then
  kubectl -n "$NAMESPACE" exec deploy/postgres -- psql -U chenile -d bulk_upload \
    -c "select id,status,total_rows,success_rows,error_rows,result_object_key from bulk_upload_file where id='${UPLOAD_ID}'" \
    > "$OUT_DIR/db-upload.txt" || true
  kubectl -n "$NAMESPACE" exec deploy/postgres -- psql -U chenile -d bulk_upload \
    -c "select group_number,status,total_rows,success_rows,error_rows from bulk_upload_group where upload_id='${UPLOAD_ID}' order by group_number" \
    > "$OUT_DIR/db-groups.txt" || true
  kubectl -n "$NAMESPACE" exec deploy/postgres -- psql -U chenile -d bulk_upload \
    -c "select group_number,chunk_number,status,total_rows,success_rows,error_rows from bulk_upload_chunk where upload_id='${UPLOAD_ID}' order by group_number,chunk_number" \
    > "$OUT_DIR/db-chunks.txt" || true
  kubectl -n "$NAMESPACE" exec deploy/postgres -- psql -U chenile -d bulk_upload \
    -c "select worker_type,status,count(*) from chenile_process_work_item where process_id='${UPLOAD_ID}' or process_id like '${UPLOAD_ID}-%' group by worker_type,status order by worker_type,status" \
    > "$OUT_DIR/db-workers.txt" || true
  kubectl -n "$NAMESPACE" exec deploy/postgres -- psql -U chenile -d bulk_upload \
    -c "select event_type,worker_type,status,message,created_at from bulk_upload_audit_event where upload_id='${UPLOAD_ID}' order by created_at,id" \
    > "$OUT_DIR/db-audit.txt" || true
fi

echo "Audit artifacts written to $OUT_DIR"
