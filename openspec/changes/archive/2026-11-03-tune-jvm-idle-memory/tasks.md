## 1. 配置契约与实现

- [x] 1.1 先编写并运行配置契约检查，确认当前文件不满足新默认值而失败（红灯）。测试边界：输入为两个 Dockerfile 与 Compose 配置；前置状态为旧启动参数；后置状态为检查能够验证默认堆参数、`JAVA_OPTS` 覆盖入口、JDWP 显式开关及容器内存上限。
- [x] 1.2 修改 API 与 Judge Dockerfile：默认 `JAVA_OPTS=-Xms64m -Xmx384m -XX:+UseSerialGC`，入口支持参数覆盖；Judge 仅在 `JUDGE_DEBUG_ENABLED=true` 时加载 JDWP，并沿用现有端口和 suspend 配置。
- [x] 1.3 修改 Docker Compose，为 API 设置 `mem_limit: 640m`，为 Judge 设置 `mem_limit: 1g`，不调整现有 Kubernetes 资源配置。
- [x] 1.4 重新运行配置契约检查及 Compose 配置解析，确认全部通过（绿灯），并审查差异不包含业务代码或无关变更。

## 2. 容器验证与基线记录

- [x] 2.1 构建 `bluenet-api-service:latest` 和 `bluenet-judge-service:latest`，通过 Compose 重新创建两个服务；前置状态为基础设施健康，后置状态为两个容器使用新镜像启动。
- [x] 2.2 验证默认 Java 实际参数为 `Xms64m/Xmx384m/SerialGC`，Judge 默认无 JDWP；再以临时覆盖配置验证 `JAVA_OPTS` 和 JDWP 显式开启路径，完成后恢复默认运行配置。
- [x] 2.3 验证 API/Judge 健康检查、容器 640MiB/1GiB 内存上限、无异常启动日志，并记录启动后及空闲一段时间后的 `docker stats`；若出现 OOM 或健康检查失败则按 design.md 回滚。
