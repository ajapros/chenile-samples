# Kubernetes Setup

This setup is intentionally DB/KEDA based. RabbitMQ is not used for scaling.

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
