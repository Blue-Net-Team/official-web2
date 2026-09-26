# Design: deploy-k3s-edge-cluster

## Context

项目当前以 docker-compose 部署（`docker/docker-compose.yml`，profiles: full/app/infra 等），服务包括 api-service（Spring Boot）、frontend（Next.js SSR）、judge-service（isolate 沙箱）、ai-service（FastAPI + pgvector），基础设施 PostgreSQL（pgvector 镜像）、Redis、RabbitMQ（均为容器）。目标环境为 5 台跨云账号的 2C2G 云服务器，无内网互通，必须公网互访；nginx 反向代理与域名位于 8.146.230.107（宿主机进程，非容器）。业务峰值估算：提交题目接口（含文件上传）约 100 QPS，judge-service 大部分时段空闲。约束：未来扩容机器也只会是 2C2G 规格。

### 节点清单与角色

| 节点 | 公网 IP | 角色 label | 常驻负载 |
|------|---------|-----------|---------|
| master | 172.18.116.241 | `bluenet/role=compute` | k3s server + Dashboard + metrics-server |
| node-1 | 8.146.230.107 | `bluenet/role=mq` | RabbitMQ；宿主机 nginx（集群外入口） |
| node-2 | 39.96.11.213 | `bluenet/role=db` | PostgreSQL + Redis |
| node-3 | 182.92.241.91 | `bluenet/role=compute` | 弹性池 |
| node-4 | 123.56.253.250 | `bluenet/role=compute` | 弹性池 |

## Goals / Non-Goals

**Goals:**

- 5 台服务器全部纳入 k3s 集群（1 server + 4 agent），统一编排，发布不 SSH
- 有状态服务（PG/Redis/RabbitMQ）以容器 + local PV + label 钉节点方式部署，声明式管理
- 无状态服务弹性调度：不指定节点，scheduler 按 request/limit 装箱
- GitHub Actions + Helm 的 push-to-deploy CD 链路
- Kubernetes Dashboard 可视化管理
- 对象存储使用云 OSS（aliyun-oss），向量检索使用 pgvector，均不新增自托管组件

**Non-Goals:**

- 高可用：所有服务单副本，节点宕机 = 该服务不可用（PG 除外靠每日备份兜底）
- 自动扩缩：HPA / cluster-autoscaler 不上（机器固定 5 台 2C2G，无可扩对象）
- MinIO / Milvus / ingress-controller / ArgoCD / Rancher：均不引入
- 数据库主从、Patroni 等 HA 方案：2C2G 内存与公网延迟下不做
- 跨节点共享存储（Longhorn/NFS）：不引入，有状态服务数据与节点绑定

## Decisions

### D1: k3s（而非 kubeadm/K8s 发行版）+ 全节点入集群

k3s 单二进制、内置 etcd/containerd/flannel，内存占用最小（server 约 500-800m），是 2C2G 边缘节点的现实选择。DB/MQ 节点也加入集群（而非留 compose 独立管理），满足"不 SSH 各服务器、统一管理"的核心诉求。

备选：kubeadm + 原生 K8s（组件多、内存开销大，2C2G 不可行）；k3s 只装 3 台 + DB/MQ 独立 compose（etcd 公网风险并未显著下降，却引入第二套编排，否）。

### D2: 公网互访安全基线

本集群属于 k3s 官方的「**分布式赝合云/多云集群**」模式（节点间无共同私有网络）：k3s 集群流量走 WireGuard VPN 网状网络（CNI 流量）与 WebSocket 隧道（管理流量）。

必需的 k3s 参数（缺一不可）：

```
server: --node-external-ip=<SERVER公网IP> --flannel-backend=wireguard-native --flannel-external-ip
agent:  --node-external-ip=<AGENT公网IP>
        (K3S_URL 指向 SERVER 公网 IP)
```

注：`--flannel-external-ip` 让 flannel 以公网 IP 作为节点地址建立跨节点隧道，缺失时 Pod 跨节点通信（CNI 层）会失效而节点仍显示 Ready。**该参数仅 server 支持：写入 agent 的 config.yaml 会导致 k3s-agent 启动失败**（实测报 exit-code）；agent 仅需 `--node-external-ip`。此模式**不支持嵌入式 etcd**（需多 server 场景应另设方案）。

