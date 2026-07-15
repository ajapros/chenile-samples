# Kubernetes Setup

This setup is intentionally DB/KEDA based. RabbitMQ is not used for scaling.

## Local Docker Desktop Validation

Use the Docker Desktop scripts from the sample root when validating on local Docker Desktop Kubernetes:

```bash
./scripts/docker-desktop-local/preflight.sh
./scripts/docker-desktop-local/build-image.sh
./scripts/docker-desktop-local/install-keda.sh
./scripts/docker-desktop-local/run-e2e.sh
```

The Docker Desktop overlay is in `k8s/docker-desktop`. It reuses the production-style base/scaling manifests and changes the image to `chenile/bulk-upload-process-sample:local`.

Artifacts are written to `target/docker-desktop-bulk-upload-audit/`.

Stop the sample namespace:

```bash
./scripts/docker-desktop-local/shutdown.sh
```

## Local kubeadm Validation

Use the kubeadm scripts from the sample root when validating on a local kubeadm cluster:

```bash
./scripts/kubeadm-local/preflight.sh
./scripts/kubeadm-local/build-image.sh
./scripts/kubeadm-local/install-keda.sh
./scripts/kubeadm-local/run-e2e.sh
```

The kubeadm overlay is in `k8s/kubeadm-local`. It reuses the production-style base/scaling manifests and changes the image to `chenile/bulk-upload-process-sample:local`.

The end-to-end run captures:

- upload report API output
- audit API output
- Postgres upload/group/chunk/work-item summaries
- KEDA ScaledObject status
- Kubernetes events
- API and worker pod logs

Artifacts are written to `target/kubeadm-bulk-upload-audit/`.

Stop the sample namespace:

```bash
./scripts/kubeadm-local/shutdown.sh
```

## Apply Base Stack

```bash
kubectl apply -f base/
```

This creates:

- namespace
- application and worker service accounts
- sample Postgres
- sample MinIO
- API Deployment and Service

Replace sample Postgres/MinIO with managed services for real environments.

## Apply KEDA Scaling

```bash
kubectl apply -f scaling/
```

The worker Deployment starts at zero replicas. KEDA polls Postgres and scales workers based on pending or expired process work rows.

Watch scaling:

```bash
kubectl -n chenile-bulk-upload get deploy,pods,svc,scaledobject,hpa
kubectl -n chenile-bulk-upload logs deploy/bulk-upload-worker
```

The worker Deployment is expected to sit at zero replicas when there is no backlog. After an upload, KEDA polls Postgres and increases replicas while `chenile_process_work_item` has pending or expired rows. After processing finishes, the worker scales back to zero.

## Audit and Data Inspection

The E2E scripts collect all developer-facing evidence under `target/*-bulk-upload-audit/`:

- `report.json`: final API report with upload/group/chunk/row counts.
- `audit.json`: API audit timeline.
- `db-upload.txt`: upload status and final result object key.
- `db-groups.txt`: group-level aggregate status.
- `db-chunks.txt`: chunk-level aggregate status.
- `db-workers.txt`: process worker status by worker type.
- `db-audit.txt`: DB audit event timeline.
- `keda-summary.txt`: KEDA ScaledObject and HPA state.
- `events.txt`: Kubernetes event timeline.
- `api.log` and `worker.log`: application logs.

For manual database inspection:

```bash
kubectl -n chenile-bulk-upload exec deploy/postgres -- \
  psql -U chenile -d bulk_upload \
  -c "select worker_type,status,count(*) from chenile_process_work_item group by worker_type,status order by worker_type,status"
```

## Framework Boundary

Keep upload-specific code in the sample or product application. Chenile process-manager owns process persistence, durable work-item claiming, retries, subprocess notification, and aggregation triggers. The sample owns CSV parsing, upload/group/chunk tables, audit event content, report format, object storage, and Kubernetes runtime wiring.

If multiple products need the same operational read model, add a generic framework read API later for process backlog and worker summaries. Do not move this sample's upload schema or audit event vocabulary into the framework.

## Developer Contract

- Add process definitions in `bulk-upload-process-def.json`.
- Implement workers using Chenile `SplitterBase`, `ExecutorBase`, and `AggregatorBase`.
- Use deterministic child IDs when splitters can retry.
- Store result rows with unique business keys.
- Keep worker logic idempotent and safe to retry.

## DevOps Contract

- Build and publish the application image.
- Replace `example.com/chenile/bulk-upload-process-sample:latest`.
- Provide Postgres and object-store credentials via Secrets.
- Install KEDA and configure least-privilege DB access for scaler queries.
- Tune `minReplicaCount`, `maxReplicaCount`, resource requests, and lock timeout.
