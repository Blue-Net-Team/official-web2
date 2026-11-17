## Context

本地五节点 kind 使用 Helm values 部署两个 API 副本、一个 Judge 副本及 PostgreSQL、Redis、RabbitMQ。`metrics-server` 只提供即时资源数据；API/Judge 虽有 Actuator，却没有 Prometheus 格式端点。现有 `src/load-test` 负责 k6 业务负载和 Docker Compose 压测的离线 JSONL/report，两者继续独立使用。

本变更严格采用集群外方案：Prometheus、Grafana、exporter、历史数据和 Dashboard 全部运行在宿主机，kind 内不新增监控 Pod、DaemonSet 或 sidecar。仅新增专用监控身份和本地应用指标端点。宿主机为 Windows/Docker Desktop 时，外部容器通过 `host.docker.internal` 访问宿主机映射的 kind API Server 与既有 NodePort。

## Goals / Non-Goals

**Goals:**

- 以 Pod 为维度持续保存 API/Judge 的应用与资源指标，避免 Service NodePort 在副本间随机负载均衡。
- 在同一 Grafana Dashboard 展示资源、HTTP、JVM、RabbitMQ、PostgreSQL、Redis 及采集目标健康状态。
- 复用现有 API Server 与 NodePort，不为 Judge 增加公网或 NodePort 入口，不改变业务流量路径。
- 本地监控配置可重复部署、可持久化、可停止和可清理，敏感配置不入库。
- 保持 k6 吞吐结果和现有压测报告契约不变。

**Non-Goals:**

- 不在 kind 内运行 Prometheus、Grafana、Alertmanager、kube-state-metrics、node-exporter 或任何监控 Agent。
- 不建设生产监控、公网监控、日志平台、分布式 Trace 或完整 OpenTelemetry 体系。
- 不替换 `metrics-server`、k6、现有采样器或离线报告，不将 k6 指标导入 Grafana。
- 不实现告警通知、容量承诺、自动调参或跨环境统一监控。
- 不改变 API/Judge 业务 API、RabbitMQ 拓扑、数据库结构及现有生产 Helm values。

## Decisions

### 1. 使用独立宿主机监控 Compose

新增独立于 `docker/docker-compose.yml` 的监控编排，包含 Prometheus、Grafana、postgres-exporter、redis-exporter。Grafana 数据源和 Dashboard 采用 provisioning，Prometheus 与 Grafana 使用独立命名数据卷，监控 profile 不与业务 profile 混用。

选择 Compose 而非手工安装，是为了固定镜像版本、配置挂载、数据卷和启停方式。宿主机端口仅绑定本机回环地址；若 Docker Desktop 不支持所需绑定方式，则文档必须明确仅用于可信本机。

### 2. 外部 Prometheus 通过 API Server 代理按 Pod 抓取

Prometheus 使用专用 ServiceAccount 的令牌和集群 CA，通过 `kubernetes_sd_configs` 发现 Pod 与 Node。应用目标的抓取地址重写为：

```text
https://<kind-api-server>/api/v1/namespaces/<namespace>/pods/<pod>:<management-port>/proxy/actuator/prometheus
```

节点资源与容器指标分别通过 `nodes/<node>/proxy/metrics/resource` 和 `nodes/<node>/proxy/metrics/cadvisor` 抓取。保留 `namespace`、`pod`、`node` 等标签，并按服务、端口和暴露注解筛选目标。

不采用直接 Pod IP，因为宿主机通常无法路由 kind 容器网络；不采用 Service NodePort，因为两个 API 副本的 JVM 曲线会混杂；不采用长期 `kubectl port-forward`，因为目标漂移和进程退出会破坏持续采集。

Docker Desktop 下 API Server 地址不能硬编码为 `6443` 或容器内的 `127.0.0.1`。配置生成流程读取当前 kind kubeconfig 的实际映射端口，以 `host.docker.internal` 为连接地址；TLS 使用集群 CA，并通过显式 `server_name` 匹配 kubeconfig 中的 API Server 名称。禁止把关闭 TLS 校验作为默认方案。

### 3. API/Judge 使用独立本地管理端口

两个服务增加 `micrometer-registry-prometheus`，默认配置仍只暴露原有端点。本地 values 设置：

- API：管理端口 `8081`，暴露 `health,prometheus`；业务探针继续走 `8080`。
- Judge：管理端口 `8091`，暴露 `health,info,metrics,prometheus`；业务探针继续走 `8090`。
- 管理端口只声明为容器端口，不创建 Service/NodePort，由 API Server Pod Proxy 访问。

独立管理端口把监控流量与业务认证/限流边界分开。实现必须用集成测试验证 `/actuator/prometheus` 可抓取且业务端口不能访问该端点；如果现有 Spring Security 过滤器仍拦截管理端口，只允许增加本地 profile 的精确管理端点规则，禁止全局匿名开放 `/actuator/**`。

