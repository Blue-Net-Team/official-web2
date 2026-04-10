## Context

当前系统存在 `tb_audit` 表（V1 建表），字段面向"操作行为"设计（`action`、`action_arg`、`remarks`），不适合 HTTP 层审计。`RequestLoggingInterceptor` 仅输出 SLF4J 日志不入库，`GlobalExceptionHandler` 中异常也不持久化。项目已有 `@EnableAsync`（`BluenetBackendApplication`）、`PermissionAspect`、`RateLimitAspect` 等 AOP 切面和 `IpUtils`、`UserCTX` 等工具类，审计切面可复用这些基础设施。

## Goals / Non-Goals

**Goals:**
- 所有带 `@RequiresPermission` 的接口自动记录审计日志，无需业务代码侵入
- 记录：认证用户、IP、UA、请求方法、请求地址、请求参数（脱敏）、响应码、响应消息、异常堆栈、请求耗时
- 异步写入数据库，不阻塞业务请求
- 敏感字段（password、verifyCode、resetToken 等）自动脱敏

**Non-Goals:**
- 不记录领域服务或应用层行为（属于业务日志，用 SLF4J 即可）
- 不实现审计日志的查询/管理 API（后续可独立变更）
- 不替换现有 `RequestLoggingInterceptor`（SLF4J 日志与数据库审计互补）
- 不实现审计日志的自动清理/归档策略

## Decisions

### 1. AOP 切面 + @Async 异步写入（不使用 Spring Event）

**选择**：`AuditAspect`（`@Around`）+ `AuditService.save()`（`@Async`）+ 专用线程池

**备选方案**：
- Spring Event + `@EventListener`：解耦好，但审计日志只有一个消费者，引入 Event 是多余间接层
- 线程池 `executor.submit()`：有效但不如 `@Async` 声明式，且项目已有 `@Async` 使用模式（`EmailSenderImpl`）

**理由**：项目已 `@EnableAsync`，邮件发送已用 `@Async`，保持一致。`CallerRunsPolicy` 拒绝策略确保审计日志不丢失。

### 2. 切点选择：`@RequiresPermission` 注解

**选择**：`@Around("@annotation(requiresPermission)")`

**备选方案**：`execution(* com.bluenet.web.api..*Controller.*(..))`——包级别切点

**理由**：所有接口都有 `@RequiresPermission`，且能在注解中获取权限标识（`value`、`name`）用于审计描述。

### 3. 切面执行顺序：`@Order(1)` 最外层

`AuditAspect` 必须在 `PermissionAspect`、`RateLimitAspect` 外层，才能捕获权限异常和限频异常。

```
AuditAspect (@Order(1))     ← 最外层，捕获所有结果/异常
  → PermissionAspect        ← 权限异常
    → RateLimitAspect       ← 限频异常
      → Controller          ← 业务异常
```

### 4. 敏感字段脱敏

维护 `Set<String> SENSITIVE_FIELDS`，在 Jackson 序列化方法参数时将匹配字段值替换为 `"***"`。

脱敏字段列表：`password`、`newPassword`、`confirmPassword`、`verifyCode`、`resetToken`。

### 5. 异常堆栈截断

`stack_trace` 字段（TEXT 类型）只保留前 2000 个字符，避免超长堆栈撑爆字段。

### 6. 表结构改造策略

通过 Flyway V26 迁移：新增字段 → 清空旧数据（量少可接受） → 删除旧字段 → 新增索引。

## Risks / Trade-offs

- **[性能] 请求参数序列化开销** → 仅在 AOP 切面中序列化，`action_arg` 限制为 JSONB，开销可控
- **[可靠性] 异步写入可能丢日志** → `CallerRunsPolicy` 兜底，队列满时同步写入，宁可慢不丢数据
- **[存储] 审计表增长快** → 暂不处理，后续可加定期清理策略
- **[兼容] 旧数据迁移** → `tb_audit` 目前无生产数据，直接清空旧字段数据即可
