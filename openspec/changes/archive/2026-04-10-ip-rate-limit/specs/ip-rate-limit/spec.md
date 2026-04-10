## ADDED Requirements

### Requirement: @RateLimit 注解
系统 SHALL 提供 `@RateLimit(interval)` 注解，用于声明接口级别的 IP 限频策略。`interval` 参数表示同一 IP 两次请求之间的最小间隔秒数。注解 SHALL 仅作用于方法级别（`ElementType.METHOD`）。

#### Scenario: 注解声明
- **WHEN** 开发者在 Controller 方法上添加 `@RateLimit(interval = 60)`
- **THEN** 系统 SHALL 对该方法实施 IP 限频，同一 IP 在 60 秒内只能请求一次

#### Scenario: 不同接口独立计算
- **WHEN** 接口 A 和接口 B 都添加了 `@RateLimit(interval = 60)`
- **AND** IP `10.0.0.1` 在 t=0s 请求了接口 A
- **THEN** IP `10.0.0.1` 在 t=30s 请求接口 B SHALL 正常放行
- **THEN** IP `10.0.0.1` 在 t=30s 请求接口 A SHALL 被拒绝

### Requirement: 基于 Redis 的限频实现
系统 SHALL 使用 Redis `SET key value NX EX seconds` 原子操作实现限频。Redis Key 格式为 `rate_limit:{client_ip}:{http_method}:{request_uri}`。限频拦截在业务逻辑执行前（AOP `@Around`）。

#### Scenario: 首次请求放行
- **WHEN** IP `192.168.1.1` 首次请求 `POST /api/v1/auth/verification-code/send`，接口标注 `@RateLimit(interval = 60)`
- **THEN** 系统 SHALL 在 Redis 中设置 key `rate_limit:192.168.1.1:POST:/api/v1/auth/verification-code/send`，TTL=60s
- **THEN** 请求 SHALL 正常执行

#### Scenario: 间隔内重复请求拒绝
- **WHEN** IP `192.168.1.1` 在 60 秒内再次请求同一接口
- **THEN** 系统 SHALL 返回 HTTP 429，错误消息为"请求过于频繁，请稍后再试"

#### Scenario: 间隔过后请求放行
- **WHEN** IP `192.168.1.1` 在 60 秒后请求同一接口
- **THEN** Redis key 已过期，系统 SHALL 放行请求并重新设置 key

### Requirement: IP 提取支持反向代理
系统 SHALL 按以下优先级提取客户端真实 IP：
1. `X-Forwarded-For` 请求头的第一个值（逗号分隔）
2. `X-Real-IP` 请求头
3. `HttpServletRequest.getRemoteAddr()`

#### Scenario: 通过 Nginx 代理访问
- **WHEN** 请求头包含 `X-Forwarded-For: 203.0.113.1, 10.0.0.1`
- **THEN** 系统 SHALL 提取客户端 IP 为 `203.0.113.1`

#### Scenario: 直接访问（无代理头）
- **WHEN** 请求不包含 `X-Forwarded-For` 和 `X-Real-IP` 头
- **THEN** 系统 SHALL 使用 `getRemoteAddr()` 的值作为客户端 IP

### Requirement: 限频拒绝复用现有异常体系
限频拒绝时 SHALL 抛出 `TooManyRequests` 异常，该异常继承 `GlobalException`，HTTP 状态码为 429。异常 SHALL 由现有 `GlobalExceptionHandler` 统一处理，无需额外配置。

#### Scenario: 限频拒绝响应格式
- **WHEN** 请求被限频拦截
- **THEN** 响应 HTTP 状态码为 429
- **THEN** 响应体遵循 `ResponseMessage` 格式：`{ code: 429, msg: "请求过于频繁，请稍后再试" }`

### Requirement: Redis 不可用时降级放行
系统 SHALL 在 Redis 操作异常时放行请求而非阻断业务。Redis 异常 SHALL 记录 ERROR 级别日志。

#### Scenario: Redis 连接超时
- **WHEN** Redis 连接超时导致 `setIfAbsent` 抛出异常
- **THEN** 系统 SHALL 放行当前请求
- **THEN** 系统 SHALL 记录 ERROR 日志（包含 IP 和接口信息）
