## ADDED Requirements

### Requirement: 集群外监控运行环境

系统 SHALL 提供独立的宿主机监控编排，以运行 Prometheus、Grafana、PostgreSQL exporter 和 Redis exporter，并 MUST 将全部监控工作负载、历史指标和 Dashboard 数据保存在 kind 集群外。该编排 MUST NOT 修改或隐含启动现有业务 Compose 服务。

#### Scenario: 独立启动监控环境

- **WHEN** 操作者在已运行本地 kind 的宿主机启动监控编排
- **THEN** Prometheus、Grafana 和两个 exporter 在宿主机运行，kind 内没有新增监控 Pod、DaemonSet 或 sidecar，现有业务服务配置不发生变化

#### Scenario: 停止监控后保留历史数据

- **WHEN** 操作者停止监控容器后再次启动且未删除监控数据卷
- **THEN** 已保存的指标与预置 Dashboard 仍可访问

### Requirement: 按 Pod 的应用指标采集

API 与 Judge SHALL 提供 Prometheus 文本格式的 Actuator 指标端点，外部 Prometheus MUST 通过 Kubernetes API Server 的 Pod 代理分别抓取每个目标，MUST NOT 通过 Service NodePort 混合采集多个 API 副本，也 MUST NOT 为 Judge 新增 NodePort。

#### Scenario: 分别采集两个 API 副本

- **WHEN** 本地 kind 中运行两个带监控标记的 API Pod
- **THEN** Prometheus 为两个 Pod 分别建立目标并保存可区分的 Pod 标签，单副本 JVM 曲线不会与另一副本混合

#### Scenario: 采集 ClusterIP Judge

- **WHEN** Judge 仍只通过 ClusterIP 提供业务服务
- **THEN** 外部 Prometheus 能通过 API Server Pod 代理抓取其管理端口指标，且 Judge 不需要新增 NodePort

#### Scenario: Pod 被替换

- **WHEN** 一个受监控 Pod 被删除并由新 Pod 替代
- **THEN** 服务发现移除旧目标并发现新目标，新时间序列保留正确的 Pod 身份

### Requirement: 本地管理端点隔离

应用 Prometheus 端点 SHALL 只在本地部署配置中通过独立管理端口开放，业务端口 MUST NOT 提供该端点，生产和其他共享环境的默认 Actuator 暴露范围 MUST 保持不变。

#### Scenario: 本地管理端口可抓取

- **WHEN** 使用本地 values 启动 API 或 Judge
- **THEN** 对应管理端口提供 `/actuator/prometheus`，响应包含 JVM 和可用的 HTTP 指标，现有业务探针继续沿用原端口

#### Scenario: 业务端口不暴露监控端点

- **WHEN** 请求 API 或 Judge 业务端口的 `/actuator/prometheus`
- **THEN** 该端点不可用，业务认证与接口行为不发生其他变化

#### Scenario: 非本地默认配置

- **WHEN** 使用未启用本地监控暴露的配置启动 API 或 Judge
- **THEN** `/actuator/prometheus` 不因新增依赖而自动暴露

### Requirement: 节点与容器资源指标

外部 Prometheus SHALL 通过 API Server 的 Node 代理采集 kubelet resource 和 cAdvisor 指标，以提供节点及按 Pod 区分的 CPU、内存、重启和 OOM 相关曲线。系统 MUST NOT 将 `metrics-server` 当作历史时序数据库。

#### Scenario: 查看各 Pod 资源曲线

- **WHEN** 压测运行且节点目标抓取成功
- **THEN** Grafana 能按 Pod 展示 CPU 和 memory working set 的历史曲线，并能识别容器重启与 OOM 相关事件指标

#### Scenario: 节点指标抓取失败

- **WHEN** 任一节点代理路径或鉴权不可用
- **THEN** Prometheus 目标健康状态与运行文档能明确暴露失败，系统 MUST NOT 静默改用 Service 混采或关闭 TLS 校验

### Requirement: 中间件指标采集

系统 SHALL 通过现有本地 NodePort 采集 RabbitMQ、PostgreSQL 和 Redis 指标，其中 PostgreSQL 与 Redis MUST 由宿主机 exporter 转换，RabbitMQ SHALL 使用其原生 Prometheus 指标端点。

