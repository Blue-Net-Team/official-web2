# cicd-helm-deploy Specification

## Purpose
TBD - created by archiving change deploy-k3s-edge-cluster. Update Purpose after archive.
## Requirements
### Requirement: Push-to-deploy 链路
代码推送（或 release 发布）MUST 触发 GitHub Actions 自动完成：构建镜像 → 推送镜像仓库 → `helm upgrade --install` 更新集群 → `kubectl rollout status` 等待就绪。整个链路 MUST NOT 包含 SSH 登录任何服务器的步骤。

#### Scenario: 自动发布
- **WHEN** 开发者推送指定分支
- **THEN** workflow 成功结束，集群内对应服务更新为新版本，全程无 SSH 步骤

### Requirement: 集群访问凭据
CI 使用独立 kubeconfig（长期 token + RBAC 限定 deploy 权限）访问 master 公网 6443；master 安全组 MUST 放通 GitHub Actions runner 出口 IP 段。kubeconfig MUST NOT 使用 cluster-admin 凭据。

#### Scenario: 凭据最小权限
- **WHEN** 审查 CI 所用 RBAC 绑定
- **THEN** 仅授予目标 namespace 内 Deployment/Secret 等必要的写权限，无集群级权限

### Requirement: 镜像仓库与拉取
镜像 MUST 推送至国内可稳定访问的镜像仓库（阿里云 ACR 或等效）；集群节点 MUST 配置 imagePullSecret 拉取私有镜像。

#### Scenario: 节点可拉取镜像
- **WHEN** 任一节点上新建业务 Pod
- **THEN** 镜像拉取成功，Pod 进入 Running

### Requirement: 回滚能力
任一服务 MUST 支持 `helm rollback <release> <revision>` 回滚至上一版本且回滚后服务健康。

#### Scenario: 回滚成功
- **WHEN** 新版本异常并执行 helm rollback
- **THEN** Pod 恢复至上一镜像版本，服务健康检查通过

