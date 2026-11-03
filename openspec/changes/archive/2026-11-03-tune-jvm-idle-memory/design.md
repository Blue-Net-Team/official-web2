## Context

API 与 Judge 都在 Dockerfile 中固定使用 `-Xms256m -Xmx384m -XX:+UseSerialGC`。当前容器健康且无 OOM/重启，但静默内存分别约为 433MiB 和 390MiB。Judge 的堆已提交约 247MiB、实际使用约 40MiB，适合先降低初始堆而不缩减最大可用堆。

本变更只覆盖本地镜像与 Docker Compose。Kubernetes 已有 API/Judge 1Gi limit，本阶段不调整。

## Goals / Non-Goals

**Goals:**

- 降低服务启动及静默阶段的堆预留，保留原有 384MiB 最大堆容量。
- 允许通过 `JAVA_OPTS` 覆盖 JVM 参数，支持后续压测和快速回滚。
- 默认关闭 Judge JDWP，同时保留显式开启远程调试的能力。
- 为本地 Compose 容器增加宽松内存兜底，避免失控进程无限占用宿主机内存。

**Non-Goals:**

- 不降低 `Xmx`，不调整 GC 算法。
- 不调整 HikariCP、Tomcat、异步线程池或 RabbitMQ 并发。
- 不开展压力测试，不承诺压测后的最终内存目标。
- 不修改业务代码、Kubernetes manifests 或 Native Image 构建。

## Decisions

### 使用 `JAVA_OPTS` 统一 JVM 参数入口

两个镜像默认设置 `JAVA_OPTS=-Xms64m -Xmx384m -XX:+UseSerialGC`，入口通过 shell 展开该变量后启动 JAR。与继续硬编码参数相比，后续调参只需重建容器，不必重新构建镜像。`JAVA_OPTS` 是容器操作员受控输入，不接收外部请求数据。

### 保留 SerialGC 和原最大堆

本阶段的目标是减少初始提交内存，而非改变运行时容量或吞吐特性，因此继续使用 SerialGC 和 384MiB 最大堆。更换 G1 或直接降低 `Xmx` 留待压测阶段。

### JDWP 采用显式开关

新增 `JUDGE_DEBUG_ENABLED`，仅当值为 `true` 时附加 JDWP agent。默认启动不开放调试监听；开启后沿用 `JUDGE_DEBUG_PORT` 和 `JUDGE_DEBUG_SUSPEND`。不对其他真值做隐式扩展，避免配置含义模糊。

### Compose 仅设置宽松硬上限

API 设置 `mem_limit: 640m`，高于当前约 433MiB 的静默值并保留峰值空间；Judge 设置 `mem_limit: 1g`，为 JVM、编译器、语言运行时和沙箱子进程保留空间。两者都不设置较小的 reservation，以免把本轮静默调优误当成容量规划。

## Risks / Trade-offs

- [shell 展开 `JAVA_OPTS` 会带来参数分词要求] → 仅支持以空格分隔的普通 JVM 参数，并通过实际 `VM.command_line` 验证；含空格的复杂参数不在本阶段支持。
- [降低 `Xms` 后业务增长时会发生堆扩展和 GC] → 保留原最大堆并观察健康状态、日志和内存；这不降低最大容量。
- [默认关闭 JDWP 会改变原有调试习惯] → 通过 `JUDGE_DEBUG_ENABLED=true` 可恢复原行为，并在验证中覆盖。
- [640MiB/1Gi 硬限制尚未经过压测] → 仅作为本地宽松兜底；若出现 OOMKilled，立即提高 limit 或回滚，不将其作为生产容量结论。

## Migration Plan

1. 记录变更前 `docker stats`、容器健康状态和 Judge JVM 数据作为基线。
2. 构建 API 与 Judge 镜像并重新创建 Compose 服务。
3. 验证健康接口、JVM 实际参数、JDWP 默认监听状态和容器内存限制。
4. 观察启动及空闲内存，记录结果；出现异常时将 `JAVA_OPTS` 恢复为 `-Xms256m -Xmx384m -XX:+UseSerialGC` 并重建容器。
5. 后续压力测试另开变更，再决定是否降低 `Xmx`、连接池或容器 limit。

## Open Questions

- 调优后的稳定空闲内存需要容器运行一段时间后复测。
- 640MiB 的 API 本地兜底是否覆盖真实峰值，需要后续压测回答。
