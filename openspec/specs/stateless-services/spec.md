# stateless-services Specification

## Purpose
TBD - created by archiving change deploy-k3s-edge-cluster. Update Purpose after archive.
## Requirements
### Requirement: 弹性调度
api-service、frontend、ai-service、judge-service MUST NOT 配置 nodeSelector 或节点亲和，其调度位置 MUST 完全由 scheduler 依据 request/limit 决定。judge-service MUST 与其他无状态服务同等对待，不设置特殊调度约束。

#### Scenario: 自由装箱
- **WHEN** 删除任一 compute 节点上的业务 Pod
- **THEN** 重建的 Pod 可由 scheduler 分配到任意满足资源条件的节点

### Requirement: 副本数与资源配额
副本数 MUST 为：api-service 2、frontend 2、ai-service 1、judge-service 1。资源 request/limit（内存）MUST 符合：api 512m/1G、frontend 384m/640m、ai 256m/512m、judge 128m/1G。

#### Scenario: 配额合规
- **WHEN** 审计四个无状态 Deployment
- **THEN** replicas 与 resources 字段均符合上述基线

### Requirement: 副本分散
api-service 与 frontend 的 Pod 间 MUST 配置 preferred podAntiAffinity（尽量不同节点）；任何副本被调度到与 db/mq 有状态 Pod 同节点是允许的（资源允许时）。

#### Scenario: 默认分散
- **WHEN** 集群余量充足时查看 api-service 两个 Pod
- **THEN** 两 Pod 位于不同节点

### Requirement: judge 特权容器
judge-service MUST 以 privileged 容器运行（isolate 沙箱需求），MUST NOT 暴露公网入口（仅集群内 Service）。该特权模式的安全风险（受信用户代码）已在设计中评估并接受。

#### Scenario: 沙箱执行正常
- **WHEN** 通过 api 提交判题任务
- **THEN** judge-service 在沙箱内完成编译运行并回传结果，judge Pod 无公网 NodePort

### Requirement: 对象存储使用云 OSS
api-service 与 judge-service MUST 配置 `STORAGE_PROVIDER=aliyun-oss`，判题产物桶 MUST 为云 OSS bucket，集群内 MUST NOT 部署 MinIO。

#### Scenario: 文件链路走 OSS
- **WHEN** 用户上传题目文件并产生判题产物
- **THEN** 文件与产物均位于云 OSS，集群内无 MinIO Pod

### Requirement: 健康检查与滚动更新
所有无状态服务 MUST 配置 liveness/readiness 探针；发布 MUST 采用 RollingUpdate（maxSurge/maxUnavailable 合理配置）保证服务不中断。

#### Scenario: 发布不中断
- **WHEN** CI 发布 api-service 新版本
- **THEN** 滚动更新期间健康检查持续通过，无全量中断窗口

