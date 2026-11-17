## Why

本地 Docker Desktop Kubernetes 集群压测目前只有 `metrics-server` 的即时资源查询和离线采样报告，缺少可回看、按 Pod 区分且能关联 JVM、HTTP 与中间件状态的实时指标曲线。需要在不增加集群内监控工作负载的前提下，为本地压测提供完整的集群外可观测能力，同时保留 k6 吞吐结果和现有实验报告作为独立的业务结果来源。

## What Changes

- 在宿主机以独立 Docker Compose 配置运行 Prometheus、Grafana、PostgreSQL exporter、Redis exporter 和宿主机 kube-state-metrics，持久化本地指标与 Dashboard，不进入 Kubernetes 集群，也不改变现有业务 Compose 服务。
- 为 API 与 Judge 引入 Micrometer Prometheus Registry，并仅在本地部署配置中开放 `/actuator/prometheus`；生产与其他共享环境的默认暴露范围保持不变。
- 配置外部 Prometheus 通过 Kubernetes API 服务发现及 API Server 的 Pod/Node 代理抓取应用、kubelet 与 cAdvisor 指标，避免通过 Service NodePort 随机抓取 API 副本，也不要求 Judge 改为 NodePort。
- 通过本地 NodePort 采集 RabbitMQ 原生指标（独立 15692 → 30692），并由集群外 exporter 采集 PostgreSQL 与 Redis 指标；监控凭据只从本地私有环境配置注入。
- 提供预置压测 Dashboard、目标健康检查和运行文档，展示 Pod CPU/内存/OOM/重启、HTTP 延迟与错误、JVM、RabbitMQ、PostgreSQL 和 Redis 核心曲线。
- 明确 k6、现有 JSONL/report 与 Grafana 的职责边界：本变更不替换、不吞并现有压测吞吐指标或实验报告，只使用统一时间范围辅助对照。

## Capabilities

### New Capabilities

- `local-external-observability`: 为本地 Docker Desktop Kubernetes 压测提供完全部署在集群外的指标采集、历史存储和可视化能力，包含按 Pod 抓取、监控访问安全、核心中间件指标及运行验收。

### Modified Capabilities

无。现有集群管理、压测场景、吞吐结果和离线报告的需求保持不变。

## Impact

- 后端：`src/backend/pom.xml`、`src/judge-service/pom.xml` 及 Actuator/本地安全配置；新增 `micrometer-registry-prometheus` 运行时依赖。
- 本地部署：`deploy/charts/bluenet/values/local/api.yaml` 与 `judge.yaml` 的指标暴露配置；新增专用监控 ServiceAccount/RBAC 清单或部署脚本。
- 运维配置：新增独立的本地监控 Compose、Prometheus、Grafana provisioning、环境变量示例和文档；Prometheus/Grafana 使用新的宿主机端口与数据卷。
- 压测：`src/load-test` 的 k6 场景、采样器和报告格式不变，仅补充与 Grafana 时间范围对照的使用说明。
- 安全：新增本地只读 Kubernetes 监控身份及 `pods/proxy`、`nodes/proxy` 权限；私钥、令牌、数据库密码和 Grafana 管理密码不得入库，监控 endpoint 不得默认暴露到生产或公网。
- 验证：需要 Maven 测试/构建、Helm 渲染、Compose 配置校验和真实本地 Docker Desktop Kubernetes 压测链路验证；kubelet/cAdvisor 经 API Server 代理的兼容性是首个技术验证点。
