# BlueNet API Service Dockerfile
# 使用预编译的 jar 文件，不在 Docker 中编译
# 构建上下文应为 src/backend 目录

FROM eclipse-temurin:21-jre-alpine
LABEL authors="IVEN"
LABEL description="BlueNet API Service"

RUN apk add --no-cache curl

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

ENV DATABASE_HOST=localhost
ENV DATABASE_PORT=5432
ENV DATABASE_NAME=db_blue_net
ENV DATABASE_USERNAME=postgres
ENV DATABASE_PASSWORD=
ENV RABBITMQ_HOST=localhost
ENV RABBITMQ_PORT=5672
ENV RABBITMQ_USERNAME=guest
ENV RABBITMQ_PASSWORD=
ENV MAIL_USERNAME=
ENV MAIL_PASSWORD=
ENV MINIO_AK=admin
ENV MINIO_SK=admin123
ENV LANG C.UTF-8
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms64m -Xmx384m -XX:+UseSerialGC"

WORKDIR /app

COPY target/*.jar /app/backend.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod 0555 /app/docker-entrypoint.sh

EXPOSE 8080

USER appuser

ENTRYPOINT ["/app/docker-entrypoint.sh"]
