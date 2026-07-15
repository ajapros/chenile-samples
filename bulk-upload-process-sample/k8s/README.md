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
kubectl -n chenile-bulk-upload get deploy,pods,scaledobject
kubectl -n chenile-bulk-upload logs deploy/bulk-upload-worker
```

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
