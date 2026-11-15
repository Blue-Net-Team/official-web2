# local-k3s-values Specification

## Purpose

为本地五节点 k3s 提供与生产 Chart 兼容的独立 values，支持本地构建镜像经局域网 Registry 分发后直接使用 Helm 部署，并与生产域名、数据桶和发布凭据隔离。

## Requirements

### Requirement: 本地服务配置完整性

系统 SHALL 为 api、frontend、ai、judge、postgres、redis 和 rabbitmq 提供独立的本地 Helm values，并且每份配置 MUST 与现有通用 Bluenet Chart 兼容。

#### Scenario: 渲染全部本地服务

- **WHEN** 使用七份本地 values 分别执行 Helm template
- **THEN** 七个服务均生成有效 Kubernetes 资源且不依赖生产 values 叠加

### Requirement: 本地镜像来源

本地 values SHALL 使用可替换的局域网 Registry 占位符，业务服务 MUST 使用不可变的 local 镜像标签，且配置 MUST NOT 依赖 ACR 或 GitHub Actions。

#### Scenario: 注入实际本地镜像仓库

- **WHEN** 部署者通过 Helm image.repository 参数注入实际 Registry 地址
- **THEN** 渲染结果使用该 Registry 和对应服务的 local 标签

### Requirement: 本地环境参数隔离

本地 values SHALL 使用本地域名及本地对象存储 bucket 占位符，敏感信息 MUST 继续引用预建 Kubernetes Secret，且 MUST NOT 修改生产 values。

#### Scenario: 检查本地环境配置

- **WHEN** 检查 api 和 frontend 的本地 values
- **THEN** 域名、Cookie、CORS、OAuth 和公开访问地址使用一致的本地域名，OSS bucket 不是生产 bucket，所有密码和密钥均来自 Secret

### Requirement: 生产拓扑一致性

本地 values SHALL 保留生产的工作负载类型、副本数、探针、资源限制、调度约束和持久化策略。

#### Scenario: 校验关键拓扑

- **WHEN** 渲染本地工作负载
- **THEN** API 为两个 Deployment 副本，PostgreSQL/Redis/RabbitMQ 为使用 local-path 的单副本 StatefulSet，PostgreSQL/Redis 调度到 db 节点，RabbitMQ 调度到 mq 节点，Judge 仅暴露 ClusterIP 且保留沙箱特权配置
