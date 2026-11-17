# 本地外部可观测实施基线

记录时间：2026-11-17T14:26:47Z（UTC）。

## 环境与拓扑

- 当前 kubeconfig context：`docker-desktop`；`kind get clusters` 返回空，目标确认为 Docker Desktop 内置 Kubernetes，而非 kind。
- kind CLI：v0.33.0（仅用于排除 kind 环境）；Docker Client/Server：29.5.3；Helm：v3.16.3。
- Kubernetes Client：v1.34.1；Server：v1.34.3；API Server：`https://127.0.0.1:27375`。
- 五个 Ready 节点：`desktop-control-plane`、`desktop-worker`、`desktop-worker2`、`desktop-worker3`、`desktop-worker4`，均为 v1.34.3。
- RabbitMQ 镜像：`rabbitmq:3-management`；实际版本：3.13.7。

## 业务工作负载

| 工作负载 | 类型 | 期望副本 | Ready |
|---|---|---:|---:|
| bluenet-ai | Deployment | 1 | 1 |
| bluenet-api | Deployment | 2 | 2 |
| bluenet-frontend | Deployment | 1 | 1 |
| bluenet-judge | Deployment | 1 | 1 |
| bluenet-postgres | StatefulSet | 1 | 1 |
| bluenet-rabbitmq | StatefulSet | 1 | 1 |
| bluenet-redis | StatefulSet | 1 | 1 |
| bluenet-rustfs | StatefulSet | 1 | 1 |

## 服务入口

| 服务 | 类型 | 端口 |
|---|---|---|
| bluenet-api | NodePort | 8080 → 30080 |
| bluenet-judge | ClusterIP | 8090，无 NodePort |
| bluenet-postgres | NodePort | 5432 → 30432 |
| bluenet-rabbitmq | NodePort | 5672 → 30572；15672 → 30672 |
| bluenet-redis | NodePort | 6379 → 30379 |
| bluenet-rustfs | NodePort | 9000 → 30700；7001 → 30701 |

基线集群中未发现 Prometheus、Grafana、exporter、OpenTelemetry 或其他监控工作负载。

## 代理连通性 Spike

使用有效期 10 分钟的临时 ServiceAccount token 和最小 ClusterRole 验证，结束后已删除 ServiceAccount、ClusterRole、ClusterRoleBinding 与临时文件：

- `pods/<pod>:8080/proxy/api/v1/health`：HTTP 200。
- `nodes/<node>/proxy/metrics/resource`：HTTP 200，返回 Prometheus 文本，含 `container_cpu_usage_seconds_total`。
- `nodes/<node>/proxy/metrics/cadvisor`：HTTP 200，返回完整 cAdvisor Prometheus 文本。
- 使用集群 CA 正常完成证书链与主机名校验。Windows Schannel 对本私有 CA 的 CRL 检查需 `curl --ssl-no-revoke`；这不关闭证书校验，Linux Prometheus 容器不使用该 Schannel 参数。

## 中间件连通性 Spike

- Docker Desktop 未将业务 NodePort 直接映射到 Windows `127.0.0.1`，外部监控容器需加入现有 `kind` Docker 网络，通过稳定节点名访问 NodePort：`desktop-worker:30432`（PostgreSQL）、`desktop-worker:30379`（Redis）、`desktop-worker2:30692`（RabbitMQ）。
- 三个地址的 TCP 连通性均通过现有 `bluenet-local-nginx` 容器验证。
- RabbitMQ 3.13.7 已启用 `rabbitmq_prometheus`，但独立监听 15692，而非管理端口 15672。本地 values 已增加 15692 → 30692；节点网络抓取 `/metrics` 返回 Erlang/RabbitMQ Prometheus 文本。

## 变更前测试基线

- `mvn test`：Reactor BUILD SUCCESS；API 820 项、Judge 29 项，全部通过。
- 八份本地 Helm values（api/frontend/ai/judge/postgres/redis/rabbitmq/rustfs）：全部渲染通过。
- 压测 Node 聚焦测试：16 项通过。
- 压测 Python 单元测试：12 项通过。

## 实施验收结果

- 完整 `mvn package`：BUILD SUCCESS；API 821 项、Judge 31 项通过。
- 外部监控：17 个 target 全部健康，包括两个 API Pod、一个 Judge Pod、五个 resource 节点、五个 cAdvisor 节点及中间件。
- Grafana：24 个面板通过 JSON 与 Prometheus 在线 PromQL 语法校验，provisioning 查询通过。
- 算法 preflight `local-observability-preflight-cookie`：6/6 成功。
- 短算法实验 `local-observability-algorithm-2027`：12/12 成功，UTC 15:41:12–15:41:46；唯一变量为 assessment.grade=2027 与外部监控开启，实验后已恢复基线年级。
- 无监控报名 preflight `local-observability-without-monitoring`：1/1 成功。
- API Pod 删除重建后服务发现出现两个全新健康目标；保留数据清理/恢复后 30 分钟前历史 `up` 序列仍可查询。
