# BlueNet Judge Service Dockerfile
# 使用预编译的 judge-service jar 文件，不在 Docker 中编译业务代码。
# 构建上下文应为项目根目录。

# judge-service 需要在容器内编译/运行多语言代码，不能使用 JRE-only 镜像。
# isolate 依赖 Linux namespaces/cgroups，实际判题镜像使用 Debian/Ubuntu 系 JDK 镜像更合适。
FROM eclipse-temurin:21-jdk
LABEL authors="IVEN"
LABEL description="BlueNet Judge Service"

# isolate 源码版本；升级时需要同步验证 isolate-check-environment 和实际判题行为。
ENV ISOLATE_VERSION=2.2.1

# 安装 judge worker 基础运行依赖：
# - curl/ca-certificates：下载 isolate 源码包。
# - make/gcc/pkg-config/libcap-dev/libseccomp-dev/libsystemd-dev：编译 isolate。
# - g++/python3/nodejs：候选代码、标准解和 generator 的基础语言运行环境。
RUN apt-get update \
  && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    make \
    gcc \
    g++ \
    pkg-config \
    libcap-dev \
    libseccomp-dev \
    libsystemd-dev \
    python3 \
    nodejs \
  && rm -rf /var/lib/apt/lists/*

# 编译并安装 isolate。安装后 isolate 会作为 setuid-root 工具运行，业务进程仍使用 appuser。
RUN curl -fsSL "https://mj.ucw.cz/download/isolate/isolate-${ISOLATE_VERSION}.tar.gz" -o /tmp/isolate.tar.gz \
  && mkdir -p /tmp/isolate \
  && tar -xzf /tmp/isolate.tar.gz -C /tmp/isolate --strip-components=1 \
  && make -C /tmp/isolate isolate isolate-check-environment \
  && make -C /tmp/isolate install \
  && rm -rf /tmp/isolate /tmp/isolate.tar.gz

# 创建非 root 应用用户；isolate 自身通过 setuid 处理沙箱隔离权限。
RUN groupadd --system appgroup \
  && useradd --system --gid appgroup --home-dir /app --shell /usr/sbin/nologin appuser

# Judge Service HTTP 端口。
ENV JUDGE_SERVER_PORT=8090
# JDWP 远程调试端口，IDEA 使用 localhost:5005 attach。
ENV JUDGE_DEBUG_PORT=5005
# 是否在 JVM 启动时等待调试器连接；本地调试可设为 y。
ENV JUDGE_DEBUG_SUSPEND=n

# PostgreSQL 连接配置；数据库迁移仍由 backend 服务负责。
ENV DATABASE_HOST=localhost
ENV DATABASE_PORT=5432
ENV DATABASE_NAME=db_blue_net
ENV DATABASE_USERNAME=postgres
ENV DATABASE_PASSWORD=000000

# RabbitMQ 连接配置；judge-service 只消费 backend 发布的判题任务 ID。
ENV RABBITMQ_HOST=localhost
ENV RABBITMQ_PORT=5672
ENV RABBITMQ_USERNAME=guest
ENV RABBITMQ_PASSWORD=

# 对象存储 provider，可选值：minio、aliyun-oss。
ENV STORAGE_PROVIDER=minio
# MinIO 连接配置；通常与 backend 使用同一个 MinIO 服务。
ENV MINIO_ENDPOINT=localhost
ENV MINIO_PORT=9000
ENV MINIO_AK=admin
ENV MINIO_SK=admin123
ENV MINIO_USE_SSL=false

# 判题资产专用 bucket，和主应用业务文件 bucket 隔离。
ENV JUDGE_STORAGE_BUCKET=bluenet-judge
# Linux 沙箱引擎，可选值：isolate、nsjail。
ENV JUDGE_SANDBOX_ENGINE=isolate
# 判题 worker 临时工作目录根路径。
ENV JUDGE_WORK_ROOT=/var/lib/bluenet-judge/work
# 容器默认字符集，避免日志或源码内容乱码。
ENV LANG=C.UTF-8

WORKDIR /app

COPY src/judge-service/target/*.jar /app/judge-service.jar

RUN mkdir -p /var/lib/bluenet-judge/work

EXPOSE 8090
EXPOSE 5005

ENTRYPOINT ["sh", "-c", "exec java -Dfile.encoding=UTF-8 -Xms256m -Xmx384m -XX:+UseSerialGC -agentlib:jdwp=transport=dt_socket,server=y,suspend=${JUDGE_DEBUG_SUSPEND},address=*:${JUDGE_DEBUG_PORT} -jar /app/judge-service.jar"]
