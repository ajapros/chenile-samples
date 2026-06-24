# Kubernetes Setup

This directory shows the production Kubernetes shape for scheduled work.

Use this setup when the platform owns scheduling and workers. Kubernetes creates CronJob executions, and the worker image owns the job implementation. Do not use an application-side Fabric8 launcher for production scheduling unless your platform explicitly requires the scheduler application to create Jobs dynamically.

## Ownership model

Developer responsibilities:

- implement the worker image
- define the worker environment variable contract
- make workers idempotent using the database `idempotency_key`
- expose useful logs and metrics
- document payload schema changes

DevOps responsibilities:

- own CronJob schedule, namespace, resource limits, RBAC, and rollout
- own database credentials and lifecycle
- configure horizontal scaling for long-running services
- configure KEDA PostgreSQL scaling for DB-backed workers
- set alerting for failed Jobs and stale scheduler executions

## Apply the JDBC scheduler stack

The base stack includes namespace, service accounts, Postgres for JDBC scheduler status, scheduler Deployment, and Service.

```bash
kubectl apply -f base/
```

This path runs the scheduler JSON from the application and stores execution status in Postgres.

## Apply Kubernetes-native CronJob workers

Apply this path when Kubernetes owns the cron schedule:

```bash
kubectl apply -f base/00-namespace.yaml
kubectl apply -f base/12-serviceaccounts.yaml
kubectl apply -f cron-workers/
```

Do not schedule the same business action in both the Chenile JDBC scheduler and a Kubernetes CronJob unless the action is explicitly idempotent.

Watch Jobs created by the CronJob:

```bash
kubectl -n chenile-scheduler-sample get cronjobs,jobs,pods
```

View worker logs:

```bash
kubectl -n chenile-scheduler-sample logs job/scheduled-report-manual
```

## Trigger one worker run manually

```bash
kubectl -n chenile-scheduler-sample create job scheduled-report-manual \
  --from=cronjob/scheduled-report-worker
```

## Scaling

Use `scaling/scheduler-hpa.yaml` for the long-running scheduler application. This requires the Kubernetes metrics server.

For backlog-driven worker execution, use the DB-backed worker setup:

```bash
kubectl apply -f scaling/scheduled-report-worker-deployment.yaml
kubectl apply -f scaling/keda-postgres-trigger-auth.yaml
kubectl apply -f scaling/keda-postgres-worker-scaledobject.yaml
```

The KEDA scaler polls Postgres and scales the worker Deployment from `0` to `10` replicas based on pending or expired work rows in `chenile_scheduler_work_item`.

The worker claim flow is idempotent and non-ambiguous:

- scheduler inserts one work row per `idempotency_key`
- duplicate inserts are rejected by the database unique constraint
- workers claim rows with `FOR UPDATE SKIP LOCKED`
- expired `RUNNING` rows can be reclaimed after `locked_until`

CronJob executions are not HPA-scaled. Use CronJob when Kubernetes owns the schedule and each schedule creates one Job. Use DB-backed KEDA scaling when a backlog should be processed by multiple worker replicas.

## Production notes

- Replace the sample Postgres manifest with a managed database in real clusters.
- Replace `example.com/chenile/scheduler-sample` and `example.com/chenile/scheduled-report-worker` with images from your registry.
- Keep CronJob `concurrencyPolicy: Forbid` unless the worker is designed for overlapping executions.
- Keep `startingDeadlineSeconds` finite so missed schedules do not flood the cluster after an outage.
- Keep `successfulJobsHistoryLimit` and `failedJobsHistoryLimit` low enough for cluster hygiene but high enough for operations.
- Grant KEDA read access only to the backlog query database, and keep the application write credentials separate when your platform supports it.
