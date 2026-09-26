# k3s 集群搭建与项目部署完整手册

> **适用场景**：从零开始，在多台**跨云账号、无内网互通、仅公网可达**的 2C2G 云服务器上组建 k3s 边缘集群，并部署本项目（BlueNet）。
>
> **本文所有 IP、ACR 地址、域名、密码均为占位符**（形如 `<MASTER_IP>`）。真实值见运维记录/密码管理器，**不得写入仓库**。
>
> 本文记录了 2026-09 首次实施时踩过的**全部 19 个坑**，是本文最有价值的部分——建议先通读第 8 节。

---

## 1. 资源与前置条件

### 1.1 服务器（示例：5 台 2C2G）

| 角色 | label | 公网 IP 占位符 | 承载 |
|------|-------|---------------|------|
| master | `bluenet/role=compute` | `<MASTER_IP>` | k3s server + Dashboard + metrics-server |
| mq | `bluenet/role=mq` | `<MQ_IP>` | RabbitMQ；宿主机 nginx（集群外入口） |
| db | `bluenet/role=db` | `<DB_IP>` | PostgreSQL(pgvector) + Redis |
| compute-1 | `bluenet/role=compute` | `<COMPUTE1_IP>` | 弹性池 |
| compute-2 | `bluenet/role=compute` | `<COMPUTE2_IP>` | 弹性池 |

**约束**：
- 2C2G 是小规格，务必按第 4 节的容量规划分配资源、按第 8 节的坑设置探针与 CPU 配额
- 节点可为「内网 IP + 弹性公网 IP」模式（阿里云常见）：**k3s 必须使用公网 IP** 作为 `--node-external-ip` / `--tls-san`
- 系统若为 CentOS 7 系老系统（cgroup v1），需额外参数（见 8.2）

### 1.2 云安全组规则（必须在安装 k3s 前配好）

| 节点 | 方向 | 端口/协议 | 来源 | 用途 |
|------|------|----------|------|------|
| 全部节点 | 入 | 22/TCP | 运维 IP 或 0.0.0.0/0 | SSH |
| master | 入 | 6443/TCP | 0.0.0.0/0 | k3s API（认证靠 TLS+token） |
| 全部节点 | 入 | 51820/UDP | **其余 4 个节点 IP 互指** | flannel wireguard（**必须是 UDP**） |
| 全部节点 | 入 | 10250/TCP | **其余 4 个节点 IP + 本机 IP** | metrics-server 抓 kubelet（本机 IP 用于 hairpin） |
| 全部节点 | 入 | 30000-32767/TCP | **仅 nginx 节点 IP** | NodePort（nginx 转发） |
| nginx 节点 | 入 | 80,443/TCP | 0.0.0.0/0 | 公网业务入口 |
| 全部节点 | 出 | 全部 | — | 拉镜像、yum、OSS、GitHub |
| 其余 | 入 | 默认拒绝 | — | PG/Redis/RabbitMQ 端口**不对公网开放** |

> 需要额外放行的可选端口：数据库/中间件的对外访问（见第 7 节，建议只对 nginx 节点开放 30432 等 NodePort）。

### 1.3 外部依赖

| 依赖 | 说明 |
|------|------|
| 容器镜像仓库 | 阿里云 ACR（个人版即可），**两个 namespace**：业务镜像 `<SVC_NS>/<SVC_REPO>`、基础镜像 `<BASE_NS>/<BASE_REPO>`（同 host） |
| 对象存储 | 阿里云 OSS（bucket + AK/SK）——**不使用 MinIO** |
| 域名 + SSL | 生产域名 `<PROD_DOMAIN>`（A 记录 → nginx 节点）；可选 Dashboard 子域 `<DASHBOARD_DOMAIN>` |
| nginx | nginx 节点上的宿主机 nginx（宝塔面板亦可），作为集群外入口 |
| 第三方凭据 | GitHub OAuth App（client id/secret）、GitHub App（Issue 同步 / 组织邀请，含私钥 PEM + webhook secret）、LLM API Key（硅基流动 / DeepSeek）、邮件 SMTP 授权码、WPS 绑定码 |

---

## 2. 架构总览

