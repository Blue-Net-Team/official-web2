# Docker Desktop 本地集群外可观测

用于本地 `docker-desktop` Kubernetes 压测。Prometheus、Grafana、PostgreSQL/Redis exporter 和仅 Pods 资源的 kube-state-metrics 全部运行在宿主机 Docker，不在 Kubernetes 中创建监控 Pod、DaemonSet 或 sidecar。

- Prometheus：`http://127.0.0.1:9090`
- Grafana：`http://127.0.0.1:3001`，Dashboard：**BlueNet Local / BlueNet Local Load Test**
- 默认抓取间隔/保留期：15 秒 / 7 天；Dashboard 使用 UTC。
- k6 继续负责业务吞吐、有效闭环与离线 JSONL/report。Grafana 的应用请求速率不能解释为有效业务完成数。

## 架构与网络

- 外部 Prometheus 使用专用 ServiceAccount，经 API Server Pod Proxy 按 Pod 抓取 API:8081、Judge:8091，不经 Service 混采。
- 节点 resource/cAdvisor 经 API Server Node Proxy 抓取；kube-state-metrics 在宿主机读取 Pods 状态，提供准确重启和 OOMKilled 指标。
- Docker Desktop 不把业务 NodePort 映射到 Windows 回环地址。监控 Compose 加入现有外部 `kind` Docker 网络，通过 `desktop-worker` / `desktop-worker2` 访问 PostgreSQL、Redis、RabbitMQ。
- RabbitMQ Prometheus 插件使用独立 15692，本地 values 映射为 30692；管理端口 15672 不提供 `/metrics`。

## 前置条件

1. 当前 kubectl context 必须为 `docker-desktop`，BlueNet 已部署到 `bluenet` namespace。
2. Docker 网络 `kind` 存在，节点名保持 `desktop-worker` / `desktop-worker2`。
3. 本地 API/Judge 镜像包含 `micrometer-registry-prometheus`，并使用 `values/local` 部署。管理端口只进入容器端口列表，不创建 Service/NodePort。
4. 复制 `.env.example` 为 `.env`，生成独立 Grafana/PostgreSQL 监控密码；当前本地 Redis 无密码时 `REDIS_PASSWORD` 留空。`.env` 与 `.private/` 均被 Git 忽略。

```powershell
Copy-Item deploy/local-observability/.env.example deploy/local-observability/.env
# 编辑 .env，填入本地随机密码和当前 Redis 密码（若为空则留空）
```

## 初始化与启动

以下命令在仓库根目录执行。初始化会拒绝任何非 `docker-desktop` 目标，且不会打印 token 或密码：

```powershell
python deploy/local-observability/setup.py init --context docker-desktop --confirm docker-desktop
python deploy/local-observability/setup.py verify

$env:POSTGRES_MONITOR_PASSWORD = (Get-Content deploy/local-observability/.env | Where-Object { $_ -match '^POSTGRES_MONITOR_PASSWORD=' }).Split('=', 2)[1]
python deploy/local-observability/postgres_monitor.py init --confirm docker-desktop
Remove-Item Env:POSTGRES_MONITOR_PASSWORD

docker compose --env-file deploy/local-observability/.env -f deploy/local-observability/docker-compose.yml up -d
```

初始化生成有效期一年的本地 ServiceAccount token。token 过期或集群重建后重新执行 `setup.py init` 并 `docker compose ... up -d` 刷新挂载文件。

PostgreSQL 角色 `bluenet_monitor` 仅获得 `pg_monitor`，不使用业务超级用户连接 exporter。该初始化不修改 Flyway，也不参与压测业务数据初始化。

## 更新本地 API/Judge

先检查 3000 端口；本变更不需要启动或构建前端。本地 API 还需挂载 nginx CA，使 Pod 内预签名请求通过 TLS 校验：

```powershell
docker cp bluenet-local-nginx:/etc/nginx/certs/bluenet.local.crt $env:TEMP/bluenet.local.crt
kubectl -n bluenet create secret generic bluenet-local-tls --from-file=ca.crt=$env:TEMP/bluenet.local.crt --dry-run=client -o yaml | kubectl apply -f -
Remove-Item $env:TEMP/bluenet.local.crt
```

```powershell
mvn clean package
# 若 IDE 锁定 Windows target 文件，请先暂停 IDE 的自动构建，不要强行结束用户进程。
docker build -t bluenet-api-service:1.2.0-local-truststore src/backend
docker build -t bluenet-judge-service:0.1.6-local-e64b82d4 src/judge-service

helm upgrade bluenet-api deploy/charts/bluenet -n bluenet -f deploy/charts/bluenet/values/local/api.yaml
helm upgrade bluenet-judge deploy/charts/bluenet -n bluenet -f deploy/charts/bluenet/values/local/judge.yaml
helm upgrade bluenet-rabbitmq deploy/charts/bluenet -n bluenet -f deploy/charts/bluenet/values/local/rabbitmq.yaml
kubectl -n bluenet rollout status deployment/bluenet-api
kubectl -n bluenet rollout status deployment/bluenet-judge
kubectl -n bluenet rollout status statefulset/bluenet-rabbitmq
```

生产 values 不包含 Prometheus 管理端口或暴露配置。业务探针仍走 8080/8090；业务端口不提供 Prometheus 指标。

## 健康检查与压测对照

1. 打开 `http://127.0.0.1:9090/targets`：应有两个 API Pod、一个 Judge Pod、五个 resource 节点、五个 cAdvisor 节点，以及 kube-state-metrics、RabbitMQ、PostgreSQL、Redis。
2. 所有预期 target 应为 `up`。`up == 0` 表示采集失败，不等于没有业务流量。
3. 检查 Judge 仍是 ClusterIP，管理端口未生成 Service。
4. 在 Grafana 选择与 k6 报告一致的 UTC 时间范围，比较 Pod CPU/memory working set、HTTP、JVM、队列和中间件曲线。
5. `BASELINE.md` 记录实施前环境、代理 spike 和测试基线。

静态校验：

```powershell
python -m unittest discover -s deploy/local-observability/tests -p 'test_*.py' -v
python deploy/local-observability/grafana/generate_dashboard.py
docker compose --env-file deploy/local-observability/.env -f deploy/local-observability/docker-compose.yml config --quiet
```

## 停止与清理

保留 Prometheus/Grafana 历史数据，停止外部容器并移除监控 RBAC：

```powershell
docker compose --env-file deploy/local-observability/.env -f deploy/local-observability/docker-compose.yml down
python deploy/local-observability/setup.py cleanup --context docker-desktop --confirm docker-desktop
```

此操作不删除监控数据卷，也不影响业务工作负载或数据。再次初始化并启动即可恢复。

**仅在明确不再需要监控历史时**执行以下命令；它只删除 `bluenet-local-observability` 的监控卷：

```powershell
docker compose --env-file deploy/local-observability/.env -f deploy/local-observability/docker-compose.yml down -v
```

如需同时回收 PostgreSQL 监控角色：

```powershell
python deploy/local-observability/postgres_monitor.py cleanup --confirm docker-desktop
```

`.private/` 含本地 token/CA/kubeconfig；需要彻底清除本地凭据时，在停止监控后手工删除该目录。任何清理命令都不会删除 PostgreSQL、Redis、RabbitMQ 或 RustFS 业务数据卷。
