# Spec: k3s-cluster-topology

## ADDED Requirements

### Requirement: 集群规模与拓扑
系统 SHALL 由 1 个 k3s server 节点与 4 个 agent 节点组成，覆盖 5 台 2C2G 云服务器（172.18.116.241、8.146.230.107、39.96.11.213、182.92.241.91、123.56.253.250），节点间仅通过公网互访。

#### Scenario: 集群就绪
- **WHEN** 在管理端执行 `kubectl get nodes`
- **THEN** 返回 5 个 Ready 节点，roles 显示 1 个 control-plane 与 4 个 worker

### Requirement: 节点角色 label
系统 SHALL 以 label `bluenet/role` 标记节点：39.96.11.213 为 `db`，8.146.230.107 为 `mq`，其余节点为 `compute`。所有调度约束 MUST 通过 label 表达，任何 manifest、values、脚本中 MUST NOT 硬编码节点 IP。

#### Scenario: 按 label 查询节点
- **WHEN** 执行 `kubectl get nodes -l bluenet/role=db`
- **THEN** 仅返回 39.96.11.213 节点

### Requirement: 公网传输加密
集群 Pod 网络 MUST 使用 wireguard 加密（`--flannel-backend wireguard-native`），每个节点 MUST 配置 `--node-external-ip` 为其公网 IP。

#### Scenario: 跨节点 Pod 连通
- **WHEN** 在 node-3 上的 Pod 访问 node-4 上 Pod 的 ClusterIP
- **THEN** 请求成功，且流量经 wireguard 加密隧道传输

### Requirement: 管理面公网暴露与认证
master 的 6443（k3s API）MUST 公网可达，安全 MUST 依赖 TLS + token/客户端证书认证；全部节点 22（SSH）维持公网密钥认证现状。CI/运维凭据 MUST NOT 使用 cluster-admin，MUST 采用最小 RBAC 权限的独立 ServiceAccount + 短周期 token，并定期轮换。

#### Scenario: 无凭据访问被拒
- **WHEN** 任意公网客户端无 token/证书连接 master:6443
- **THEN** TLS 握手或认证失败，无法获得任何 API 响应

#### Scenario: 凭据最小权限
- **WHEN** 审查 CI 所用 RBAC 绑定
- **THEN** 仅授予目标 namespace 内必要写权限，无集群级权限

### Requirement: 安全组白名单
flannel/wireguard 端口（51820/UDP）MUST 仅对 5 节点公网 IP 互放；NodePort（30000-32767）MUST 仅对 nginx 节点（8.146.230.107）公网 IP 开放；nginx 节点 80/443 对公网开放；上述规则 MUST NOT 对 0.0.0.0/0 开放（6443/22/80/443 除外）。PostgreSQL(5432)、Redis(6379)、RabbitMQ(5672/15672) MUST NOT 暴露公网（区别于 compose 时代，仅集群内 ClusterIP 访问）；其余入站一律默认拒绝；出网默认放行（镜像拉取、系统更新需要）。

#### Scenario: 未授权访问被拒绝
- **WHEN** 任意非白名单 IP 访问 master:6443 或任一节点 NodePort
- **THEN** 连接被安全组拒绝