```
公网 → nginx（宿主机，集群外）
        ├─ /            → NodePort 30000  frontend
        ├─ /api/v1      → NodePort 30080  api-service
        ├─ /ai/v1       → NodePort 30081  ai-service
        └─ <db 子域>:5432 → NodePort 30432 PostgreSQL（四层 stream 转发，可选）

k3s 集群（1 server + 4 agent，公网互访 + wireguard 加密 overlay）
  ├─ master ：控制面 + Dashboard + metrics-server（也参与业务调度）
  ├─ db     ：PostgreSQL + Redis        ← nodeSelector bluenet/role=db
  ├─ mq     ：RabbitMQ                  ← nodeSelector bluenet/role=mq
  └─ 其余   ：api / frontend / ai / judge ← 无节点约束，自由调度
```

### 关键设计决策（务必理解，否则排障会走弯路）

| 决策 | 原因 |
|------|------|
| **k3s 多云模式**：`--flannel-backend wireguard-native --flannel-external-ip` | 节点间只有公网可达，必须让 flannel 以公网 IP 建隧道 |
| **禁用自带 Traefik** | 其 ServiceLB 会在每节点 hostPort 抢占 80/443，与宿主机 nginx 冲突 |
| **只有"数据"决定节点** | 调度约束一律用 label（`bluenet/role`），manifest 中不出现 IP |
| **有状态服务单副本 + local-path** | 无共享存储；接受"节点宕机=服务不可用"，靠备份兜底 |
| **云 OSS + pgvector** | 不部署 MinIO / Milvus |
| **镜像 tag 不可变**（`<svc>-<版本>`） | 回滚的前提：旧 tag 不被覆盖 |
| **外部 nginx + NodePort**（不用 ingress-controller） | 省内存（2C2G），并复用已有域名证书 |

---

## 3. 目录与文件说明

```
deploy/
├── README.md                       # 总览：发布/回滚流程、Secrets 清单、常用命令
├── CLUSTER-SETUP.md               # ★ 本文：从零搭建手册
├── k3s/
│   ├── README.md                  # k3s 安装（在线/离线）、ACR 凭据配置、排障
│   ├── install-server.sh          # 在线安装 master
│   ├── install-agent.sh           # 在线安装 agent
│   └── offline-install.sh         # 离线安装（国内推荐，含 airgap 镜像）
├── charts/bluenet/                 # ★ 唯一 Helm chart（通用模板 + 每服务 values）
│   ├── README.md                  # 字段速查、镜像注入、回滚约定
│   ├── templates/                 # deployment/statefulset/cronjob/service/secret
│   └── values/                    # api/frontend/ai/judge/postgres/redis/rabbitmq
└── scripts/
    ├── README.md
    ├── mirror-images.ps1          # 本地把第三方镜像中转推送到 ACR
    └── install-dashboard.sh       # 安装 Kubernetes Dashboard
```

---

## 4. 容量规划（2C2G 实测基线）

| 服务 | 副本 | request (mem/cpu) | limit (mem/cpu) | 说明 |
|------|------|------------------|-----------------|------|
| api-service | 2 | 512Mi / 500m | 1Gi / **1** | JVM：CPU 给足否则启动极慢（见 8.7） |
| frontend | 1–2 | 384Mi / 200m | 640Mi / 500m | Next.js SSR |
| ai-service | 1 | 256Mi / 200m | 512Mi / 500m | FastAPI |
| judge-service | 1 | 256Mi / 500m | 1Gi / **1** | JVM + isolate 沙箱（privileged） |
| PostgreSQL | 1 | 512Mi / 300m | 1200Mi / 1000m | shared_buffers=256MB |
| Redis | 1 | 64Mi / 100m | 256Mi / 300m | appendonly + maxmemory 192MB |
| RabbitMQ | 1 | 256Mi / 200m | 512Mi / 500m | 含 management 插件 |
| Dashboard | 1 | 100Mi / 50m | 300Mi / 200m | 含 metrics-scraper |
| metrics-server | 1 | 50Mi / 50m | 100Mi / 100m | k3s 内置 |

**每台节点建议**：加 1–2G swap、`vm.swappiness=10`（默认 0 也能防 OOM，但 10 更积极）。

---

## 5. 分步实施

### 步骤 1：节点准备（每台执行）

