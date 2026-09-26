# deploy/ —— 部署资产

本目录承载 k3s 集群部署相关的全部资产。**真实服务器 IP、ACR 地址、namespace、密码一律不入库**，仅在部署时通过参数/Secrets 注入。

```
deploy/
├── README.md              # 本文件：总览、发布与回滚流程
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
   ↓ cd-<svc>.yml：
       1. ghcr 拉取 → 推送 ACR：<ns>/bluenet:<svc>-<版本号>（不可变）+ <svc>（浮动别名，仅调试）
       2. 按 deploy_mode 选择部署路径：
          - compose（旧）：SCP + SSH + docker compose up
          - helm（新）：调用 cd-helm-deploy.yml → helm upgrade --install bluenet-<svc>
```

`deploy_mode` 取值来源：workflow 输入 > 仓库变量 `DEPLOY_MODE` > `compose`（默认，保证过渡期行为不变）。

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

## CI 依赖的 Secrets / Vars

| 类型 | 名称 | 用途 |
|------|------|------|
| Secret | `ACR_REPO` / `ACR_NAMESPACE` / `ACR_USERNAME` / `ACR_PWD` | 镜像推送 |
| Secret | `ACR_REPOSITORY` | 业务镜像仓库名（新增） |
| Secret | `KUBECONFIG` | base64 编码的限权 kubeconfig（SA 限 bluenet namespace，新增） |
| Var | `DEPLOY_MODE` | `compose`（默认）/ `helm` |
| Var | `K8S_BACKEND_HOST` / `K8S_BACKEND_PORT` / `K8S_SSL_ENABLED` | frontend 镜像构建期的 SSR 目标（如 `bluenet-api` / `8080` / `false`） |
| Var | `PUBLIC_HOST` / `PUBLIC_PORT` / `PUBLIC_SSL_ENABLED` | frontend 浏览器侧目标（生产域名 / 443 / true） |
| Var | `PUBLIC_AI_HOST` / `PUBLIC_AI_PORT` / `PUBLIC_AI_SSL_ENABLED` | 浏览器侧 AI 服务目标（经 nginx `/ai/v1`） |

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
