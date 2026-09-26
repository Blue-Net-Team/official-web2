# Tasks: deploy-k3s-edge-cluster

## 1. 集群搭建与网络安全基线

- [x] 1.1 安全组开白（已手工完成；规则含服务器 IP，不入库）：已清理 0.0.0.0/0 遗留规则并按设计 D2 放行（6443/SSH 公网认证、wireguard 节点互指、NodePort 仅 nginx 节点、DB/Redis/MQ 不公网、默认拒绝）
- [x] 1.2 编写 `deploy/k3s/install-server.sh`：k3s server 安装参数（`--flannel-backend wireguard-native`、`--node-external-ip`、`--tls-san <master公网IP>`、`--write-kubeconfig-mode 644`；kubeconfig server 地址写 master 公网 IP，供本地/CI 直接使用）
- [x] 1.3 编写 `deploy/k3s/install-agent.sh`：agent 加入脚本（`K3S_URL`/`K3S_TOKEN`/`--node-external-ip` 参数化）
- [x] 1.4 在 5 台服务器执行安装脚本，验证 `kubectl get nodes` 返回 5 个 Ready 节点（本地经公网 6443 访问；实测坑：master 为内网 IP+EIP 需用 EIP 作 `--node-external-ip`/`--tls-san`，且老系统 cgroup v1 需 `--kubelet-arg=fail-cgroupv1=false`）
- [x] 1.5 执行节点打标：`bluenet/role=db`（39.96.11.213）、`=mq`（8.146.230.107）、`=compute`（其余 3 台含 master），验证 label 查询
- [x] 1.6 验证跨节点 Pod 连通性（wireguard 隧道生效，无 MTU 问题）：实测跨节点 Pod IP ping 0% 丢包、CoreDNS 解析 `kubernetes.default.svc.cluster.local` → 10.43.0.1、经 ClusterIP 访问 apiserver 返回 401（证明链路全通）
- [x] 1.7 修复多云跨节点 CNI 连通性：master 启用 `--flannel-external-ip`（已写入 `deploy/k3s/*.sh` 与 design D2）后跨节点通信恢复；**该参数仅 server 支持，agent 配置会导致 k3s-agent 启动失败**

## 2. 集群基础组件

- [x] 2.0 配置 containerd 镜像加速/认证（`/etc/rancher/k3s/registries.yaml`）供后续拉取非离线包镜像使用；**验证必须用 `k3s crictl pull`（走 CRI，会读 registries.yaml）而非 `k3s ctr`（不读该配置）**；写完文件后必须重启 k3s/k3s-agent 才生效（实测 master 因未生效报 `insufficient_scope: authorization failed`，重写并重启后恢复）

- [ ] 2.1 确认 local-path provisioner（k3s 内置）可用，创建测试 PVC 验证供给（阻塞在：未验证）
- [x] 2.2 部署 metrics-server（k3s 内置）并验证：5 台节点均可 `kubectl top nodes`；实测坑：metrics-server 为普通 Pod，需直连节点 ExternalIP:10250，因此安全组需在 5 台节点间互放 10250/TCP（含“来源=自身”以覆盖 hairpin），且 db 节点本机防火墙（firewalld/iptables REJECT）会导致 `no route to host`
- [ ] 2.3 部署 Kubernetes Dashboard（官方 chart），验证 port-forward 可访问且不暴露公网
- [ ] 2.4 db 节点启用 swap（1-2G）并配置 vm.swappiness，写入节点初始化文档
- [x] 2.5 安全组补开 kubelet 10250/TCP（每台来源 = 其余 4 台节点公网 IP + 本机 IP，共 25 条），修复 metrics-server；并排查 db 节点本机防火墙 REJECT 导致的不通

## 3. 有状态服务（stateful-services spec）

- [x] 3.1 通用 chart `deploy/charts/bluenet` + `values/postgres.yaml`：kind=StatefulSet + volumeClaimTemplates(local-path 8Gi) + nodeSelector(bluenet/role=db) + 保守 PG 参数（shared_buffers=256MB）+ pgvector 镜像 + 密码走 Secret 引用 + /dev/shm 内存卷
- [x] 3.2 通用 chart + `values/redis.yaml`：StatefulSet + appendonly + maxmemory/allkeys-lru + nodeSelector(db) + 探针带密码
- [x] 3.3 通用 chart + `values/rabbitmq.yaml`：StatefulSet + nodeSelector(mq) + 用户名/密码/erlang-cookie 走 Secret；队列/Exchange 拓扑仍由应用侧声明
- [ ] 3.3b 创建命名空间与三个有状态服务 Secret（`bluenet-postgres` / `bluenet-redis` / `bluenet-rabbitmq`）并于集群部署验证 Pod 落在正确节点、PVC Bound
- [ ] 3.4 编写 PG 每日备份 CronJob（pg_dump → 云 OSS，03:00 Asia/Shanghai），验证备份文件落桶
- [ ] 3.5 编写 PG 恢复文档（新 PV + pg_restore），并在测试 namespace 做一次恢复演练
- [ ] 3.6 数据迁移：`pg_dump` 导出 compose 环境 PG → 导入 k3s PG（含 db_blue_net 与 rag 库），抽样校验数据一致性

## 4. 无状态服务（stateless-services spec）