```bash
# 1) swap（2C2G 必需，防新旧服务共存时 OOM）
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
sysctl -w vm.swappiness=10 && echo 'vm.swappiness=10' >> /etc/sysctl.conf

# 2) 端口自检（不应被占用）
ss -lntup | grep -E ':(6443|10250)\b'

# 3) 时间同步（TLS 证书校验依赖时钟）
timedatectl set-ntp true || yum install -y chrony && systemctl enable --now chronyd
timedatectl
```

### 步骤 2：安装 k3s

**离线安装（国内推荐）**：见 `deploy/k3s/README.md`（用清华/北外/南大镜像站或 ghfast 代理下载 k3s 二进制 + airgap 镜像包，再上传到各节点）。

**master**：

```bash
MASTER_PUBLIC_IP=<MASTER_IP> bash deploy/k3s/install-server.sh
# 脚本内部参数（关键）：
#   --kubelet-arg=fail-cgroupv1=false        # 老系统 cgroup v1 必需（见 8.2）
#   --flannel-backend wireguard-native
#   --flannel-external-ip                    # ★ 多云模式必需（见 8.3）
#   --node-external-ip <MASTER_IP>           # 用【公网 IP】
#   --tls-san <MASTER_IP>
#   --write-kubeconfig-mode 644
```

**agent（其余 4 台）**：

```bash
K3S_URL=https://<MASTER_IP>:6443 \
K3S_TOKEN=<master 打印的 token> \
NODE_EXTERNAL_IP=<本机公网 IP> \
bash deploy/k3s/install-agent.sh
# 注意：agent 只加 --node-external-ip + --kubelet-arg=fail-cgroupv1=false
#      ★ 千万不要在 agent 上加 --flannel-external-ip（见 8.4）
```

### 步骤 3：禁用自带 Traefik（**必做**）

```bash
# 在 master 上
printf 'disable:\n  - traefik\n' >> /etc/rancher/k3s/config.yaml
cat /etc/rancher/k3s/config.yaml      # 应含 flannel-external-ip: true 与 disable: [traefik]
systemctl restart k3s && sleep 40

# 验证
kubectl -n kube-system get deploy traefik     # 期望 NotFound
kubectl -n kube-system get ds | grep svclb    # 期望无输出
```

> 不禁用的后果：宿主机 nginx 的 80/443 被 svclb 抢占，公网访问返回 `TRAEFIK DEFAULT CERT`（见 8.8）。

### 步骤 4：节点打标

```bash
kubectl label node <db 节点名>  bluenet/role=db
kubectl label node <mq 节点名>  bluenet/role=mq
kubectl label node <master 节点名> bluenet/role=compute
kubectl label node <compute-1 节点名> bluenet/role=compute
kubectl label node <compute-2 节点名> bluenet/role=compute
kubectl get nodes -L bluenet/role        # 验证
```

> 节点名与公网 IP 的对应关系用这条命令确认：
> `kubectl get nodes -o custom-columns='NAME:.metadata.name,EXT:.status.addresses[?(@.type=="ExternalIP")].address'`

### 步骤 5：网络连通性验证（**必须做，别跳过**）

```bash
# ① 跨节点 Pod↔Pod
kubectl run nettest --restart=Never --image=<ACR>/<BASE_NS>/<BASE_REPO>:busybox-1.36 \
  --overrides='{"spec":{"nodeName":"<某个非 master 节点名>","containers":[{"name":"nettest","image":"<同上>","command":["sleep","3600"]}]}}'
kubectl exec nettest -- ping -c 3 <master 上某个 Pod 的 IP>
kubectl exec nettest -- nslookup kubernetes.default.svc.cluster.local
kubectl exec nettest -- wget -qO- --no-check-certificate https://kubernetes.default.svc.cluster.local/version   # 401 也算成功（证明链路通）
kubectl delete pod nettest
```

若 ping 不通 → `--flannel-external-ip` 缺失（见 8.3）。

### 步骤 6：镜像中转与节点凭据

```powershell
# 本地（需 Docker Desktop + 能访问 docker.io）
powershell -ExecutionPolicy Bypass -File deploy\scripts\mirror-images.ps1 `
  -Acr <ACR_HOST> -Namespace <BASE_NS> -Repo <BASE_REPO> -Username <ACR_USER>
