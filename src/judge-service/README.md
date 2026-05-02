# BlueNet Judge Service

`judge-service` is an independently runnable Spring Boot JAR for algorithm judging. It consumes RabbitMQ task ids, reads the current judge metadata from PostgreSQL, accesses judge-only OSS assets from the `bluenet-judge` bucket, and executes generator, standard solution, and candidate code through a Linux sandbox.

Database migrations are owned only by `src/backend`. This service must keep `spring.flyway.enabled=false` in every runtime profile and must not create or alter schema on startup.

Local Windows development can compile and run the Spring service, but sandbox execution requires Linux. Use WSL2 or a Linux host with `isolate` or `nsjail` installed before enabling real execution.

## Docker Debug

Build the jar before building the image:

```bash
mvn -pl src/judge-service -am -DskipTests package
```

Build the debug image from the repository root:

```bash
docker build -f docker/judge-service.Dockerfile -t bluenet-judge-service:debug .
```

Run the container and expose the Spring Boot port plus the JDWP debug port:

```bash
docker run --rm --name bluenet-judge-service \
  -p 8090:8090 \
  -p 5005:5005 \
  -e JUDGE_DEBUG_SUSPEND=y \
  -e DATABASE_HOST=host.docker.internal \
  -e DATABASE_PORT=15432 \
  -e DATABASE_NAME=db_blue_net \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=000000 \
  -e RABBITMQ_HOST=host.docker.internal \
  -e RABBITMQ_PORT=5672 \
  -e RABBITMQ_USERNAME=admin \
  -e RABBITMQ_PASSWORD=bluenet123 \
  -e STORAGE_PROVIDER=minio \
  -e MINIO_ENDPOINT=host.docker.internal \
  -e MINIO_PORT=7000 \
  -e MINIO_AK=admin \
  -e MINIO_SK=admin123 \
  -e MINIO_USE_SSL=false \
  -e JUDGE_STORAGE_BUCKET=bluenet-judge \
  bluenet-judge-service:debug
```

Attach from IntelliJ IDEA with a Remote JVM Debug configuration:

```text
Host: localhost
Port: 5005
Debugger mode: Attach to remote JVM
```
