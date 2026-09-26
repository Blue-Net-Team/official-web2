# Proposal: deploy-k3s-edge-cluster

## Why

当前项目以单机 docker-compose 方式部署在多台 2C2G 云服务器上，每次发布都需要 SSH 登录到不同机器手动操作，无统一编排与面板，且数据库/缓存等基础设施与应用耦合在同一 compose 拓扑中难以维护。需要将部署方式改造为 k3s 边缘集群，实现统一编排、声明式发布（Helm CD）与集群可视化管理，同时"不 SSH 到各服务器部署"。

## What Changes

- 在 5 台 2C2G 云服务器（跨云账号、无内网互通、公网互访）上搭建 k3s 集群（1 master + 4 agent），跨公网链路使用 wireguard 加密 overlay，安全组按节点公网 IP 白名单放通
- 节点以 label（`bluenet/role=db|mq|compute`）标记角色，有状态服务（PostgreSQL、Redis、RabbitMQ）通过 nodeSelector 钉在对应节点，local-path PV 承载数据；**接受单副本、节点宕机即该服务不可用**的取舍
- 无状态服务（api-service、frontend、ai-service、judge-service）不指定节点，由 scheduler 自由装箱，以 request/limit 塑形 + preferred podAntiAffinity 保证副本分散
- judge-service 以 privileged 容器运行（isolate 沙箱需求）；威胁模型：仅受信登录用户可提交代码，接受其容器逃逸风险，不做特殊调度隔离
- 新增 `deploy/charts/` Helm chart 体系（api / frontend / judge / ai / postgres / redis / rabbitmq 等），所有运行时配置从 compose 环境变量迁移至 k8s Secret / values
- 新增 GitHub Actions CD：构建镜像推送镜像仓库后，通过 helm upgrade 直接更新集群，不 SSH 到任何节点
- 部署 Kubernetes Dashboard（官方 chart）作为集群管理面板
- 8.146.230.107 宿主机 nginx 保留为公网入口，upstream 改为各节点 NodePort；域名与证书管理不变
- 基础设施去 MinIO 化：对象存储切换为云 OSS（STORAGE_PROVIDER=aliyun-oss），judge 产物桶同用云 OSS；向量检索使用 pgvector（随 PostgreSQL 部署），**不引入** Milvus
- PostgreSQL 每日逻辑备份 CronJob，备份产物上传云 OSS
- **BREAKING（部署层面）**：docker-compose 部署方式下线，原 `docker/docker-compose.yml` 的 app profile 不再作为生产部署路径（开发本地环境可保留）

## Capabilities

### New Capabilities

- `k3s-cluster-topology`: 集群节点拓扑、label 角色划分、公网互访网络安全基线（wireguard、安全组白名单、6443 访问控制）
- `helm-chart-packaging`: 各服务的 Helm chart 结构、values 与镜像构建参数约定、Secret 管理规范
- `stateful-services`: PostgreSQL（含 pgvector）、Redis、RabbitMQ 的集群内单副本部署、local PV、备份策略与参数基线
- `stateless-services`: api/frontend/ai/judge 的弹性调度、资源配额（request/limit）、副本数、反亲和与健康检查约定
- `cicd-helm-deploy`: GitHub Actions 构建镜像、推送镜像仓库、helm upgrade 集群的完整发布链路
- `cluster-observability`: Kubernetes Dashboard、metrics-server、PG 内存告警等基础可观测能力

### Modified Capabilities

（无既有 spec 需求变更；本变更为纯部署/运维能力新增）

## Impact

- **新增目录**：`deploy/`（k3s 安装脚本、helm charts、CD workflow、文档）
- **CI/CD**：新增 GitHub Actions workflow（build + push + helm upgrade）
- **镜像仓库**：需要可用的容器镜像仓库（阿里云镜像服务或 ghcr），CI 凭据以 Secrets 配置
- **配置迁移**：`docker/.env` 中的运行时变量迁移为 k8s Secret；GitHub App 私钥（issue/org 两个 pem）改为 Secret 挂载
- **基础设施**：5 台服务器的安全组规则需按白名单调整；master 节点 6443 仅对 CI runner 与管理端 IP 开放
- **nginx 节点**：upstream 配置由具体后端 IP 改为节点 NodePort
- **回滚路径**：迁移期间 compose 与 k3s 可短暂并行，切流后 compose app profile 退役