# 中转清单：pgvector-pg17 / redis-7 / rabbitmq-3-management /
#           dashboard-v2.7.0 / metrics-scraper-v1.0.8 / busybox-1.36 / alpine-3.20
```

```bash
# 每台节点配置 ACR 认证（认证按 host 生效，与 namespace 无关）
printf 'configs:\n  "<ACR_HOST>":\n    auth:\n      username: <ACR_USER>\n      password: <ACR_PWD>\n' \
  > /etc/rancher/k3s/registries.yaml
cat /etc/rancher/k3s/registries.yaml

systemctl restart k3s          # master
systemctl restart k3s-agent    # agent

# ★ 验证必须用 crictl（会读 registries.yaml），不要用 ctr
sudo /usr/local/bin/k3s crictl pull <ACR_HOST>/<BASE_NS>/<BASE_REPO>:busybox-1.36
```

### 步骤 7：命名空间与 Secret

```bash
kubectl create namespace bluenet --dry-run=client -o yaml | kubectl apply -f -

K=--kubeconfig=<你的 kubeconfig>
# ① 三个有状态服务
kubectl $K -n bluenet create secret generic bluenet-postgres  --from-literal=postgres-password='<PG_PWD>'
kubectl $K -n bluenet create secret generic bluenet-redis     --from-literal=redis-password='<REDIS_PWD>'
kubectl $K -n bluenet create secret generic bluenet-rabbitmq  --from-literal=username=admin \
  --from-literal=password='<MQ_PWD>' --from-literal=erlang-cookie='<随机串>'

# ② api（8 个键）；PowerShell 中含 $ ! # 的值必须用单引号
kubectl $K -n bluenet create secret generic bluenet-api-secret \
  --from-literal=jwt-secret='<JWT_SECRET 建议 48 位随机>' \
  --from-literal=oss-ak='<OSS_AK>' --from-literal=oss-sk='<OSS_SK>' \
  --from-literal=mail-password='<SMTP 授权码>' --from-literal=system-user-password='<系统账号密码>' \
  --from-literal=github-client-secret='<OAuth secret>' \
  --from-literal=github-app-webhook-secret='<webhook secret>' \
  --from-literal=wps-bind-code='<WPS 码>'

# ③ ai
kubectl $K -n bluenet create secret generic bluenet-ai-secret \
  --from-literal='pgvector-uri=postgresql://postgres:<PG_PWD>@bluenet-postgres:5432/db_blue_net' \
  --from-literal='siliconflow-api-key=<KEY>' --from-literal='deepseek-api-key=<KEY>'

# ④ GitHub App 两个 PEM（以文件挂载，defaultMode 0400；应用只认路径 /app/*.pem）
kubectl $K -n bluenet create secret generic bluenet-github-keys \
  --from-file=github-issue-private-key.pem=<本地 issue pem> \
  --from-file=github-org-private-key.pem=<本地 org pem>
```

> Secret 名称必须与 chart 里引用的一致（`bluenet-postgres`/`bluenet-redis`/`bluenet-rabbitmq`/`bluenet-api-secret`/`bluenet-ai-secret`/`bluenet-github-keys`），否则 Pod 会 `CreateContainerConfigError`（见 8.14）。

### 步骤 8：部署 Helm chart

**chart 结构**：一个 `deploy/charts/bluenet` 覆盖全部服务，**每服务一份 values、一个 release**（发布/回滚互不影响）。

```bash
CHART=deploy/charts/bluenet
NS=bluenet
SVC_REPO=<ACR_HOST>/<SVC_NS>/<SVC_REPO>
BASE_REPO=<ACR_HOST>/<BASE_NS>/<BASE_REPO>

# 有状态（先装）
helm upgrade --install bluenet-postgres $CHART -n $NS -f $CHART/values/postgres.yaml --set image.repository=$BASE_REPO
helm upgrade --install bluenet-redis    $CHART -n $NS -f $CHART/values/redis.yaml    --set image.repository=$BASE_REPO
helm upgrade --install bluenet-rabbitmq $CHART -n $NS -f $CHART/values/rabbitmq.yaml --set image.repository=$BASE_REPO

