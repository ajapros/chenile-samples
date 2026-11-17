# Chenile Scheduler Sample

This sample shows Chenile's event-based scheduling (Chenile 2.1.36):

- **A schedule fires an event.** `schedules/daily-report.json` fires the `scheduledReport` event every 30 seconds
  through chenile-scheduler. Nothing names a service or operation.
- **A service subscribes to the event.** `ScheduledReportController.generate` carries
  `@EventsSubscribedTo("scheduledReport")`, so the same operation runs on `POST /scheduled-report/generate` and on
  every fire. It enqueues an idempotent work item (`scheduled-report:<report>:<date>`) in Postgres or H2.
- **Workers do the work.** The `worker` profile claims and processes work items; in Kubernetes, KEDA scales the
  workers on the backlog, or a CronJob runs them.

Each fire goes through the TriggerService: in this sample it is chenile-trigger's in-memory DirectTriggerService,
which deduplicates by `schedule:<id>:<scheduled fire time>`. Running several scheduler nodes needs clustered Quartz
(`spring.quartz.job-store-type=jdbc`), so that each fire happens on one node.

## Run with H2

```bash
mvn spring-boot:run
```

The schedule fires every 30 seconds and the log shows `Scheduled report work item enqueued` once, then
`already-exists` for the same report and date.

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

- `src/main/resources/org/chenile/samples/scheduler/schedules/daily-report.json`
- `src/main/java/org/chenile/samples/scheduler/configuration/controller/ScheduledReportController.java`
- `k8s/cron-workers/10-scheduled-report-cronjob.yaml`
- `k8s/scaling/scheduler-hpa.yaml`
- `k8s/scaling/keda-postgres-worker-scaledobject.yaml`
- `workers/scheduled-report-worker/run.sh`
