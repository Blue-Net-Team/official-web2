# deploy/ —— 部署资产

> **★ 从零搭建集群并部署本项目，请先读 [CLUSTER-SETUP.md](./CLUSTER-SETUP.md)** —— 含完整步骤、验收清单、容量规划与 **20 个踩坑记录**。

本目录承载 k3s 集群部署相关的全部资产。**真实服务器 IP、ACR 地址、namespace、密码一律不入库**，仅在部署时通过参数/Secrets 注入。

```
deploy/
├── README.md              # 本文件：总览、发布与回滚流程
├── CLUSTER-SETUP.md      # ★ 从零搭建集群 + 部署项目完整手册（含踩坑记录）
├── k3s/                   # 集群安装（安装脚本、离线包说明、ACR 凭据配置）
│   ├── README.md
│   ├── install-server.sh / install-agent.sh      # 在线安装
│   └── offline-install.sh                         # 离线安装（国内推荐）
├── charts/bluenet/        # 唯一 Helm chart（通用模板 + 每服务 values）
│   ├── README.md          # 字段速查、镜像注入、回滚约定
│   ├── templates/         # deployment / statefulset / cronjob / service / secret
│   └── values/            # api / frontend / ai / judge / postgres / redis / rabbitmq
├── scripts/               # 辅助脚本（第三方镜像中转等）
│   ├── README.md
│   └── mirror-images.ps1
└── values/                # 预留：环境级 values 覆写（不入库敏感值）
```

## 部署架构速览

```
公网 → nginx（宿主机，集群外）
        ├─ /        → NodePort 30000  frontend
        ├─ /api/v1  → NodePort 30080  api-service
        └─ /ai/v1   → NodePort 30081  ai-service

k3s 集群（5 节点，公网互访 + wireguard 加密 overlay）
  ├─ master：控制面 + 轻系统件（Dashboard/metrics-server）
  ├─ db 节点：PostgreSQL(pgvector) + Redis        ← nodeSelector bluenet/role=db
  ├─ mq 节点：RabbitMQ                            ← nodeSelector bluenet/role=mq
  └─ 其余 compute：api / frontend / ai / judge    ← 自由调度，无节点约束
```

## 发布流程

### 方式一：CI 自动发布（推荐）

```
开发者改代码 → 提升 trigger/<svc> 版本号
   ↓ ci.yml：测试 + 构建 → ghcr.io（tag: <版本号> / sha-xxx / latest）
   ↓ cd-<svc>.yml（纯 helm，无 SSH）：
       1. push-image job：ghcr 拉取 → 推送 ACR：<ns>/<repo>:<svc>-<版本号>（不可变）+ <svc>（浮动别名，仅调试）
       2. deploy job：调用 cd-helm-deploy.yml → helm upgrade --install bluenet-<svc> → rollout status
   ↓ cd-infra.yml：postgres / redis / rabbitmq 三个 StatefulSet 同样经 helm 发布（基础镜像来源 namespace）
```

### 方式二：本地/手工发布

```bash
export KUBECONFIG=~/.kube/bluenet-k3s.yaml
helm upgrade --install bluenet-api deploy/charts/bluenet -n bluenet \
  -f deploy/charts/bluenet/values/api.yaml \
  --set image.repository=<ACR>/<SERVICE-NAMESPACE>/<SERVICE-REPO> \
  --set image.tag=api-<版本号> \
  --wait --timeout 5m
```

基础服务（postgres/redis/rabbitmq）用 `<BASE-NAMESPACE>/<BASE-REPO>` 注入。

## 回滚流程

```bash
# 查看发布历史与各版本使用的镜像 tag
helm history bluenet-api -n bluenet
helm get values bluenet-api -n bluenet --revision <N> | grep -A2 '^image:'

# 回滚到指定 revision（旧 tag 镜像仍在 ACR，秒级生效）
helm rollback bluenet-api <N> -n bluenet
```

> 前提：CI 必须推送**不可变 tag**（`<svc>-<版本号>`）。若使用固定 tag（如 `:api`），镜像被覆盖后回滚会静默失败。

## CI 依赖的 Secrets / Vars（全面 helm 后）

### Secrets

| 名称 | 状态 | 用途 |
|------|------|------|
| `ACR_REPO` | 保留 | ACR host（业务与基础镜像同一 host） |
| `ACR_NAMESPACE` | 保留 | 业务镜像 namespace |
| `ACR_USERNAME` / `ACR_PWD` | 保留 | ACR 登录 |
| `ACR_REPOSITORY` | **新增** | 业务镜像仓库名 |
| `ACR_BASE_NAMESPACE` | **新增** | 基础镜像 namespace（仅 cd-infra 使用） |
| `ACR_BASE_REPOSITORY` | **新增** | 基础镜像仓库名 |
| `KUBECONFIG` | **新增** | base64 编码的限权 kubeconfig（SA 限 bluenet namespace） |
| `QODANA_TOKEN` | 保留 | 代码质量扫描 |
| `IVEN_PACKAGES_USER` / `IVEN_PACKAGES_TOKEN` | 保留 | 私有包依赖 |
| ~~`*_DEPLOY_HOST_*` / `*_DEPLOY_PATH_*` / `*_DEPLOY_KEY` / `*_DEPLOY_USER` / `*_DEPLOY_PORT` / `DEPLOY_*` / `DATABASE_HOST_*` / `RABBITMQ_HOST_*`~~ | **待清理（49 个）** | compose/SSH 时代遗留，切流稳定后删除 |

