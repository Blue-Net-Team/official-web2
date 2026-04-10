## 1. 数据库迁移

- [x] 1.1 创建 Flyway V26 迁移脚本，新增 `request_method`、`request_uri`、`http_status`、`response_message`、`stack_trace`、`duration_ms` 字段到 `tb_audit` 表
- [x] 1.2 删除旧字段 `action`、`remarks`，新增 `idx_audit_request_uri`、`idx_audit_http_status` 索引

## 2. 实体与仓储层

- [x] 2.1 更新 `Audit` 实体类，添加 `requestMethod`、`requestUri`、`httpStatus`、`responseMessage`、`stackTrace`、`durationMs` 字段，删除 `action`、`remarks` 字段
- [x] 2.2 创建 `AuditRepository` 接口和 `AuditRepositoryImpl` 实现类（位于 infrastructure/repository），提供 `insert` 方法

## 3. 敏感字段脱敏工具

- [x] 3.1 创建 `SensitiveFieldFilter`（Jackson `SimpleBeanPropertyFilter`），对 `password`、`newPassword`、`confirmPassword`、`verifyCode`、`resetToken` 等字段值替换为 `"***"`

## 4. 审计服务层

- [x] 4.1 创建审计专用线程池配置类 `AuditAsyncConfig`，定义 `auditExecutor` Bean（core=2, max=4, queue=500, CallerRunsPolicy）
- [x] 4.2 创建 `AuditService` 接口和 `AuditServiceImpl` 实现类（位于 application/service），提供 `@Async("auditExecutor")` 的 `save` 方法

## 5. AOP 审计切面

- [x] 5.1 创建 `AuditAspect` 切面类（位于 infrastructure/security/aspect），使用 `@Around("@annotation(requiresPermission)")` 和 `@Order(1)` 确保最外层执行
- [x] 5.2 实现请求信息提取：从 `HttpServletRequest` 获取 `requestMethod`、`requestUri`、`ipAddress`（`IpUtils`）、`userAgent`；从 `UserCTX` 获取 `actionUserId`
- [x] 5.3 实现响应信息提取：从 `ResponseEntity< ResponseMessage<?>>` 中提取 `httpStatus` 和 `responseMessage`
- [x] 5.4 实现异常信息提取：从 `Throwable` 中提取 `stackTrace`（截断至 2000 字符）和 `httpStatus`
- [x] 5.5 实现请求参数序列化：使用 `SensitiveFieldFilter` 脱敏后序列化方法参数为 JSONB
- [x] 5.6 实现请求计时：`System.currentTimeMillis()` 计算差值写入 `durationMs`

## 6. 验证

- [x] 6.1 手动验证：启动应用，调用多个接口，检查 `tb_audit` 表记录是否完整
- [x] 6.2 验证脱敏：检查登录接口的审计记录，确认 `password` 字段值为 `"***"`
- [x] 6.3 验证异常记录：触发权限异常和业务异常，确认审计记录包含正确状态码和堆栈
- [x] 6.4 验证异步写入：确认接口响应时间不受审计写入影响
