# Chenile Scheduler Sample

This sample shows two production-style scheduler integrations:

- JDBC execution history with local Chenile service execution.
- Kubernetes-native CronJob workers owned by DevOps.
- DB-backed KEDA scaling for idempotent Postgres work items.

The scheduler framework owns the generic trigger, dispatch, and execution-store contracts. In production Kubernetes, the preferred setup is to let Kubernetes own CronJobs and let worker images own the job implementation. Fabric8 is useful in tests or specialized dynamic-job launchers, but it is not the default production recommendation in this sample.

## Run with JDBC execution history

The `jdbc` profile uses:

- `chenile.scheduler.store.type=jdbc`
- H2 datasource
- `chenile-scheduler-schema.sql`
- default launcher `local`

Run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=jdbc
```

This mode executes `scheduledReportService.generate` in the same JVM and records execution status in `chenile_scheduler_job_execution`.

## Run locally with Postgres

The local Postgres setup uses:

- application port `18080`
- Postgres host port `55432`
- database `scheduler`
- username `scheduler`
- password `scheduler`

Start Postgres:

```bash
docker compose -f docker-compose.local.yml up -d postgres
```

Run the app against that database:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Check the scheduler execution table:

```bash
docker exec chenile-scheduler-sample-postgres \
  psql -U scheduler -d scheduler \
  -c "select execution_id, job_name, status, launcher, started_at, finished_at from chenile_scheduler_job_execution order by started_at desc limit 10;"
```

Check the DB-backed worker backlog:

```bash
docker exec chenile-scheduler-sample-postgres \
  psql -U scheduler -d scheduler \
  -c "select idempotency_key, status, attempt, locked_by, locked_until from chenile_scheduler_work_item order by created_at desc limit 10;"
```

## Kubernetes-native scheduled workers

The Kubernetes setup lives under `k8s/`.

Apply the sample manifests:

```bash
kubectl apply -f k8s/base/
```

Apply the Kubernetes CronJob worker path:

```bash
kubectl apply -f k8s/base/00-namespace.yaml
kubectl apply -f k8s/base/12-serviceaccounts.yaml
kubectl apply -f k8s/cron-workers/
```

Trigger one worker run manually:

```bash
kubectl -n chenile-scheduler-sample create job scheduled-report-manual \
  --from=cronjob/scheduled-report-worker
```

Read `k8s/README.md` for namespace, Postgres, scheduler deployment, CronJob worker, HPA, and KEDA ScaledJob setup.

## Files to read

- `src/main/resources/org/chenile/samples/scheduler/jobs/jdbc-report.json`
- `k8s/cron-workers/10-scheduled-report-cronjob.yaml`
- `k8s/scaling/scheduler-hpa.yaml`
- `k8s/scaling/keda-postgres-worker-scaledobject.yaml`
- `workers/scheduled-report-worker/run.sh`