### Variables

| 名称 | 状态 | 值示例 | 用途 |
|------|------|--------|------|
| `K8S_BACKEND_HOST` | **新增** | `bluenet-api` | frontend 构建期 SSR 目标（集群内 DNS） |
| `K8S_BACKEND_PORT` | **新增** | `8080` | 同上 |
| `K8S_SSL_ENABLED` | **新增** | `false` | SSR 内部访问为 http |
| `PUBLIC_HOST` | **新增** | `<生产域名>` | 浏览器侧目标（经 nginx） |
| `PUBLIC_PORT` | **新增** | `443` | 同上 |
| `PUBLIC_SSL_ENABLED` | **新增** | `true` | 同上 |
| `PUBLIC_AI_HOST` / `PUBLIC_AI_PORT` / `PUBLIC_AI_SSL_ENABLED` | **新增** | `<域名>` / `443` / `true` | 浏览器侧 AI 服务（经 nginx `/ai/v1`） |
| `AI_SERVICE_PREFIX` | 保留 | `/ai/v1` | AI 路由前缀（构建期注入） |
| ~~`BACKEND_HOST` / `BACKEND_PORT` / `SSL_ENABLED` / `AI_SERVICE_HOST` / `AI_SERVICE_PORT` / `AI_SERVICE_SSL_ENABLED`~~ | **待清理（6 个）** | — | 旧兜底变量，新变量就位后删除 |

## Kubernetes Dashboard

访问方式二选一：

**A. 公网域名（已配置，推荐日常使用，配合只读账号）**

```
https://<DASHBOARD_DOMAIN>
```

链路：宝塔 nginx（该站点）→ `https://127.0.0.1:30443`（本机 NodePort）→ dashboard Pod。
指向本机 NodePort 的好处：**不需要为该端口开云安全组**（nginx 主机本身就是 k3s 节点）。
nginx 侧关键配置（Dashboard 特有）：`proxy_ssl_verify off`（上游自签证书）、
`Upgrade/Connection` 两个头（WebSocket：日志/终端）、`proxy_read_timeout 3600s`（日志长连接）。

**B. 本地隧道（不经公网，用于 cluster-admin 应急操作）**

```bash
# 安装（如已装可跳过）
./deploy/scripts/install-dashboard.sh <ACR-ADDRESS>/<BASE-NAMESPACE>/<BASE-REPO>

# 建立隧道
kubectl -n kubernetes-dashboard port-forward svc/kubernetes-dashboard 8443:443
# 浏览器打开 https://localhost:8443（自签证书，忽略告警）

# 获取登录 token（dashboard-admin / cluster-admin，有效期 30 天）
kubectl -n kubernetes-dashboard create token dashboard-admin --duration=720h
```

| 项 | 值 |
|----|-----|
| 命名空间 | `kubernetes-dashboard` |
| 部署方式 | 官方 v2.7.0 `recommended.yaml`（自带 Deployment/Service/RBAC，无需自写） |
| 镜像 | ACR 中转的 `dashboard-v2.7.0` / `metrics-scraper-v1.0.8` |
| 登录 | ServiceAccount `dashboard-admin` + token（ClusterRoleBinding → cluster-admin） |
| 指标 | 由 `metrics-server` 提供（`kubectl top` 可用） |

> token 30 天过期，到期重跑上面第 3 条命令即可。

### 账号与 token

| 账号 | 权限 | 用途 | 取 token |
|------|------|------|---------|
| `dashboard-viewer` | 只读（ClusterRole `view`） | **公网域名日常使用** | `kubectl -n kubernetes-dashboard create token dashboard-viewer --duration=720h` |
| `dashboard-admin` | cluster-admin | 仅本地隧道应急 | `kubectl -n kubernetes-dashboard create token dashboard-admin --duration=720h` |

### 公网入口的安全加固（建议）

Dashboard 是 cluster-admin 级面板，公网可达时建议至少做以下两项：

1. **宝塔站点开启"密码访问"（Basic Auth）** 或手工加：
   ```nginx
   auth_basic "BlueNet Dashboard";
   auth_basic_user_file /www/server/panel/vhost/nginx/.htpasswd_dashboard;
   ```
   生成密码文件：`htpasswd -bc /www/server/panel/vhost/nginx/.htpasswd_dashboard <用户名> '<密码>'`
2. **日常只用 `dashboard-viewer`（只读）token**，避免 cluster-admin token 暴露在公网入口。

> 注意：宝塔面板保存站点配置时可能重写该文件，重写后需确认 `proxy_ssl_verify off` 与两处超时设置仍在。

## 运维常用命令

```bash
# 集群与工作负载
kubectl get nodes -L bluenet/role
kubectl -n bluenet get pods -o wide
kubectl -n bluenet get pvc

# 资源用量
kubectl top nodes
kubectl top pods -n bluenet

# 数据库/中间件本地访问（无需开放公网端口）
kubectl -n bluenet port-forward svc/bluenet-postgres 15432:5432
kubectl -n bluenet port-forward svc/bluenet-rabbitmq 15672:15672
kubectl -n bluenet port-forward svc/bluenet-redis 16379:6379
```

## 相关文档

- 集群安装与 ACR 凭据配置：`deploy/k3s/README.md`
- chart 字段与回滚约定：`deploy/charts/bluenet/README.md`
- 镜像中转脚本：`deploy/scripts/README.md`
- 变更全过程与决策：`openspec/changes/deploy-k3s-edge-cluster/`