集群跨公网、跨云账号，安全组默认全拒，按需白名单：

- master 6443（k3s API）：公网可达，安全依赖 **TLS + token/客户端证书认证**（决策：团队接受“强认证即可公网暴露”的模型，与 SSH 密钥认证公网暴露的现有惯例一致）
- 节点 22（SSH）：维持现状，公网密钥认证登录
- CI 凭据加固：CI 使用独立 ServiceAccount + 限权 RBAC + 短生命周期 token，降低凭据泄露爆炸半径；apiserver 安全更新需及时跟进（见风险表）
- flannel/WireGuard UDP 51820：仅 5 节点公网 IP 互指
- NodePort（30000 段）：仅 nginx 节点（8.146.230.107）公网 IP 可访问（节点 IP 固定，无动态问题）
- nginx 节点 80/443：对公网开放（业务入口，现状保留）
- 所有节点禁止 0.0.0.0/0 放通上述管理端口；kubelet(10250) 等组件端口默认拒绝
- **例外：kubelet 10250/TCP 需在 5 台节点间互放**（来源 = 其余 4 台节点公网 IP，共 20 条）。原因：metrics-server 作为普通 Pod 只能直连节点 ExternalIP:10250 抓取指标（k3s 的 agent 反向 WebSocket 隧道仅适用于 apiserver 的 exec/logs 场景）。kubelet 默认关闭匿名访问、需 webhook 认证，暴露面限于 5 个已知节点 IP。若不需要 `kubectl top` 与 Dashboard 指标，可用 `--disable metrics-server` 并去掉这些规则

安装参数：`--flannel-backend wireguard-native --node-external-ip <公网IP> --tls-san <master公网IP>`；agent 以 `K3S_URL=https://<master>:6443` 加入。备选管理面方案（动态 IP 问题）：曾评估 Tailscale overlay（6443/22 仅 tailnet 可达）以规避运维 IP 动态变化，因部署复杂度被否；最终决策为公网暴露 + 强认证。

### D3: 调度模型——"只有数据决定节点"

```
① 硬钉（nodeSelector，仅 3 个有状态服务）
   bluenet/role=db → PostgreSQL + Redis
   bluenet/role=mq → RabbitMQ

② 自由调度（其余全部，含 judge）
   仅 request/limit + preferred podAntiAffinity
```

- 钉节点一律使用 label，**任何 manifest/values 中不出现 IP**；IP 仅存在于集群外（k3s join 脚本、nginx upstream）
- master 不打 taint（资源太珍贵），Dashboard/metrics-server 等轻系统件放 master，业务 Pod 靠 request 装箱自然避开
- judge-service 与其他无状态服务完全同权；privileged 容器逃逸风险被接受（威胁模型：仅受信登录用户可提交代码）

备选：judge 专用节点/taint（一台 2C2G 独占空闲服务过于奢侈，且用户明确接受风险）；master 打 NoSchedule taint（浪费约 1G 可调度余量）。

### D4: 资源配额（容量规划）

| 服务 | 副本 | request (mem/cpu) | limit (mem/cpu) |
|------|------|------------------|-----------------|
| api-service | 2 | 512m / 250m | 1G / 500m |
| frontend | 2 | 384m / 200m | 640m / 500m |
| ai-service | 1 | 256m / 200m | 512m / 500m |
| judge-service | 1 | 128m / 200m | 1G / 1000m |
| PostgreSQL | 1 | 512m / 300m | 1.2G / 1000m |
| Redis | 1 | 64m / 100m | 256m / 300m |
| RabbitMQ | 1 | 256m / 200m | 512m / 500m |
| k8s-dashboard | 1 | 100m / 50m | 300m / 200m |
| metrics-server | 1 | 50m / 50m | 100m / 100m |

request 合计 ≈ 2.3G/1.6 核，池子可调度余量 ≈ 6.9G（master≈1.1G、mq≈1.4G、db≈1.2G、两台 compute≈1.6G×2），装箱健康。request 故意给低（尤其 judge 128m）以换取调度自由度；PG limit 1.2G 受 db 节点余量约束，配合保守 PG 参数（`shared_buffers=256MB` 等）与 1-2G swap。

### D5: 入口——保留宿主机 nginx + NodePort

