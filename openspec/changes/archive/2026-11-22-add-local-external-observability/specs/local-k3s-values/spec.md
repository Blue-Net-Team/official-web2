## MODIFIED Requirements

### Requirement: 生产拓扑一致性

本地 values SHALL 保留生产的工作负载类型、探针、资源限制、调度约束和持久化策略。为降低本地压测资源占用，API Deployment 副本数 SHALL 为一个；其他服务副本数保持现有本地配置。

#### Scenario: 校验关键拓扑

- **WHEN** 渲染本地工作负载
- **THEN** API 为一个 Deployment 副本，Judge 为两个 Deployment 副本，PostgreSQL/Redis/RabbitMQ 为使用 local-path 的单副本 StatefulSet，PostgreSQL/Redis 调度到 db 节点，RabbitMQ 调度到 mq 节点，Judge 仅暴露 ClusterIP 且保留沙箱特权配置
