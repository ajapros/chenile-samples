# Chenile Bulk Upload Process Sample

This sample shows a production-style bulk upload flow with Chenile process-manager:

- uploaded CSV is stored in object storage
- upload/process metadata is stored in Postgres
- the root process splits the file into chunk subprocesses
- workers claim durable work from `chenile_process_work_item`
- chunk executors store row-level results idempotently
- the aggregator stores final counts and a summary object
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

curl -F "file=@/tmp/bulk-upload.csv" "http://localhost:8095/bulk-uploads?chunkSize=2"
```

Check status:

```bash
curl http://localhost:8095/bulk-uploads/{uploadId}
curl http://localhost:8095/bulk-uploads/{uploadId}/processes
```

Inspect worker backlog:

```bash
psql "postgresql://chenile:chenile@localhost:55432/bulk_upload" \
  -c "select process_id, worker_type, status, attempt from chenile_process_work_item order by created_at"
```

## Worker Scaling Model

The application pod only accepts uploads and enqueues process work. Worker pods run with the `worker` profile and continuously claim rows from `chenile_process_work_item`.

Workers are safe to scale horizontally because:

- each process work item has a deterministic idempotency key
- duplicate enqueues are ignored by the database
- workers claim rows with `FOR UPDATE SKIP LOCKED`
- failed work returns to `PENDING` until `max-attempts`
- row results are keyed by `upload_id + line_number`

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