不上 ingress-controller（节省约 300-500m 内存与一套证书管理）。api/frontend/ai 暴露为 NodePort Service（如 api:30080、frontend:30000），nginx（8.146 宿主机）upstream 指向多个节点 IP:NodePort，Pod 漂移对入口透明。judge 不暴露公网（仅集群内 Service）。SSL 证书与域名管理维持现状。

### D6: Helm chart 结构与配置管理

```
deploy/
├── k3s/                  # 集群安装脚本（server/agent 参数、label 初始化、安全组 IP 清单）
├── charts/               # 每个服务一个 chart，chart 内 MUST 同时包含：
│   ├── bluenet-api/          # templates/deployment.yaml + service.yaml + values.yaml
│   ├── bluenet-frontend/     # Service 为服务发现唯一入口，跨 Pod 通信禁止直连 Pod IP
│   ├── bluenet-judge/        # judge 仅 ClusterIP（集群内服务发现，无 NodePort）
│   ├── bluenet-ai/           # ai 经 nginx /ai/v1 转发 → NodePort
│   ├── bluenet-postgres/     # StatefulSet + headless/ClusterIP Service
│   ├── bluenet-redis/
│   └── bluenet-rabbitmq/
├── values/               # 环境级 values 覆写（如 values-prod.yaml），与 chart 默认值分离
└── secrets/              # sops/age 或 CI 注入脚本（二选一，见 Open Questions）

# CI workflow 位于仓库根 .github/workflows/（不属于 deploy/ 目录）
```

- **Service 是服务发现的唯一方式**：api/frontend/ai 对外经 NodePort Service（供 nginx upstream），judge/pg/redis/rabbitmq 为 ClusterIP Service（集群内 DNS 发现，如 `bluenet-api:8080`、`bluenet-postgres:5432`）；任何环境变量中的后端地址 MUST 指向 Service DNS 名而非 Pod IP

- env 从 compose 的 `x-shared-env` / `x-api-service-env` 平移至各 chart values，敏感项（DB 密码、JWT_SECRET、GitHub App pem、LLM API key）走 k8s Secret，CI 中以 `kubectl apply` 或 helm-secrets 注入，**不提交明文**
- frontend 的 `NEXT_PUBLIC_*` 为构建期注入，镜像 build 阶段由 CI 传 build args；运行时仅 `BACKEND_HOST` 等 SSR 变量（指向集群内 Service DNS，如 `bluenet-api:8080`）
- 镜像仓库：阿里云容器镜像服务（国内节点拉取稳定）或 ghcr；CI 凭据用 GitHub Secrets

### D7: CD 链路（push-to-deploy，无 GitOps）

```
git push / release → GitHub Actions:
  build images（api/judge/ai/frontend，frontend 带 build args）
  → push 镜像仓库
  → 配置 KUBECONFIG（master 公网 6443，短周期 token + 限权 RBAC）
  → helm dependency build && helm upgrade --install（各 chart 独立 release）
  → kubectl rollout status 等待就绪
```

不上 ArgoCD（约 1G 内存，master 余量不允许）；如需回滚用 `helm rollback <release> <revision>`。

### D8: 有状态服务部署细节

- PostgreSQL：StatefulSet + local-path PV，钉 `bluenet/role=db`；image 沿用 `pgvector/pgvector:pg17`；`shared_buffers=256MB`、`work_mem` 保守值经 values 注入；每日 03:00 `pg_dump` CronJob → 云 OSS（复用 aliyun-oss 凭据）
- Redis：单实例，`appendonly yes`，密码经 Secret；钉 db 节点
- RabbitMQ：`rabbitmq:3-management`，钉 mq 节点；队列/exchange 拓扑沿用 compose 中 `JUDGE_*` 约定
- 备份恢复流程写入 `deploy/` 文档（恢复 = 新 PV + pg_restore）

### D9: ai-service 向量检索走 pgvector