# 无状态
helm upgrade --install bluenet-api      $CHART -n $NS -f $CHART/values/api.yaml      --set image.repository=$SVC_REPO --set image.tag=api-<版本>
helm upgrade --install bluenet-judge    $CHART -n $NS -f $CHART/values/judge.yaml    --set image.repository=$SVC_REPO --set image.tag=judge-<版本>
helm upgrade --install bluenet-ai       $CHART -n $NS -f $CHART/values/ai.yaml       --set image.repository=$SVC_REPO --set image.tag=ai-<版本>
helm upgrade --install bluenet-frontend $CHART -n $NS -f $CHART/values/frontend.yaml --set image.repository=$SVC_REPO --set image.tag=frontend-<版本>
```

**验收**：`kubectl -n bluenet get pods -o wide` 中 PG/Redis 应落在 db 节点、RabbitMQ 落在 mq 节点，其余自由分布；PVC 全部 Bound。

### 步骤 9：数据迁移（如从旧环境迁移）

1. 在**同实例的临时库**先预演（`restore_test`），通过后再导入正式库
2. 导入前先建扩展：`CREATE EXTENSION IF NOT EXISTS vector;`
3. 若源备份是 GUI 工具导出的 SQL，**必须先修 5 类问题**（见 8.10）
4. 导入后修正序列：`psql -f fix-sequences.sql`（解析 `pg_default` 的 DO 块 setval + 绑定 OWNED BY）
5. 验收：表数、关键表行数、`flyway_schema_history` 版本、向量检索（`order by col <=> ... limit 3`）

### 步骤 10：CD 流水线

**GitHub Secrets**（10 个）：

| Secret | 用途 |
|--------|------|
| `ACR_REPO` / `ACR_NAMESPACE` / `ACR_REPOSITORY` / `ACR_USERNAME` / `ACR_PWD` | 业务镜像推送 |
| `ACR_BASE_NAMESPACE` / `ACR_BASE_REPOSITORY` | 基础镜像（cd-infra） |
| `KUBECONFIG` | 集群部署（**永久 SA token**，见下） |
| `IVEN_PACKAGES_USER` / `IVEN_PACKAGES_TOKEN` | 私有包依赖 |

（`QODANA_TOKEN` 为组织级 secret；`GITHUB_TOKEN` 内置）

**Variables**：`K8S_BACKEND_HOST=bluenet-api`、`K8S_BACKEND_PORT=8080`、`K8S_SSL_ENABLED=false`、`PUBLIC_HOST=<PROD_DOMAIN>`、`PUBLIC_PORT=443`、`PUBLIC_SSL_ENABLED=true`、`PUBLIC_AI_*`、`AI_SERVICE_PREFIX=/ai/v1`

**CI 专用集群凭据（永久 token）**：

```bash
# master 上
kubectl -n bluenet create serviceaccount bluenet-ci
kubectl -n bluenet create rolebinding bluenet-ci-deploy --clusterrole=edit \
  --serviceaccount=bluenet:bluenet-ci

# 永久 token（无 exp）：创建这种类型的 Secret 即可，token controller 会自动填充
cat <<'EOF' | kubectl apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: bluenet-ci-token
  namespace: bluenet
  annotations:
    kubernetes.io/service-account.name: bluenet-ci
type: kubernetes.io/service-account-token
EOF

kubectl -n bluenet get secret bluenet-ci-token -o go-template='{{index .data "token"}}' | base64 -d
```

把该 token 拼成 kubeconfig（server 用 master **公网 IP**），base64 后存入 GitHub Secret `KUBECONFIG`：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("ci.kubeconfig")) | gh secret set KUBECONFIG
```

权限自检（最小权限应为：可部署、不可越权）：

```bash
KUBECONFIG=ci.kubeconfig kubectl -n bluenet auth can-i create deployments   # yes
KUBECONFIG=ci.kubeconfig kubectl auth can-i get nodes                       # no
```

**发布流程**：

```
改代码 → 提升 trigger/<svc> 版本号 → push
   → ci.yml：测试 + 构建 → ghcr.io
   → cd-<svc>.yml：ghcr 拉取 → 推 ACR（<svc>-<版本> 不可变 + <svc> 别名）→ helm upgrade
   → 成功后打 git tag deploy/<svc>/v<版本>
```

> ⚠️ **仅改 chart（探针/资源/env）也要提升 trigger 版本**——CI 的"构建不部署"规则要求版本漂移才部署（已知待改进项，见 9）。

### 步骤 11：nginx 入口切流

**业务站点**（`<PROD_DOMAIN>`）：

