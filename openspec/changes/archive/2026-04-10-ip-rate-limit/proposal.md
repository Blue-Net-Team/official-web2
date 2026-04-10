## Why

验证码发送接口的限频当前依赖数据库查询（`findLatestByEmailWithinSeconds`），高并发下会成为数据库瓶颈。同时 `tb_verify_code` 表的 `ip_address` 列从未被正确写入（三层断裂：VO 无字段、领域服务丢弃、Repository 未映射），无法提供 IP 维度的安全防护。需要引入基于 Redis 的 IP 限频机制，解耦限频逻辑与数据库，并清理相关死代码。

## What Changes

- 新增 `@RateLimit(interval)` 注解，用于声明接口级别的 IP 限频策略
- 新增 `RateLimitAspect` 切面，基于 Redis SETNX + TTL 实现原子限频，按 `{ip}:{http_method}:{uri}` 独立计算
- 新增 `IpUtils` 工具类，从 `X-Forwarded-For` / `X-Real-IP` / `getRemoteAddr()` 提取真实客户端 IP
- 新增 `TooManyRequests` 异常类（继承 `GlobalException`，HTTP 429），复用现有异常体系
- 将 `@RateLimit(interval=60)` 注解应用到三个验证码发送接口
- 删除 `tb_verify_code.ip_address` 列及相关的死代码链路（领域服务 IP 参数、Repository 限频查询方法、Service 层限频调用）
- **BREAKING**: 验证码发送的限频从数据库查询 + 邮箱维度切换为 Redis + IP 维度

## Capabilities

### New Capabilities
- `ip-rate-limit`: 基于 Redis 的 IP 限频注解机制，支持按接口独立配置最小请求间隔

### Modified Capabilities
- `forgot-password`: 移除 sendCode 中的数据库限频查询和 IP 参数传递，改由 `@RateLimit` 注解处理
- `email-verification-login`: 移除 sendVerificationCode 中的数据库限频查询，改由 `@RateLimit` 注解处理
- `backend-user-profile`: 移除 sendEmailVerificationCode 中的数据库限频查询，改由 `@RateLimit` 注解处理

## Impact

- **后端代码**：新增 4 个文件（注解、切面、工具类、异常），修改 3 个 Controller，删除领域层/仓储层/应用层的限频相关代码
- **数据库**：新增 Flyway 迁移删除 `tb_verify_code.ip_address` 列
- **依赖**：无新增依赖，复用已有的 `StringRedisTemplate`
- **API 行为**：限频拒绝时返回 HTTP 429（原为 400），限频维度从 email+scene 变为 IP+接口
- **Redis**：新增 `rate_limit:*` 前缀的 Key，每个限频接口每 IP 占用一个 Key
