# p1 process deployment

This is the output of the Process Management blueprint with a trivial tweaking. 
## How to create it

```sh
jgen.sh -e oo -g chenile-process-management
# This generates a file called input-file.json under a folder called "oo"
cd oo
jgen.sh -f input-file.json
# This generates the process called p1 under a folder called output/p1
# You can make minor tweaks to take care of it. 
# Read below to see how you can start process-ui on it.
```

## Description 

This reactor contains one process server and one worker mini-monolith.

The registry endpoint is http://localhost:8000.

| Host | Deployment URL | Role |
| --- | --- | --- |
| p1Server | http://localhost:8000 | Process state, outbox, definitions REST, triggers and crontabs, service registry |
| p1Worker | http://localhost:8001 | Headless Kafka listener, process delegate and all worker JARs |


## Build and run

```sh
mvn install
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export PROCESS_MANAGEMENT_API_KEY=$(openssl rand -hex 32)
./run-server.sh OR
java -jar p1Server/p1Server-package/target/p1Server-package-0.0.1-SNAPSHOT.jar
# In another terminal, after the server has registered processService:
./run-workers.sh OR
java -jar p1Worker/p1Worker-package/target/p1Worker-package-0.0.1-SNAPSHOT.jar
```

Start Kafka first. Create topics `p1-work`; the worker consumes all of them
using consumer group `p1-workers` (override with
`PROCESS_WORK_CONSUMER_GROUP`). Worker replicas share that group. Preserve
`x-chenile-tenant-id` in messages and callbacks.

The worker uses `process-delegate` and the service registry to discover
`processService`; the server deployment URL is advertised to that registry.
Its headless work operation has no HTTP route. The worker deployment URL describes
the host, not the message transport. No process datasource or outbox dispatcher
runs on the worker.

Each process definition has a worker JAR:
- `p1-batch-workers`: parent for `p1-batch-api` and `p1-batch-service`; its API owns `BatchIn` and `AggregatedResult&lt;ItemOutput&gt;`, while its service contains the splitter and typed aggregator.
- `p1-item-workers`: parent for `p1-item-api` and `p1-item-service`; its API owns `ItemIn` and `ItemOutput`, while its service contains the executor.
Implement the generated splitter/executor hooks before submitting real work.
They deliberately throw until application logic is supplied. Aggregators collect
typed child results and errors using `AggregatedResult<ChildOutput>` from
`process-delegate`, without defining an application-specific aggregate model.

The server owns `p1-server-config`, including definitions and optional
cron schedules. Bootstrap inserts missing records only; restarts preserve saved
administrator changes. Generated cron schedules remain disabled unless enabled
in the input specification.

## Persistence and configuration

The local default is an H2 file database with Hibernate schema update. For a
managed database, supply `PROCESS_DATABASE_URL`, `PROCESS_DATABASE_USERNAME`,
`PROCESS_DATABASE_PASSWORD`, and `PROCESS_SCHEMA_MODE` according to your schema
management policy. A separate registry runs its own datasource. Give it separate
database environment values when deploying it independently.

Override `SERVER_PORT` per host if the advertised deployment URL is fronted by a
reverse proxy. Update deployment URLs and regenerate when the registry must
advertise a different public endpoint.

The management API requires `PROCESS_MANAGEMENT_API_KEY`. That key protects its
administrator routes; configure your application's HTTP authentication for
public process, trigger, crontab and registry routes before exposing them.

Kafka publications wait for broker acknowledgement before the outbox command can
complete. The server uses Chenile's `chenile-kafka` publisher; configure
`PUBSUB_PUBLISH_WAIT_TIME_MS` to change the acknowledgement timeout (default
`30000` ms). A value of `0` disables waiting, following the publisher's
`pubsub.publish.waitTime` contract. Worker delivery failures remain retryable;
external business effects must still tolerate redelivery. The generated tests verify the REST contract,
committed-process publication to Kafka, broker delivery to the headless worker,
tenant restoration and failed-publication propagation. Run `mvn install` with
Docker available: the server and worker tests extend `KafkaBaseTest`, which
starts and stops Kafka through Testcontainers automatically. No external Kafka
server or test bootstrap-server configuration is needed. The deployment startup
instructions above apply when running the applications outside these tests.

The server initializes the runtime's idempotent outbox/receipt SQL schema;
Hibernate manages the process, definition and crontab entities in the local
default configuration. For an externally managed schema, disable automatic SQL
initialization after provisioning those tables and select schema validation.

With the default file-backed H2 database, the server exposes the H2 browser
console at `http://localhost:8000/h2-console` (JDBC URL:
`jdbc:h2:file:./.p1Server/data;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE`,
user `sa`, blank password). Set `PROCESS_H2_CONSOLE_ENABLED=false` when using an
externally managed database or before exposing the server outside local development.

## Process manager UI

After starting the server above, run the UI from the Chenile process-management
checkout in another terminal:

```sh
cd process-ui
npm ci
PROCESS_API_TARGET=http://localhost:8000 npm run dev
```

Open the Vite URL, enter tenant `default` and the same
`PROCESS_MANAGEMENT_API_KEY` exported for the server. The UI's development proxy
adds `/process-management/api`; `PROCESS_API_TARGET` should be the server origin,
without that path. If `definitionSource` is `json`, definitions are read-only in
the UI; use `database` when administrators need to create or edit definitions.
If a trigger trace returns 404, confirm the proxy target is the same generated
server that accepted the trigger and the UI tenant matches the trigger tenant.
An accepted trigger appears in the trace while pending, before Kafka dispatch
creates its process record.