- [x] 4.1 通用 chart + `values/api.yaml`：Deployment×2 + Service(NodePort 30080) + 资源配额 512m/1G + preferred anti-affinity + 探针 + GitHub App PEM 文件挂载 + 全量 env（Secret 引用）
- [x] 4.2 通用 chart + `values/frontend.yaml`：Deployment×2 + Service(NodePort 30000) + 384m/640m + anti-affinity + SSR 变量指向集群内 Service DNS
- [x] 4.3 通用 chart + `values/ai.yaml`：Deployment×1 + Service(NodePort 30081) + 256m/512m + pgvector 后端（URI 指向同库 db_blue_net）
- [x] 4.4 通用 chart + `values/judge.yaml`：Deployment×1 + privileged + 128m/1G + 仅 ClusterIP（无 NodePort）+ emptyDir 工作目录
- [ ] 4.4b 创建无状态服务的 Secret（`bluenet-api-secret` / `bluenet-ai-secret` / `bluenet-github-keys`）
- [ ] 4.5 验证 judge 沙箱在 containerd/k3s 特权容器内正常编译运行判题（isolate 兼容性确认）
- [ ] 4.6 验证 aliyun-oss 链路：文件上传与 judge 产物均落云 OSS bucket，集群内无 MinIO Pod

## 5. 配置与 Secret 管理（helm-chart-packaging spec）

- [ ] 5.1 将 compose `x-shared-env`/`x-api-service-env` 变量清单整理为各 chart values 骨架，敏感项标记为 Secret 引用
- [ ] 5.2 建立 Secret 管理方案并创建初始 Secret：`bluenet-postgres`、`bluenet-redis`、`bluenet-rabbitmq`、`bluenet-api-secret`（JWT/OSS/邮件/GitHub OAuth+App secrets/WPS）、`bluenet-ai-secret`（LLM key、pgvector URI）、`bluenet-github-keys`（两个 PEM 以文件挂载，`defaultMode: 0400`）
- [ ] 5.3 验证所有跨服务访问地址使用 Service DNS 名（如 `bluenet-postgres:5432`、`bluenet-api:8080`），无 Pod IP / 节点 IP 直连配置
- [ ] 5.4 验证仓库 git 历史无明文敏感配置泄露
- [ ] 5.5 frontend SSR 运行时变量（BACKEND_HOST 等）指向集群内 Service DNS，NEXT_PUBLIC_* 构建期变量移交 CI build args

## 6. 入口切流（nginx + NodePort）

- [ ] 6.1 将 8.146.230.107 宿主机 nginx upstream 改为节点 NodePort（api:30080、frontend:30000、ai:30081，各配 2-3 个节点 IP 兜底；`/ai/v1` 路径转发 ai NodePort）
- [ ] 6.2 按序切流：ai-service → judge-service → api-service → frontend，每段验证通过后继续
- [ ] 6.3 全链路回归：登录、题目提交（含文件上传 100 QPS 量级压测）、判题、AI 问答、GitHub Issue 同步

## 7. CI/CD 链路（cicd-helm-deploy spec）

- [ ] 7.1 准备镜像仓库（阿里云 ACR：基础镜像与业务镜像同 host、不同 namespace），配置集群 imagePullSecret/`registries.yaml` 与 GitHub Secrets 凭据；**真实 ACR 地址与 namespace 不入库**（chart values 仅占位符，部署时用 `--set image.repository` 注入，业务侧由 `ACR_REPO`/`ACR_NAMESPACE` 提供）
- [ ] 7.2 创建 CI 专用 ServiceAccount + RBAC（限 bluenet namespace deploy 权限），签发短周期 token kubeconfig，存入 GitHub 仓库 Secret（名：`KUBECONFIG`）；使用 GitHub 托管 runner（6443 公网可达 + 认证，无需 runner IP 白名单）；token 存 GitHub Secrets，泄露即吊销重建。具体步骤：
  ```bash
  # 在 master 上执行
  kubectl create namespace bluenet
  kubectl create serviceaccount bluenet-ci -n bluenet
  kubectl create rolebinding bluenet-ci-deploy --clusterrole=edit \
    --serviceaccount=bluenet:bluenet-ci -n bluenet
  kubectl create token bluenet-ci -n bluenet --duration=720h   # 30天，到期轮换
  # 以 /etc/rancher/k3s/k3s.yaml 的 clusters 段 + 上面 token 拼 CI 专用 kubeconfig
  # （server 写 master 公网 IP https://123.56.253.250:6443，users 段仅 token，不用 admin 证书）
  ```
  - kubeconfig YAML 粘贴到 GitHub Secret `KUBECONFIG`（Settings → Secrets and variables → Actions）
  - 本地留存一份于 `deploy/secrets/ci.kubeconfig` 作为备份/排查用，**必须加入 `.gitignore`**（追加 `*.kubeconfig` 规则），并验证 `git status` 不出现该文件、git 历史无泄露
  - 轮换流程：token 到期前重跑 `kubectl create token ... --duration=720h` → 更新 GitHub Secret → 零停机
- [ ] 7.3 编写 GitHub Actions workflow：构建 api/judge/ai/frontend 镜像（frontend 带 build args）→ 推送仓库（**tag 必须不可变：`<服务名>-<git短SHA>`；`-latest` 仅供调试不得用于部署**）→ helm upgrade --install（`--set image.tag=<服务名>-<git短SHA>`，可选 `--set image.digest=...`）→ rollout status
- [ ] 7.4 端到端验证：推送一次提交，确认集群自动更新且全程无 SSH
- [ ] 7.5 验证 `helm rollback <release> <revision>` 回滚路径：确认历史 tag 镜像仍在 ACR、Pod 成功回到旧版本（若 CI 用固定 tag，此验证必然失败）

## 8. 可观测与收尾（cluster-observability spec）

- [ ] 8.1 为 PG Pod 配置内存告警（limit 80% 阈值），接入通知通道
- [ ] 8.2 compose 环境保留一周作为回滚源（infra profile 只读），验证稳定后归档 compose 生产部署文档
- [ ] 8.3 编写运维文档：`deploy/README.md`（集群拓扑、常用命令、回滚、备份恢复指引）
- [ ] 8.4 更新根 README / docs 部署章节，指向 k3s 部署为新生产路径
