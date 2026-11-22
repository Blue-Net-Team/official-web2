## 1. 本地集群连通性验证与现状保护

- [x] 1.1 记录当前 Docker Desktop、Kubernetes 和 RabbitMQ 版本，确认当前上下文确为目标 `docker-desktop`，并记录 API Server 实际映射地址、现有 NodePort 与工作负载基线。
- [x] 1.2 使用临时最小权限身份验证 API Server 的 Pod Proxy、`nodes/<node>/proxy/metrics/resource` 和 `nodes/<node>/proxy/metrics/cadvisor` 连通性、CA/`server_name` 与返回格式；验证后删除临时身份并记录结论，不退回关闭 TLS 或 NodePort 混采。
- [x] 1.3 验证 RabbitMQ 原生 Prometheus 指标端口（实际为 15692，本地 NodePort 30692），并确认 PostgreSQL `30432`、Redis `30379` 的宿主机访问路径；若 RabbitMQ 插件未启用，只调整本地配置。
- [x] 1.4 运行现有 API/Judge 测试、Helm 本地渲染和压测聚焦测试，保存变更前基线，确认后续可识别回归。

## 2. API 与 Judge Prometheus 端点（TDD）

- [x] 2.1 为 API 添加管理端口集成测试，覆盖本地配置下 `8081/actuator/prometheus` 返回 Prometheus 文本及 JVM/HTTP 指标、业务 `8080` 不暴露该端点、默认配置不暴露该端点。
- [x] 2.2 为 Judge 添加对应管理端口集成测试，覆盖本地 `8091` 可抓取、业务 `8090` 隔离及默认配置不暴露。
- [x] 2.3 在两个服务的 `pom.xml` 增加由 Spring Boot 管理的 `micrometer-registry-prometheus` 依赖，并以最小本地配置实现管理端口暴露，使新增测试通过。
- [x] 2.4 若 Security 过滤器拦截管理端口，增加仅限 `/actuator/prometheus` 精确路径的访问规则与测试；验证业务认证、权限扫描和其他 Actuator 白名单不变，禁止全局匿名开放 `/actuator/**`。
- [x] 2.5 检查 Micrometer HTTP 指标标签只使用方法、状态、异常和 URI 模板等受控维度，并增加测试防止完整 URL、用户/题目/任务 ID 进入标签。

## 3. 本地 Helm 与监控身份

- [x] 3.1 更新 `values/local/api.yaml` 和 `values/local/judge.yaml`，声明本地管理端口 env、容器端口和监控发现标签/注解，保持业务 Service、探针、副本和 Judge ClusterIP 不变，不修改生产 values。
- [x] 3.2 增加 Helm 渲染测试或静态校验，确认管理端口不生成 Service/NodePort，生产 values 不包含 Prometheus 暴露配置，本地 API 为一个副本。
- [x] 3.3 新增本地监控 ServiceAccount、ClusterRole/Binding 无秘密模板，最小化 Pods/Nodes/Services/Endpoints 读取及 `pods/proxy`、`nodes/proxy` 权限。
- [x] 3.4 实现带 `docker-desktop` 上下文显式确认的监控 RBAC/凭据初始化流程，从当前 kubeconfig 生成私有 token、CA 和 API Server 配置，禁止日志输出秘密并确保生成文件被 Git 忽略。
- [x] 3.5 使用 `kubectl auth can-i` 验证监控身份可访问所需代理但不能读取 Secret、创建/修改/删除工作负载，并验证非本地上下文会在变更前失败。

## 4. 宿主机监控编排与采集配置

- [x] 4.1 新增独立监控 Compose 与 `.env.example`，固定 Prometheus、Grafana、postgres-exporter、redis-exporter 镜像版本，配置回环地址端口、独立数据卷、健康检查和私有凭据引用，不接入业务 Compose profiles。
- [x] 4.2 配置 Prometheus 的 Kubernetes Pod/Node 服务发现、API Server 代理 relabel、CA 与 `server_name`，按本地监控标签和端口筛选 API/Judge，保留 namespace/pod/node 标签并设置初始 15 秒抓取间隔。
- [x] 4.3 配置 kubelet resource/cAdvisor、RabbitMQ 原生端点及两个宿主机 exporter 的 scrape job，所有目标不健康时均可明确诊断，配置中不得启用 `insecure_skip_verify`。
- [x] 4.4 提供 PostgreSQL 最小权限监控账号的本地初始化与回收说明/脚本，避免污染 Flyway 和业务压测初始化；Redis、RabbitMQ 与 Grafana 凭据通过未跟踪 env 注入。
- [x] 4.5 校验 Compose 与 Prometheus 配置，启动监控环境并确认 Kubernetes 集群内没有新增监控 Pod、DaemonSet 或 sidecar，重启后历史数据与 Grafana 配置仍存在。

## 5. Grafana 压测 Dashboard

- [x] 5.1 配置 Grafana Prometheus datasource 与 Dashboard provisioning，管理密码仅来自私有 env，数据源无需手工创建。
- [x] 5.2 添加 Pod CPU、memory working set、重启/OOM 和 scrape target 健康面板，按 namespace/pod/node 筛选并正确命名资源口径。
- [x] 5.3 添加 API HTTP QPS、P95/P99、错误率及 API/Judge JVM Heap、Non-heap、GC、线程、直接缓冲区面板，支持按 Pod 拆分且不冒充 k6 有效业务吞吐。
- [x] 5.4 添加 RabbitMQ run/formal ready、unacked、投递/确认速率及 PostgreSQL 连接/事务/锁、Redis 内存/客户端/命中率面板。
- [x] 5.5 校验 Dashboard JSON 和所有 PromQL，确保无流量与抓取失败可区分，时间范围按相同绝对时间对照且不包含高基数标签查询。

## 6. 文档、回归与端到端验收

- [x] 6.1 编写本地监控初始化、镜像构建部署、启动、目标健康检查、绝对时间对照、停止、保留数据清理及显式删除历史数据文档，明确清理不影响业务数据卷。
- [x] 6.2 更新 `src/load-test` 文档，仅说明 k6/JSONL/report 与 Grafana 的独立职责和时间对照方式，不修改 k6 场景、有效样本定义、采样文件或报告格式。
- [x] 6.3 执行 Maven 测试与打包、前端无关性检查、Helm 渲染、Compose/Prometheus/Dashboard 校验及现有压测聚焦测试，确认无业务契约和权限扫描回归。
- [x] 6.4 构建并部署本地 API/Judge 镜像，验证管理端点、业务探针和登录链路；启动外部监控，确认一个 API Pod、Judge、全部节点及中间件 target 健康。
- [x] 6.5 运行现有 preflight 和一轮短时算法压测，在相同绝对时间窗口核对 k6 报告、Pod 资源、HTTP/JVM 和 run/formal 队列曲线，并执行一次 API Pod 替换验证服务发现更新。
- [x] 6.6 验证未启动监控时原压测工具仍正常工作，执行保留数据的清理与恢复演练；最终检查 Git diff、生成文件忽略规则和日志，确保没有真实秘密或生产配置变更。
