# jvm-runtime-memory Specification

## Purpose

定义 API 与 Judge 的默认 JVM 堆配置、运行参数覆盖方式、Judge 远程调试开关及本地容器内存兜底，确保低风险静默内存调优可验证、可回滚。

## Requirements

### Requirement: 默认 JVM 堆配置

API 与 Judge 镜像 SHALL 默认以 64MiB 初始堆、384MiB 最大堆和 SerialGC 启动，且 MUST NOT 在本变更中降低原有最大堆容量。

#### Scenario: 使用镜像默认参数启动

- **WHEN** 操作员未覆盖 `JAVA_OPTS` 并启动 API 或 Judge 容器
- **THEN** Java 进程使用 `-Xms64m -Xmx384m -XX:+UseSerialGC`

### Requirement: JVM 参数可覆盖

API 与 Judge 容器 SHALL 支持通过 `JAVA_OPTS` 环境变量覆盖默认 JVM 参数，以便无需重建镜像即可调参或回滚。

#### Scenario: 操作员覆盖 JVM 参数

- **WHEN** 操作员为容器设置自定义 `JAVA_OPTS` 并启动服务
- **THEN** Java 进程使用自定义参数而不是镜像中的默认堆参数

### Requirement: Judge 调试代理显式启用

Judge SHALL 默认不加载 JDWP 调试代理，并 SHALL 仅在 `JUDGE_DEBUG_ENABLED=true` 时加载；开启时 MUST 沿用现有调试端口和 suspend 配置。

#### Scenario: 默认启动 Judge

- **WHEN** 未设置 `JUDGE_DEBUG_ENABLED` 并启动 Judge
- **THEN** Java 进程不包含 JDWP agent 且不监听 JDWP 调试端口

#### Scenario: 显式开启 Judge 调试

- **WHEN** 设置 `JUDGE_DEBUG_ENABLED=true` 并启动 Judge
- **THEN** Java 进程按 `JUDGE_DEBUG_PORT` 和 `JUDGE_DEBUG_SUSPEND` 加载 JDWP agent

### Requirement: 本地容器内存兜底

Docker Compose SHALL 为 API 设置 640MiB 内存硬上限，为 Judge 设置 1GiB 内存硬上限；该限制 MUST NOT 被解释为本阶段已完成生产容量压测。

#### Scenario: 通过 Compose 创建服务

- **WHEN** 操作员使用 Compose 创建 API 和 Judge 容器
- **THEN** API 容器内存上限为 640MiB，Judge 容器内存上限为 1GiB

### Requirement: 服务可用性保持

完成运行参数调整后，API 与 Judge MUST 保持现有启动和健康检查行为。

#### Scenario: 调整参数后的服务启动

- **WHEN** 使用新镜像重新创建 API 和 Judge 容器
- **THEN** 两个容器启动成功且现有健康接口返回成功