```nginx
location /         { proxy_pass http://<任意节点IP>:30000; }   # frontend
location /api/v1/  { proxy_pass http://<任意节点IP>:30080; }   # api
location /ai/v1/   { proxy_pass http://<任意节点IP>:30081; }   # ai
```

**公网验收**：

```bash
curl -s https://<PROD_DOMAIN>/api/v1/health    # {"code":200,...,"db":{"status":"UP"}}
curl -s https://<PROD_DOMAIN>/ai/v1/health     # {"status":"ok"}
```

**数据库对外访问（可选，四层转发）**：PostgreSQL/Redis 不是 HTTP，**不能用 http proxy**，必须用 nginx `stream` 模块（TCP 转发），且 TCP 协议无 Host 概念，**一个端口只能对应一个后端**：

```bash
kubectl -n bluenet patch svc bluenet-postgres -p '{"spec":{"type":"NodePort","ports":[{"name":"postgres","port":5432,"targetPort":5432,"nodePort":30432}]}}'
```

```nginx
# 宝塔：放到它已有的 stream 块内（/www/server/panel/vhost/nginx/tcp/*.conf）
server {
    listen 5432;
    proxy_pass <DB节点IP>:30432;      # 或 127.0.0.1:30432（nginx 本身也是 k3s 节点，可免开安全组）
    proxy_connect_timeout 10s;
    proxy_timeout 1800s;
}
```

> 坑：`stream{}` 上下文**全配置只能有一处**（宝塔已有），重复声明会 `"stream" directive is duplicate`（见 8.19）。Redis 不建议对公网暴露（存 JWT/session，L4 转发无法加认证）。

### 步骤 12：Kubernetes Dashboard

```bash
./deploy/scripts/install-dashboard.sh <ACR_HOST>/<BASE_NS>/<BASE_REPO>
```

| 项 | 值 |
|----|-----|
| 访问（本地隧道） | `kubectl -n kubernetes-dashboard port-forward svc/kubernetes-dashboard 8443:443` → https://localhost:8443 |
| 访问（公网子域，可选） | nginx 站点 → `https://127.0.0.1:30443`（**必须 `proxy_ssl_verify off`** + WebSocket 两个头 + 长 `proxy_read_timeout`） |
| 只读账号（日常） | `kubectl -n kubernetes-dashboard create token dashboard-viewer --duration=720h` |
| 管理员账号（应急） | `kubectl -n kubernetes-dashboard create token dashboard-admin --duration=720h` |

---

## 6. 验收清单（从零到上线逐项确认）

- [ ] 5 台节点 `kubectl get nodes` 全部 Ready
- [ ] 节点 label（db/mq/compute）正确
- [ ] 跨节点 Pod ping 通、CoreDNS 解析正常、ClusterIP 返回 401/200 而非超时
- [ ] `kubectl top nodes` 五台都有数据（否则 10250 未放通）
- [ ] Traefik 已禁用（`kubectl -n kube-system get deploy traefik` → NotFound）
- [ ] 各节点 `crictl pull <ACR 镜像>` 成功
- [ ] 6 个 Secret 齐全，PG/Redis/RabbitMQ Pod 未出现 CreateContainerConfigError
- [ ] PVC 全部 Bound
- [ ] 服务健康：api/judge/ai 端点 200；`https://<PROD_DOMAIN>/api/v1/health` 返回 db UP
- [ ] 前端可访问且**能登录**
- [ ] 文件上传（OSS）、判题（judge isolate）、AI 问答、GitHub Webhook 同步各跑一次
- [ ] `helm history`/`helm rollback` 各验证一次
- [ ] Dashboard 可登录（只读 token 日常用）
- [ ] 数据库对外访问（如启用）能连上
- [ ] 记录：本地 admin kubeconfig 证书到期日（1 年）、CI token 为永久（无需轮换）

---

## 7. 日常运维