#### Scenario: 观察判题队列

- **WHEN** 算法压测产生 run 和 formal 消息
- **THEN** Grafana 分别展示两个队列的 ready、unacked 及可用的投递和确认速率

#### Scenario: 观察数据库与缓存

- **WHEN** PostgreSQL 与 Redis exporter 正常连接本地服务
- **THEN** Grafana 展示 PostgreSQL 连接、事务和锁指标，以及 Redis 内存、客户端和命中率指标

### Requirement: 监控凭据与访问安全

系统 SHALL 使用专用 Kubernetes 监控身份和本地私有凭据，身份权限 MUST 仅限于服务发现所需资源读取及 `pods/proxy`、`nodes/proxy` 访问。仓库 MUST NOT 保存真实令牌、CA 私钥、数据库密码、Redis 密码或 Grafana 管理密码，TLS 证书校验 MUST 默认开启。

#### Scenario: 监控身份越权操作

- **WHEN** 使用监控身份尝试读取 Secret 或修改业务工作负载
- **THEN** Kubernetes 拒绝该操作

#### Scenario: 从干净检出的仓库部署

- **WHEN** 操作者根据示例配置本地监控
- **THEN** 流程要求从当前 kind 和本地私有环境生成或注入凭据，不需要使用仓库中的真实秘密，也不会将秘密打印到日志或写入已跟踪文件

#### Scenario: 非本地环境误用

- **WHEN** 监控初始化目标不是明确选择的本地 kind 上下文
- **THEN** 初始化 MUST 停止并要求显式确认，MUST NOT 将高权限代理身份创建到测试或生产集群

### Requirement: 预置压测可视化与目标健康

Grafana SHALL 自动配置 Prometheus 数据源和本地压测 Dashboard，展示 Pod 资源、HTTP、JVM、中间件和抓取目标健康指标。HTTP 指标 MUST 使用 URI 模板等受控标签，MUST NOT 使用用户、题目、任务标识或完整请求 URL 作为标签。

#### Scenario: 打开压测 Dashboard

- **WHEN** 操作者在压测期间打开预置 Dashboard
- **THEN** 无需手工创建数据源即可查看 Pod CPU/内存、HTTP 延迟与错误、JVM、RabbitMQ、PostgreSQL、Redis 和 target up 曲线

#### Scenario: 诊断采集缺口

- **WHEN** 任一预期目标无法抓取
- **THEN** Dashboard 或 Prometheus 目标页显示对应目标不健康，操作者能区分“无业务流量”和“采集失败”

### Requirement: 与现有压测结果保持边界

本能力 SHALL 与 k6 和现有 JSONL/report 按 UTC 时间范围对照使用，MUST NOT 替换、删除或改变现有 k6 吞吐指标、有效业务闭环定义、采样文件或报告格式，也 MUST NOT 将应用请求计数解释为有效业务完成数。

#### Scenario: 对照一次压测

- **WHEN** 操作者完成一轮现有 k6 场景并在 Grafana 选择相同 UTC 时间窗口
- **THEN** k6 继续产出原有业务结果，Grafana 独立提供系统指标曲线，两者可辅助定位延迟或资源异常

#### Scenario: 仅运行原压测工具

- **WHEN** 操作者未启动外部监控而运行现有压测工具
- **THEN** 原压测场景和离线报告仍按既有方式工作，不依赖 Prometheus 或 Grafana

### Requirement: 可重复部署与清理

系统 SHALL 提供本地监控的初始化、启动、健康检查和清理说明，涵盖 API/Judge 镜像更新、监控 RBAC、私有凭据、Compose 和目标验证。清理 MUST 区分保留历史数据与明确删除历史数据。

#### Scenario: 清理监控但保留业务环境

- **WHEN** 操作者执行保留数据的监控清理流程
- **THEN** 外部监控容器和本地监控 RBAC 被移除，kind 业务工作负载及数据不受影响，监控历史卷保留

#### Scenario: 明确删除监控历史

- **WHEN** 操作者显式确认删除监控历史数据
- **THEN** 流程只删除监控数据卷，不删除 PostgreSQL、Redis、RabbitMQ 或对象存储业务数据卷
