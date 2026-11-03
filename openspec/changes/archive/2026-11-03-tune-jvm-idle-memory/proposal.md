## Why

API 与 Judge 当前均以 `-Xms256m -Xmx384m` 启动，运行约两小时后静默内存分别约为 433MiB 和 390MiB；其中 Judge 堆实际仅使用约 40MiB，说明初始堆预留是静默内存偏高的重要原因。在降低最大堆或开展 Go 重构之前，需要先完成不降低业务容量的低风险调优并建立可复测基线。

## What Changes

- API 与 Judge 的默认 JVM 初始堆由 256MiB 降为 64MiB，最大堆继续保持 384MiB，GC 继续使用 SerialGC。
- 两个服务均支持通过 `JAVA_OPTS` 覆盖 JVM 参数，便于后续压测和回滚而无需修改镜像内容。
- Judge 的 JDWP 远程调试改为显式启用，默认不加载调试代理；启用时保留现有端口和 suspend 配置。
- Docker Compose 为 API 增加 640MiB、为 Judge 增加 1GiB 的宽松内存兜底，不改变现有 Kubernetes 资源限制。
- 通过镜像构建、容器重建、健康检查和运行参数检查验证行为；不调整连接池、线程池、最大堆或业务代码。

## Capabilities

### New Capabilities

- `jvm-runtime-memory`: 定义 API 与 Judge 的 JVM 初始/最大堆配置、可覆盖启动参数、Judge 调试开关和本地容器内存兜底要求。

### Modified Capabilities

<!-- 无既有 capability 的需求变更。 -->

## Impact

- `src/backend/Dockerfile`
- `src/judge-service/Dockerfile`
- `docker/docker-compose.yml`
- 本地 API/Judge 容器需要重建镜像并重新创建后生效。
- 不改变 REST API、数据库结构、业务逻辑及 Kubernetes 资源配置。