`TBD_RAG_VECTOR_STORE_BACKEND=pgvector`，`TBD_RAG_PGVECTOR_URI` 指向集群内 PG Service 的 `db_blue_net` 库；不部署 Milvus，也**不创建独立 rag 库**（已核实：RAG 向量表由主 API 服务的 Flyway migration `V17__add_rag_vector_tables.sql` 在 `db_blue_net` 中创建，ai-service 与主服务共用同一库）。ai-service 无状态、无特权，普通弹性 Pod。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 跨公网 etcd 心跳受云间延迟/丢包影响，leader 抖动 | k3s 默认容忍参数不动；监控 etcd leader changes；单云内优先选同 region 节点做 server（当前 master 与 node-3 均为阿里云，可评估迁移 server） |
| 单副本 PG 节点宕机 = 全站不可用 | 每日逻辑备份到 OSS + 恢复文档；接受 RPO 24h/RTO 数小时（业务量级可承受） |
| PG limit 1.2G 接近 db 节点红线，pgvector 大查询 OOM | 保守 shared_buffers；swap 兜底；Dashboard 配置内存告警；慢查询观察 |
| judge privileged + 受信用户代码：逃逸即节点沦陷（数据节点同池） | 威胁模型已评估接受；未来如需收紧可用 seccomp/Kata（非本次范围） |
| wireguard flannel 在部分云安全组下 MTU 问题 | 安装后跨节点 Pod 连通性验证纳入验收；必要时调 flannel MTU |
| 存量服务器为 cgroup v1（CentOS 7 系老系统），k8s 1.35+ 默认拒绝启动 kubelet | 安装脚本统一加 `--kubelet-arg=fail-cgroupv1=false`（v1.37 仍保留该回退开关，代码删除不早于 1.38）；长期应评估迁移 cgroup v2 |
| 节点为“VPC 内网 IP + 公网 IP”模式且 VPC 间不互通，k8s Endpoints 默认登记内网 IP | 已按官方多云模式启用 `--flannel-external-ip`；实测跨节点 Pod↔Pod、DNS、ClusterIP 均正常 |
| metrics-server 无法抓取 kubelet 指标（`Failed to scrape node ... :10250 timeout`） | 需在 5 台节点间互放 10250/TCP（见 D2 例外）；若仍报 TLS 校验失败，给 metrics-server 加 `--kubelet-insecure-tls` |
| NodePort 直接暴露，绕过 ingress 的限流/WAF 能力 | 安全组限定仅 nginx 节点可达；nginx 层保留现有访问控制 |
| CI 凭据（kubeconfig）泄露 = 集群失守 | 公网暴露决策下此风险上升：缓解 = CI token 绑定最小 RBAC 权限 + 短有效期 + 仅 GitHub Secrets 存储；泄露后立即吊销重建 ServiceAccount |
| apiserver 认证绕过类 0day（历史 CVE 先例） | 6443 公网可达且无网络层白名单，唯一防线是响应速度：订阅 k3s 安全公告，CVE 修复 48h 内滚动升级 server 节点 |
| frontend 构建期变量与运行时环境耦合（NEXT_PUBLIC_*） | CI build args 按环境（staging/prod）分 workflow job，镜像 tag 区分 |

## Migration Plan

1. **并行期**：k3s 集群与现有 compose 并存；compose 的 DB/MQ 保留运行（存量数据）
2. **数据迁移**：`pg_dump` 导出 compose PG → 导入 k3s PG；Redis 缓存可冷启动不迁；RabbitMQ 队列冷启动
3. **灰度切流**：先切 ai-service / judge-service（低风险）→ api-service → frontend；nginx upstream 逐段改指 NodePort
4. **下线 compose**：app profile 停止；infra profile 的 PG 保留只读一周作为回滚源，验证后归档
5. **回滚**：任一阶段 nginx upstream 改回原 compose 地址即回滚；k3s 集群本身可随时重建（声明式）

## Open Questions

1. **镜像仓库选型**：阿里云 ACR（推荐，国内拉取快）vs ghcr（免额外服务但国内不稳）——需用户提供/确认仓库凭据
2. **Secret 管理**：sops/age 加密入库（推荐，可审计）vs CI 纯 Secrets 注入（简单但集群内变更不可审计）——倾向 sops
3. ~~ai-service 的 pgvector 库~~ → **已决策**：复用 `db_blue_net` 单库（RAG 向量表由主服务 Flyway 管理，无独立 rag 库）
4. **master 迁移评估**：server 迁至与多数节点同云（阿里云 182.92.241.91 或 123.56.253.250）以降低 etcd 公网延迟——实施时实测后决定，非阻塞项
