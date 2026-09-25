# Spec: helm-chart-packaging

## ADDED Requirements

### Requirement: Chart 覆盖全部服务
`deploy/charts/` 下 MUST 为每个服务提供独立 Helm chart：bluenet-api、bluenet-frontend、bluenet-judge、bluenet-ai、bluenet-postgres、bluenet-redis、bluenet-rabbitmq。每个 chart MUST 可通过 `helm upgrade --install` 独立发布。

#### Scenario: 独立发布单个服务
- **WHEN** CI 仅构建 api-service 并执行其 chart 的 `helm upgrade --install`
- **THEN** 仅 api-service 的 Deployment 更新，其余服务不受影响

### Requirement: 配置来源约定
非敏感配置 MUST 通过各 chart 的 values.yaml 管理；敏感配置（数据库密码、JWT_SECRET、GitHub App 私钥、LLM API key、OSS 凭据）MUST 引用 k8s Secret，chart MUST NOT 在 values 或模板中明文承载敏感值。

#### Scenario: values 不含明文密钥
- **WHEN** 审查任一 chart 的 values.yaml 与渲染后的模板
- **THEN** 不存在任何明文密码、私钥或 token

### Requirement: Service 服务发现
每个 chart MUST 包含 Service 模板：api（NodePort 30080）、frontend（NodePort 30000）、ai（NodePort 30081，供 nginx /ai/v1 转发）；judge、postgres、redis、rabbitmq 为 ClusterIP。所有跨服务访问 MUST 通过 Service DNS 名（如 `bluenet-postgres:5432`），MUST NOT 直连 Pod IP 或节点 IP。

#### Scenario: DNS 发现可用
- **WHEN** api-service Pod 解析 `bluenet-postgres` 主机名
- **THEN** 成功解析到 postgres Service ClusterIP 且连接成功

### Requirement: 镜像与标签约定
chart 的 image repository/tag MUST 参数化，tag 默认指向发布版本（CI 注入），MUST NOT 使用 `latest` 作为默认发布标签。

#### Scenario: 发布指定版本
- **WHEN** CI 以 `--set image.tag=<git-sha>` 执行 upgrade
- **THEN** Deployment 滚动更新至该镜像版本

### Requirement: Secret 版本化管理
敏感配置 SHOULD 以 sops/age 加密文件存于仓库（deploy/secrets/），解密仅在 CI 中进行；若采用 CI 纯注入方案，集群内 Secret 的变更 MUST 可追溯到 CI 运行记录。

#### Scenario: 仓库无泄露
- **WHEN** 检索仓库 git 历史
- **THEN** 任何提交中均不含明文敏感配置
