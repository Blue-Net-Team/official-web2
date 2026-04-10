## ADDED Requirements

### Requirement: 审计切面自动记录所有接口请求
系统 SHALL 通过 AOP 切面自动拦截所有带 `@RequiresPermission` 注解的 Controller 方法，记录完整的 HTTP 请求审计信息，无需业务代码侵入。

#### Scenario: 成功的 GET 请求被记录
- **WHEN** 已认证用户发送 `GET /api/v1/users` 请求并成功返回 200
- **THEN** 系统在 `tb_audit` 表中异步写入一条记录，包含 `request_method=GET`、`request_uri=/api/v1/users`、`http_status=200`、`success_state=true`、`action_user_id` 为当前用户 ID、`duration_ms` 为实际耗时

#### Scenario: 权限校验失败被记录
- **WHEN** 未授权用户访问 PROTECTED 接口，权限切面抛出 `Forbidden` 异常
- **THEN** 审计记录中 `http_status=403`、`success_state=false`、`stack_trace` 包含异常信息、`action_user_id` 为当前用户 ID（如已认证）

#### Scenario: 未认证请求被记录
- **WHEN** 未登录用户访问 AUTHENTICATED 接口，抛出 `Unauthorized` 异常
- **THEN** 审计记录中 `http_status=401`、`success_state=false`、`action_user_id` 为 NULL

#### Scenario: 业务异常被记录
- **WHEN** Controller 方法抛出 `GlobalException`（如 404、409）
- **THEN** 审计记录中 `http_status` 为异常对应的 HTTP 状态码、`response_message` 为异常消息、`stack_trace` 包含异常堆栈

#### Scenario: 参数校验异常被记录
- **WHEN** 请求参数校验失败（`@Valid`、`MethodArgumentNotValidException`）
- **THEN** 审计记录中 `http_status=400`、`response_message` 包含校验错误信息

### Requirement: 审计日志异步写入
系统 SHALL 通过 `@Async` 注解和专用线程池异步写入审计日志，不阻塞 HTTP 请求的响应返回。

#### Scenario: 异步写入不阻塞响应
- **WHEN** Controller 方法执行完毕并返回结果
- **THEN** 审计日志写入操作在独立线程中执行，HTTP 响应立即返回，不等待数据库写入完成

#### Scenario: 线程池队列满时降级为同步写入
- **WHEN** 审计线程池队列已满（超过 500 条排队）
- **THEN** 使用 `CallerRunsPolicy` 策略，由业务线程同步执行写入，确保审计日志不丢失

### Requirement: 请求参数脱敏
系统 SHALL 在记录请求参数时对敏感字段进行脱敏处理，将敏感值替换为 `"***"`。

#### Scenario: 登录请求的密码字段被脱敏
- **WHEN** 用户发送登录请求 `POST /api/v1/auth/login/student-id`，body 为 `{"studentId":"2021001","password":"abc123hash"}`
- **THEN** 审计记录的 `action_arg` 为 `{"studentId":"2021001","password":"***"}`

#### Scenario: 验证码字段被脱敏
- **WHEN** 用户发送邮箱登录请求，body 包含 `verifyCode` 字段
- **THEN** `action_arg` 中 `verifyCode` 的值被替换为 `"***"`

#### Scenario: 无敏感字段的请求正常记录
- **WHEN** 用户发送 `POST /api/v1/enrollments`，body 不包含任何敏感字段
- **THEN** `action_arg` 完整记录请求参数，不做任何替换

### Requirement: 异常堆栈截断
系统 SHALL 对记录的异常堆栈进行截断，只保留前 2000 个字符。

#### Scenario: 短异常堆栈完整记录
- **WHEN** 异常堆栈长度小于 2000 字符
- **THEN** `stack_trace` 完整记录整个堆栈

#### Scenario: 超长异常堆栈截断
- **WHEN** 异常堆栈长度超过 2000 字符
- **THEN** `stack_trace` 只保留前 2000 个字符

### Requirement: API 耗时记录
系统 SHALL 记录每个接口请求的处理耗时（毫秒）。

#### Scenario: 正常请求记录耗时
- **WHEN** 接口请求从进入切面到返回结果耗时 150ms
- **THEN** 审计记录的 `duration_ms` 字段值为 150

#### Scenario: 异常请求也记录耗时
- **WHEN** 接口请求处理过程中抛出异常，从进入切面到异常捕获耗时 50ms
- **THEN** 审计记录的 `duration_ms` 字段值为 50

### Requirement: 切面执行顺序保证
`AuditAspect` SHALL 在 `PermissionAspect` 和 `RateLimitAspect` 外层执行（`@Order(1)`），确保能捕获所有内部异常。

#### Scenario: 审计切面捕获权限异常
- **WHEN** `PermissionAspect` 抛出权限异常
- **THEN** `AuditAspect` 能捕获该异常并记录到审计日志

#### Scenario: 审计切面捕获限频异常
- **WHEN** `RateLimitAspect` 抛出限频异常
- **THEN** `AuditAspect` 能捕获该异常并记录到审计日志
