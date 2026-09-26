# stateful-services Specification

## Purpose
TBD - created by archiving change deploy-k3s-edge-cluster. Update Purpose after archive.
## Requirements
### Requirement: 有状态服务节点钉定
PostgreSQL 与 Redis MUST 通过 `nodeSelector: bluenet/role: db` 调度；RabbitMQ MUST 通过 `nodeSelector: bluenet/role: mq`。三者 MUST 为单副本，MUST 使用 local-path PV 持久化，MUST NOT 配置跨节点漂移。

#### Scenario: Pod 与数据同节点
- **WHEN** 查看 PG Pod 详情
- **THEN** 其所在节点为 db label 节点，挂载的 PV 为 local-path 且路径位于该节点本地磁盘

### Requirement: 资源配额
有状态服务的 request/limit MUST 符合设计基线：PostgreSQL 512m/1.2G、Redis 64m/256m、RabbitMQ 256m/512m（内存 request/limit）。PG MUST 配置保守参数（shared_buffers=256MB 等）且 db 节点启用 swap。

#### Scenario: 配额可审计
- **WHEN** 查看三个有状态 Deployment/StatefulSet 的 resource 字段
- **THEN** 数值与设计基线一致

### Requirement: PostgreSQL 每日备份
系统 SHALL 提供每日 03:00（Asia/Shanghai）执行的 `pg_dump` CronJob，备份产物 MUST 上传云 OSS，保留策略不少于 7 天。恢复流程 MUST 形成文档（新 PV + pg_restore）。

#### Scenario: 备份产物存在
- **WHEN** 在备份执行后检查 OSS 目标 bucket
- **THEN** 存在当日的 db_blue_net 逻辑备份文件

### Requirement: 向量检索使用 pgvector
PostgreSQL MUST 使用 pgvector/pgvector:pg17 镜像；ai-service 的向量存储后端 MUST 配置为 pgvector，指向集群内 PG Service，MUST NOT 部署 Milvus。

#### Scenario: 向量检索可用
- **WHEN** ai-service 执行文档向量入库与检索
- **THEN** 数据存储于 PG 的向量表且检索返回结果，集群内无 Milvus 相关 Pod

