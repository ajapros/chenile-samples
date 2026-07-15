# Chenile Bulk Upload Process Sample

This sample shows a production-style bulk upload flow with Chenile process-manager:

- uploaded CSV is stored in object storage
- upload/process metadata is stored in Postgres
- the root process splits the file into group subprocesses
- each group process splits into chunk subprocesses
- workers claim durable work from `chenile_process_work_item`
- chunk executors store row-level results idempotently
- group and root aggregators store final counts, audit events, and a report
- KEDA scales workers from the Postgres backlog, not RabbitMQ

## Local Run

Build the app image:

```bash
mvn package
docker compose -f docker-compose.local.yml up --build
```

Upload a test file:

```bash
cat > /tmp/bulk-upload.csv <<'CSV'
one,ok
,bad
three,ok
ERROR,bad
CSV

curl -F "file=@/tmp/bulk-upload.csv" "http://localhost:8095/bulk-uploads?chunkSize=2&chunksPerGroup=2"
```

Check status:

```bash
curl http://localhost:8095/bulk-uploads/{uploadId}
curl http://localhost:8095/bulk-uploads/{uploadId}/processes
curl http://localhost:8095/bulk-uploads/{uploadId}/report
curl http://localhost:8095/bulk-uploads/{uploadId}/audit
```

Inspect worker backlog:

```bash
psql "postgresql://chenile:chenile@localhost:55432/bulk_upload" \
  -c "select process_id, worker_type, status, attempt from chenile_process_work_item order by created_at"
```

## Worker Scaling Model

The application pod accepts uploads and enqueues process work. Worker pods run with the `worker` profile and continuously claim rows from `chenile_process_work_item`.

The nested process tree is:

```text
bulkUpload
  bulkUploadGroup
    bulkUploadChunk
```

The root splitter creates deterministic group process IDs. Each group splitter creates deterministic chunk process IDs. This makes split retries safe and keeps the process tree easy to inspect.

Workers are safe to scale horizontally because:

- each process work item has a deterministic idempotency key
- duplicate enqueues are ignored by the database
- workers claim rows with `FOR UPDATE SKIP LOCKED`
- failed work returns to `PENDING` until `max-attempts`
- row results are keyed by `upload_id + line_number`
- group rows are keyed by `upload_id + group_number`

## Local Docker Desktop Kubernetes

The Docker Desktop path validates the same Docker image and Kubernetes manifests used by developers:

```bash
./scripts/docker-desktop-local/preflight.sh
./scripts/docker-desktop-local/build-image.sh
./scripts/docker-desktop-local/install-keda.sh
./scripts/docker-desktop-local/run-e2e.sh
```

The run writes the final report, audit API response, Postgres summaries, KEDA status, pod logs, and Kubernetes events to:

```text
target/docker-desktop-bulk-upload-audit/
```

Docker Desktop Kubernetes must be enabled and the current `kubectl` context must be `docker-desktop`.

## Local kubeadm Kubernetes

The kubeadm path is also available when `kubeadm` is installed and the current `kubectl` context points to the local kubeadm cluster:

```bash
./scripts/kubeadm-local/preflight.sh
./scripts/kubeadm-local/build-image.sh
./scripts/kubeadm-local/install-keda.sh
./scripts/kubeadm-local/run-e2e.sh
```

If the node runtime is containerd, `build-image.sh` imports `chenile/bulk-upload-process-sample:local` into `k8s.io` using `ctr`.

## Kubernetes

Apply base infrastructure and app manifests:

```bash
kubectl apply -f k8s/base/
```

Apply KEDA worker scaling:

```bash
kubectl apply -f k8s/scaling/
```

The KEDA scaler uses this backlog query:

```sql
select count(*)
from chenile_process_work_item
where status = 'PENDING'
   or (status = 'RUNNING' and locked_until < now())
```

DevOps owns namespace, credentials, image names, resource requests/limits, KEDA installation, managed Postgres, and object storage. Developers own splitter/executor/aggregator code and idempotent result writes.