```bash
# 资源与状态
kubectl top nodes
kubectl -n bluenet get pods -o wide
kubectl -n bluenet get pvc

# 日志
kubectl -n bluenet logs deploy/bluenet-api -f
kubectl -n bluenet logs -l app.kubernetes.io/name=bluenet-api --tail=100

# 本地连库/中间件（无需开放公网端口）
kubectl -n bluenet port-forward svc/bluenet-postgres 15432:5432
kubectl -n bluenet port-forward svc/bluenet-rabbitmq 15672:15672
kubectl -n bluenet port-forward svc/bluenet-redis 16379:6379

# 发布单个服务（CI 方式：提升 trigger 版本）
echo "1.0.5" > trigger/api && git commit -am "chore: 提升 api 版本" && git push

# 回滚
helm history bluenet-api -n bluenet
helm rollback bluenet-api <revision> -n bluenet

# 改 Secret 后重启生效
kubectl -n bluenet rollout restart deploy/bluenet-api
```

### 扩容（加节点）

```bash
# 1) 新节点加 swap、配 registries.yaml、安全组加入（51820/udp、10250 与所有节点互指）
# 2) 安装 agent，NODE_EXTERNAL_IP 填本机公网 IP
# 3) 打 label（compute 即可，业务自动调度过去）
kubectl label node <新节点名> bluenet/role=compute
```

---

## 8. 踩坑全记录（★ 最有价值）

| # | 现象 | 根因 | 解决 |
|---|------|------|------|
| 8.1 | master 装完 agent 连不上；`K3S_URL` 用内网 IP 超时 | 云主机是「内网 IP + 弹性公网 IP」 | `--node-external-ip` / `--tls-san` / `K3S_URL` 全用**公网 IP** |
| 8.2 | k3s 反复重启：`kubelet is configured to not run on a host using cgroup v1` | 老系统（CentOS 7 系）cgroup v1，k8s 1.35+ 默认拒绝 | `--kubelet-arg=fail-cgroupv1=false`（注意是**小写 kebab**，`failCgroupV1` 会报 unknown flag） |
| 8.3 | 节点全 Ready，但**跨节点 Pod 完全不通**（ping 100% 丢包、DNS 超时） | 缺 `--flannel-external-ip`（官方多云模式必需） | master 加该参数并重启；**修好后 ping/nslookup/ClusterIP 立即正常** |
| 8.4 | agent 加了上述参数后起不来（exit 1） | `--flannel-external-ip` **仅 server 支持** | agent 的 `config.yaml` 里不要有它（agent 从 server 拉 flannel 配置） |
| 8.5 | metrics-server 0/1，日志 `timeout to access kubelet` / `no route to host` | 10250 未放行；且部分节点本机防火墙 REJECT | 安全组 5 台互放 10250 且**含"来源=自身"**（Pod 与目标同机时的 hairpin）；再排查 firewalld/iptables |
| 8.6 | RabbitMQ 反复重启（`CrashLoopBackOff`，exit 0） | 探针 `timeoutSeconds` 默认 **1s**，2C2G 上 `rabbitmq-diagnostics ping` 常超时被杀 | 探针 `timeoutSeconds=10` + `startupProbe`（PG/Redis 也加 5s） |
| 8.7 | api 启动要 70s+，Pod 被 startup probe 杀掉重启（exit 143） | CPU limit 0.5 核 → `nr_throttled 35/38`（92% 时间片被限流） | CPU **request 500m / limit 1**；startupProbe 预算给到 10 分钟；启动耗时降到 ~32s |
| 8.8 | 公网 443 返回 `TRAEFIK DEFAULT CERT`，宝塔 nginx 明明在监听 | k3s 自带 Traefik 的 ServiceLB(`svclb-*` DaemonSet) 在每节点 hostPort 抢占 80/443 | master `config.yaml` 加 `disable: [traefik]` 并重启 k3s |
| 8.9 | api 启动成功但文件操作失败 | `ALIYUN_OSS_ENDPOINT` 用了内网 endpoint，跨 VPC 无法访问（DNS 能解析、连接超时） | 改为**公网 endpoint**（`oss-cn-<region>.aliyuncs.com`） |
| 8.10 | 恢复 GUI 导出的 SQL 备份报大量错误 | 5 类问题：① vector 列丢维度 ② 索引带非法 `COLLATE` ③ HNSW/GIN 索引带 `NULLS FIRST/LAST` ④ 未导出序列 `OWNED BY`/setval ⑤ dump 内含 pgvector 扩展函数与已装扩展冲突 | 语句级修：补 `vector(1024)`、去 COLLATE/NULLS、丢弃引用 `$libdir/vector` 的语句块、导入后跑 setval DO 块（解析 `pg_default`，不能用 `pg_get_serial_sequence`，因为没绑定） |
| 8.11 | `kubectl cp` 报 `one of src or dest must be a local file specification`；`ls /tmp/x` 变成 Windows 路径 | Windows + git-bash 的 MSYS 路径转换 + 含空格路径 | 用相对路径 + `export MSYS_NO_PATHCONV=1` |
| 8.12 | 用 `k3s ctr images pull` 验证凭据结果不可信 | `ctr` **不读** k3s 生成的 registry 认证配置 | 用 `k3s crictl pull`（走 CRI，应用 `registries.yaml`） |
| 8.13 | 某节点 pull 私有镜像报 `insufficient_scope: authorization failed` | `registries.yaml` 内容错或**写完未重启** k3s/k3s-agent | 重写文件并重启；对比各节点文件 md5 一致 |
| 8.14 | Redis Pod `CreateContainerConfigError`，事件 `secret "bluenet-redis" not found` | 创建 Secret 时漏了一个；且早期 chart 里 postgres/redis/rabbitmq 用了带 `-auth` 后缀的名称，与 api 侧引用不一致 | Secret 名称统一为 6 个约定名；创建时逐个核对 |
| 8.15 | 本地 `helm upgrade` 被中断后，后续任何 helm 操作报 "another operation in progress" | release 停在 `pending-install/pending-upgrade` | `helm uninstall <release>` 后重装（或 rollback 清理）；**集群操作尽量交给 CD，别中途打断** |
| 8.16 | 只改 chart（探针/CPU）时，推送后 CI 成功但**没有部署** | CI「构建不部署」规则：需 `trigger/<svc>` 版本漂移才部署 | 改 chart 时同步提升 trigger 版本（待改进：增加 chart-only 发布路径） |
| 8.17 | 本机 `curl https://<域名>` 返回 000，openssl 却正常 | Windows curl 用 schannel，证书吊销检查离线（`CRYPT_E_REVOCATION_OFFLINE`） | 加 `--ssl-no-revoke`（或 `-k`）——**不是服务端故障** |
| 8.18 | PowerShell 里 Secret 值被截断/变量被展开 | 值含 `$`、`!`、`#` 时双引号会被解释 | 一律用**单引号**；`sudo echo x > file` 也不行（重定向在用户层）→ 用 `sudo sh -c '...'` |
| 8.19 | 宝塔加 stream 配置报 `"stream" directive is duplicate` | stream 上下文全配置只能出现一次；宝塔已有（include `/www/server/panel/vhost/nginx/tcp/*.conf`） | 把 `server{}` 放进宝塔的 `tcp/` 目录，**不要**再写 `stream{}` |
| 8.20 | Dashboard 公网 502 | 只改了 nginx，忘了创建 NodePort；且 Dashboard 上游是 HTTPS（自签） | 建 NodePort（30443→8443）+ `proxy_ssl_verify off` + WebSocket 头 + 长超时 |

