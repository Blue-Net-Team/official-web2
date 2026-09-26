# cluster-observability Specification

## Purpose
TBD - created by archiving change deploy-k3s-edge-cluster. Update Purpose after archive.
## Requirements
### Requirement: 集群管理面板
集群 MUST 部署 Kubernetes Dashboard（官方 chart），管理员可通过浏览器查看全部节点、Pod、工作负载与日志；Dashboard MUST 不暴露公网（仅经 `kubectl port-forward` 或白名单访问）。

#### Scenario: 面板可用
- **WHEN** 管理员通过 port-forward 访问 Dashboard
- **THEN** 可查看节点资源用量、各 namespace 工作负载与 Pod 日志

### Requirement: 指标服务
集群 MUST 部署 metrics-server，为 Dashboard 与 `kubectl top` 提供资源用量数据。

#### Scenario: 资源指标可查询
- **WHEN** 执行 `kubectl top nodes` 与 `kubectl top pods -A`
- **THEN** 返回各节点/Pod 的 CPU 与内存用量

### Requirement: PG 内存告警
Dashboard/监控 MUST 为 PostgreSQL Pod 配置内存用量告警（达到 limit 的 80% 时通知），告警通道可为邮件或群机器人。

#### Scenario: 告警触发
- **WHEN** PG Pod 内存用量持续超过其 limit 的 80%
- **THEN** 管理员收到告警通知

