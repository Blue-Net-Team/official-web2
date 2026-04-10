## Why

系统需要记录所有 API 请求的访问日志，用于安全审计、访问追溯和异常追踪。当前 `tb_audit` 表已存在但字段不匹配 HTTP 层审计需求，`RequestLoggingInterceptor` 仅输出到 SLF4J 日志不入库，`GlobalExceptionHandler` 中的异常也不持久化。需要一套完整的 AOP 切面方案，自动、异步、无侵入地记录所有接口请求。

## What Changes

- 改造 `tb_audit` 表结构：删除 `action`/`remarks` 字段，新增 `request_method`、`request_uri`、`http_status`、`response_message`、`stack_trace`、`duration_ms` 字段，使其适配 HTTP 层审计语义
- 新增 `AuditAspect` AOP 切面：拦截所有带 `@RequiresPermission` 注解的 Controller 方法，记录请求方法、地址、IP、UA、认证用户、响应码、响应消息、异常堆栈、请求耗时
- 新增 `AuditService` 应用服务：提供 `@Async` 异步写入能力，使用专用线程池，不阻塞业务请求
- 请求参数序列化并脱敏：将方法参数序列化为 JSONB 存入 `action_arg`，对 `password`、`newPassword`、`confirmPassword`、`verifyCode`、`resetToken` 等敏感字段替换为 `"***"`
- 更新 `Audit` 实体类以匹配新表结构
- 新增 Flyway 迁移脚本 `V26`

## Capabilities

### New Capabilities
- `api-audit-log`: API 审计日志 —— 基于 AOP 切面的全接口自动审计记录，包含异步写入、敏感字段脱敏、请求耗时统计

### Modified Capabilities

## Impact

- **数据库**：`tb_audit` 表结构变更（Flyway V26 迁移），已有数据可能需要处理
- **后端代码**：新增 `AuditAspect`、`AuditService`、`AuditRepository`，修改 `Audit` 实体，新增审计专用线程池配置
- **性能**：AOP 切面在接口层执行，异步写入不阻塞业务；请求参数序列化有少量 CPU 开销
- **现有功能**：`RequestLoggingInterceptor` 可保留（SLF4J 日志与数据库审计互补），不影响现有权限、限频切面