---

## 9. 已知待改进项

| 项 | 说明 |
|----|------|
| chart-only 发布路径 | 仅改 chart 也需 bump trigger 版本（多跑一次镜像构建） |
| 无自动备份 | 当前**未配置** PostgreSQL 定时备份 CronJob（决策：暂不做）→ 若启用，建议 `pg_dump` + OSS + 保留 7 天 |
| 单点风险 | 有状态服务单副本；节点宕机即服务不可用（设计已接受） |
| 本地 admin kubeconfig 到期 | 客户端证书 1 年（记录到期日，到期重新从 master 拉取 `k3s.yaml`） |
| 凭据轮换 | 首次部署期间出现过明文传递的凭据，建议按安全流程轮换一轮（OSS AK/SK、GitHub secret、LLM key、各服务密码） |
| Dashboard 公网入口加固 | 建议开启 nginx Basic Auth，并日常只用只读 token |

---

## 10. 参考

- `deploy/README.md` — 发布/回滚流程、Secrets 清单、常用命令
- `deploy/k3s/README.md` — k3s 安装（在线/离线）、ACR 凭据、排障
- `deploy/charts/bluenet/README.md` — chart 字段、镜像注入、回滚约定
- `deploy/scripts/README.md` — 镜像中转脚本
- `openspec/changes/deploy-k3s-edge-cluster/` — 本次改造的 proposal / design / spec / tasks 全记录