### 4. 最小化集群内 RBAC，不部署采集器

为本地监控创建专用 ServiceAccount、ClusterRole 和绑定，权限仅覆盖服务发现所需的 Pods/Nodes/Services/Endpoints 读取，以及 `pods/proxy`、`nodes/proxy` 访问。角色不授予 Secret、工作负载修改或业务资源写权限。

令牌通过本地私有挂载提供给 Prometheus；仓库只提供无秘密的 RBAC 模板、配置生成流程和 `.env.example`。`nodes/proxy` 能访问节点诊断端点，因此该身份仅用于一次性本地 kind，不复用到测试或生产集群。

### 5. 中间件使用现有 NodePort 与集群外 exporter

- RabbitMQ：直接抓 `host.docker.internal:30672/metrics`，优先使用 RabbitMQ 内置 Prometheus 插件，不再增加 exporter。
- PostgreSQL：宿主机 postgres-exporter 经 `host.docker.internal:30432` 连接，使用仅具监控所需权限的独立账号。
- Redis：宿主机 redis-exporter 经 `host.docker.internal:30379` 连接，密码由本地私有环境变量注入。
- Prometheus 再抓取两个 exporter 的容器内 `/metrics`。

这些入口均沿用本地 values 已有 NodePort，不新增中间件集群内工作负载。

### 6. Dashboard 服务于压测诊断，k6 保持独立

预置一个本地压测 Dashboard，包含：

- Pod CPU、memory working set、重启与 OOM 指标；
- API HTTP 请求速率、P95/P99、错误率，按 Pod 可拆分；
- API/Judge JVM Heap、Non-heap、GC、线程和直接缓冲区；
- RabbitMQ run/formal ready、unacked、投递/确认速率；
- PostgreSQL 连接、事务和锁，Redis 内存、客户端和命中率；
- 所有 scrape target 的 `up` 与最近抓取状态。

Grafana 与 k6 使用相同 UTC 时间窗口对照。Dashboard 不展示伪造的“总吞吐”面板，也不将 Prometheus 的应用请求计数冒充 k6 的有效业务闭环数。

## Risks / Trade-offs

- [API Server 代理增加控制面负载和权限面] → 仅用于本地五节点 kind；抓取间隔初始为 15 秒，只发现带监控标记的目标；使用专用身份并在清理流程中删除 RBAC。
- [kind/Docker Desktop 的 kubeconfig 地址、TLS SAN 或代理路径存在版本差异] → 将实现前的连通性 spike 作为首要任务，分别验证 Pod、resource、cAdvisor 路径；失败时保留明确诊断，不静默退回 NodePort 混采或关闭 TLS。
- [独立管理端口仍受应用 Security 过滤器影响] → 以本地 profile 的精确规则和集成测试处理，不修改生产白名单，不开放全部 Actuator。
- [Micrometer HTTP 标签导致高基数] → 使用框架默认 URI 模板，禁止加入用户 ID、题目 ID、任务 ID、完整 URL 等标签；Dashboard 优先按服务、方法和 URI 模板聚合。
- [外部 exporter 经 NodePort 引入连接与凭据风险] → 使用本地监控账号、私有 env 文件和最小权限；样例不包含真实密码，Compose 不输出敏感环境值。
- [监控本身影响压测结果] → 监控组件位于集群外但仍共享宿主机资源；记录抓取间隔与版本，压测时观察监控容器资源，对照实验保持同一监控配置。
- [kubelet 的 working set 与 JVM Heap、容器 RSS 含义不同] → Dashboard 和文档分别命名，不用 Heap 回落推断容器内存回落或泄漏。

## Migration Plan

1. 先验证当前 kind API Server 代理和节点指标兼容性，再引入应用 Prometheus 端点。
2. 构建并部署本地 API/Judge 镜像，确认业务探针、认证和现有压测不受影响。
3. 创建本地监控 RBAC 和私有凭据，启动外部 Compose，逐个启用应用、节点和中间件 target。
4. 运行 preflight 与短压测，核对所有 target、按 Pod 曲线、Grafana 时间窗口及 k6 报告。
5. 回滚时停止并删除监控 Compose、数据卷（仅在明确不需要历史数据时）和监控 RBAC，撤销本地管理端口 env 并恢复先前本地镜像。应用默认配置未开放 Prometheus，因此无需修改生产环境。

## Open Questions

- 当前 kind 与 Docker Desktop 版本是否允许外部 Prometheus 使用带 CA/`server_name` 的 API Server 地址稳定连接，需以实际 spike 结果确认。
- 当前 RabbitMQ 镜像是否默认启用聚合 `/metrics`；若未启用，只允许在本地 RabbitMQ 配置中显式启用对应插件，不引入集群内 exporter。
- PostgreSQL 监控账号的初始化采用一次性本地命令还是既有初始化流程，实施时选择不污染业务迁移和压测重置数据的方式。
